# 배포 메모 (개인용)

Docker + GitHub Actions로 우분투 서버에 배포하는 절차. `production` 브랜치에 push하면 자동 배포됨.

이미지: `ghcr.io/ghlee0303/dayogg`
배포 방식: production push → Actions가 이미지 빌드 → ghcr 푸시 → 서버 SSH 접속해 pull & 재기동

---

## A. 최초 1회만 (한 번 해두면 끝)

### 1. 서버 — Docker 설치

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER      # 재접속해야 적용됨
docker compose version             # 확인
```

### 2. 서버 — 배포 폴더 만들기

소스 clone 필요 없음. compose 파일 + .env 두 개만 있으면 됨.

```bash
mkdir -p ~/dayogg && cd ~/dayogg
# 로컬에서 docker-compose.prod.yml 을 여기로 복사:
#   scp docker-compose.prod.yml <user>@<서버IP>:~/dayogg/
cp .env.example .env
nano .env          # DB_PASSWORD, GAME_API_KEY 실제 값 입력 (change-me 지우기)
```

> `.env.example`도 같이 scp로 올려두면 편함.

### 3. SSH 키 만들기 (Actions가 서버 접속할 때 씀)

```bash
ssh-keygen -t ed25519 -f deploy_key
ssh-copy-id -i deploy_key.pub <user>@<서버IP>   # 공개키를 서버에 등록
cat deploy_key                                  # 이 개인키 전체를 SSH_KEY 시크릿에 붙여넣기
```

### 4. GitHub Secrets 등록

레포 → Settings → Secrets and variables → Actions → New repository secret

| 이름 | 값 |
|------|-----|
| `SSH_HOST` | 서버 IP |
| `SSH_USER` | 접속 계정 (예: `ubuntu`) |
| `SSH_KEY` | `deploy_key` (개인키) 내용 전체 |
| `SSH_PORT` | SSH 포트 (22면 안 넣어도 됨) |
| `DEPLOY_PATH` | `/home/ubuntu/dayogg` (2번에서 만든 폴더) |
| `GHCR_TOKEN` | ghcr pull용 PAT — 아래 참고 |

**GHCR_TOKEN 발급**: GitHub → Settings → Developer settings → Personal access tokens (classic) → `read:packages`만 체크해서 생성.
→ 귀찮으면 이미지 패키지를 public으로 바꾸고 이 토큰 생략 가능 (레포 → Packages → dayogg → Package settings → Change visibility).

### 5. 서버 방화벽 (선택)

```bash
sudo ufw allow 22 && sudo ufw allow 8080 && sudo ufw enable
```

---

## B. 배포할 때마다 (반복)

평소 개발은 `main`, 배포는 `production`으로 올릴 때만.

```bash
git checkout production
git merge main
git push               # ← 이 순간 배포 시작됨
```

진행 상황: GitHub 레포 → **Actions** 탭에서 실시간 로그 확인.

> production 브랜치 처음 만들 때: `git checkout -b production && git push -u origin production`

---

## C. 확인 / 문제 생겼을 때 (서버에서)

```bash
cd ~/dayogg

docker compose -f docker-compose.prod.yml ps          # 컨테이너 상태
docker compose -f docker-compose.prod.yml logs -f app # ⚠ 안 나옴. 앱 로그는 CloudWatch — 아래 참고
docker compose -f docker-compose.prod.yml logs redis  # Redis 로그 (MySQL 은 RDS 라 여기 없음)

# 수동으로 다시 받아 재기동
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d

# 전체 재시작 / 내리기
docker compose -f docker-compose.prod.yml restart
docker compose -f docker-compose.prod.yml down        # 볼륨은 유지됨
```

### 앱 로그 조회는 CloudWatch

`app` 컨테이너는 `awslogs` 드라이버를 쓴다 (`docker-compose.prod.yml` 의 `logging` 블록).
stdout 이 ECS JSON 한 줄씩 나가고, 그대로 CloudWatch 로 올라가 **필드 단위 질의**가 된다.

```
CloudWatch → Log Management → /dayogg/app        (리전 ap-northeast-2)
CloudWatch → Log analytics                       (Logs Insights, 질의)
```

```sql
-- 느린 호출
fields @timestamp, method, elapsedMs
| filter elapsedMs > 1000
| sort elapsedMs desc

