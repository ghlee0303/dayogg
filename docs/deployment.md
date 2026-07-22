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
docker compose -f docker-compose.prod.yml logs -f app # 앱 로그 실시간
docker compose -f docker-compose.prod.yml logs mysql  # DB 로그

# 수동으로 다시 받아 재기동
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d

# 전체 재시작 / 내리기
docker compose -f docker-compose.prod.yml restart
docker compose -f docker-compose.prod.yml down        # 볼륨은 유지됨
```

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
dayogg logs        # 앱 로그 실시간 (dayogg logs mysql → DB 로그)
dayogg start       # 정지된 컨테이너 재개
dayogg stop        # 정지 (컨테이너 유지)
dayogg restart     # 재시작
dayogg up          # 새 이미지 pull + 재기동 (수동 배포)
dayogg down        # 내리기 (DB 볼륨은 유지)
dayogg clean       # 디스크 정리 (안 쓰는 이미지/빌드캐시/정지 컨테이너, 볼륨은 유지)
dayogg help        # 도움말
```

- 항상 `~/dayogg`의 `docker-compose.prod.yml` 대상. 경로가 다르면 `DAYOGG_DIR` / `DAYOGG_FILE` 환경변수로 변경.
- 실수로 DB 볼륨 날리는 걸 막으려고 `down -v` 는 스크립트에서 막아둠 (데이터 삭제는 수동으로만).
- **디스크 꽉 차서 배포 실패**(`no space left on device`) 시: `dayogg clean` 으로 옛 이미지·빌드캐시 정리 후 `dayogg up` 재시도. 볼륨(DB)은 안 건드리므로 안전.

### 자주 막히는 것

- **Actions에서 ghcr pull 실패** → 이미지가 private인데 `GHCR_TOKEN` 없음/만료. 토큰 재발급하거나 패키지 public 전환.
- **app이 mysql 연결 실패** → `.env`의 `DB_PASSWORD`가 mysql 컨테이너와 app에 같은 값으로 들어갔는지 확인. 비번 바꿨으면 기존 `mysql-data` 볼륨엔 옛 비번이 박혀 있음 → `docker compose ... down -v`로 볼륨 지우고 다시 (⚠️ 데이터 날아감).
- **8080 접속 안 됨** → `ufw allow 8080` 했는지, 클라우드면 보안그룹/인바운드 규칙도 확인.
- **`ddl-auto: create`** → 앱 뜰 때마다 스키마 새로 만듦. 운영에서 데이터 유지하려면 `validate`/`none`으로 바꿔야 함 (application.yml).

---

## 참고: 로컬에서 통째로 띄워보기

배포 말고 내 PC에서 테스트할 때는 빌드까지 하는 로컬용 compose 사용.

```bash
cp .env.example .env    # 값 채우고
docker compose up -d --build
```