-- 에러
fields @timestamp, method, `error.message`
| filter `log.level` = "ERROR"
```

점(`.`) 든 필드는 백틱으로 감싼다. `code`·`elapsedMs`·`playerId` 는 그냥 쓴다.

알아둘 것:

- **보존기간 14일.** 새 로그 그룹의 기본값은 무기한이라 영원히 과금된다. 그룹을 새로 만들면
  즉시 Actions → Edit retention setting 에서 걸 것.
- **`dayogg logs` 로 app 로그는 못 본다.** awslogs 는 읽기를 지원하지 않는다.
  도커 20.10+ 의 dual logging 이 읽기용 로컬 사본을 남겨주긴 하는데, EC2 디스크가
  빠듯해 `cache-disabled: "true"` 로 껐다. **app 로그 조회 창구는 CloudWatch 하나뿐이다.**
- `dayogg logs redis` 는 그대로 된다 (redis 는 `json-file` 유지).
- `mode: non-blocking` 이라 버퍼(4m)를 넘긴 로그는 **조용히 버려진다.** 전송이 밀려도
  앱이 안 느려지는 대가.
- `redis` 로그는 `json-file` 에 `max-size: 10m` / `max-file: "3"` 을 걸어 총 30MB 로 묶었다.
  기본값에는 크기 상한이 없어 무한정 쌓인다.
- 되돌리려면 compose 의 `logging` 블록만 지우고 재배포. 즉시 원복된다.

### `dayogg` 관리 스크립트 (추천)

위 `docker compose -f docker-compose.prod.yml ...` 를 매번 치기 번거로우니 래퍼 스크립트를 씀. 소스는 `scripts/dayogg_script`.

**설치 (서버에서 한 번만):**

> ⚠️ 배포 폴더가 `~/dayogg` 라서, 스크립트를 `~/dayogg` 라는 이름으로 올리면 폴더와 충돌함.
> 반드시 **다른 이름(`dayogg_script`)** 으로 업로드한 뒤, 명령어 이름(`dayogg`)으로 이동시킬 것.

```bash
scp -P <포트> scripts/dayogg_script <user>@<서버IP>:~/dayogg_script   # 배포 폴더와 다른 이름으로 업로드
# 서버에서:
chmod +x ~/dayogg_script
sudo mv ~/dayogg_script /usr/local/bin/dayogg     # 명령어 이름은 dayogg (폴더 ~/dayogg 와 위치가 달라 충돌 없음)
sed -i 's/\r$//' /usr/local/bin/dayogg            # 혹시 CRLF면 정리
```

**사용:**

```bash
dayogg status      # 컨테이너 상태 (인자 없으면 status)
dayogg stats       # CPU/메모리 사용량
dayogg logs        # ⚠ app 은 안 나옴 (CloudWatch 로 조회). dayogg logs redis → Redis 로그
dayogg start       # 정지된 컨테이너 재개
dayogg stop        # 정지 (컨테이너 유지)
dayogg restart     # 재시작
dayogg up          # 정리(clean) 후 새 이미지 pull + 재기동 (수동 배포, 디스크 자동 확보)
dayogg down        # 내리기 (DB 볼륨은 유지)
dayogg clean       # 디스크 정리만 (안 쓰는 이미지/빌드캐시/정지 컨테이너, 볼륨은 유지)
dayogg help        # 도움말
```

- 항상 `~/dayogg`의 `docker-compose.prod.yml` 대상. 경로가 다르면 `DAYOGG_DIR` / `DAYOGG_FILE` 환경변수로 변경.
- 실수로 DB 볼륨 날리는 걸 막으려고 `down -v` 는 스크립트에서 막아둠 (데이터 삭제는 수동으로만).
- `dayogg up` 은 pull **전에** 자동으로 정리하므로 평소엔 `up` 만 쳐도 디스크가 확보됨. `clean` 은 배포 없이 정리만 하고 싶을 때 쓰면 됨. 어느 쪽도 볼륨(DB)은 안 건드림.

### 자주 막히는 것

- **Actions에서 ghcr pull 실패** → 이미지가 private인데 `GHCR_TOKEN` 없음/만료. 토큰 재발급하거나 패키지 public 전환.
- **app이 RDS 연결 실패** → ① `.env`의 `DB_HOST`(RDS 엔드포인트)·`DB_USERNAME`·`DB_PASSWORD` 확인. ② **RDS 보안그룹**이 EC2 보안그룹(또는 프라이빗 IP)에서 3306 인바운드를 허용하는지. ③ RDS가 같은 VPC 프라이빗에 있는지.
- **app 기동 시 스키마 검증 실패**(validate) → RDS에 스키마가 아직 없거나 엔티티와 불일치. 최초 1회는 기존 DB를 `mysqldump` → RDS import 로 옮겨야 함 (아래 D 참고). 스키마가 맞으면 정상 기동.
- **8080 접속 안 됨** → `ufw allow 8080` 했는지, 클라우드면 보안그룹/인바운드 규칙도 확인.
- **운영 스키마 정책** → 운영은 `JPA_DDL_AUTO=validate`(compose.prod)로 앱이 스키마를 건드리지 않음. 로컬/테스트는 기본 `create` 유지(application.yml).

---

## D. MySQL → RDS 이관 (최초 1회)

메모리 절약을 위해 운영 DB는 EC2 컨테이너가 아니라 **RDS(MySQL)** 를 씀. Redis 는 EC2 유지.

1. **RDS 생성** — MySQL 8.x, 같은 VPC 프라이빗. 퍼블릭 액세스는 끔.
2. **RDS 보안그룹** — 인바운드 3306 을 **EC2 의 보안그룹**(소스에 SG 지정)에서 허용.
3. **기존 데이터 이관** (EC2 의 옛 mysql 컨테이너가 아직 있을 때):
   ```bash
   # EC2 에서 기존 컨테이너 DB 덤프
   docker exec -i <mysql컨테이너> mysqldump -u er_db -p eternal_return > dump.sql
   # RDS 로 import (RDS 엔드포인트로)
   mysql -h <RDS엔드포인트> -u er_db -p eternal_return < dump.sql
   ```
   > RDS 에 `eternal_return` 데이터베이스가 없으면 먼저 생성: `mysql -h <RDS> -u <admin> -p -e "CREATE DATABASE eternal_return;"`
4. **`.env` 에 `DB_HOST=<RDS 엔드포인트>` 추가** (`.env.example` 참고).
5. `dayogg up` 으로 재배포 → app 이 RDS 에 붙어 `validate` 통과하면 완료.

> 새로 시작(이관할 데이터 없음)하는 경우엔 스키마가 비어 `validate` 가 실패함.
> 이럴 땐 최초 1회만 `.env` 에 `JPA_DDL_AUTO=update` 를 넣어 스키마를 만든 뒤, 다시 지우고(=validate) 배포.

---

## 참고: 로컬에서 통째로 띄워보기

배포 말고 내 PC에서 테스트할 때는 빌드까지 하는 로컬용 compose 사용.

```bash
cp .env.example .env    # 값 채우고
docker compose up -d --build
```
