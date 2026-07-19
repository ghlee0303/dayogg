# 시작하기

[← README로 돌아가기](../README.md)

## 요구 사항

- JDK 21
- MySQL 8.0+ (`eternal_return` 스키마 생성 필요)
- Redis (기본 `localhost:6379`)

## 환경 변수

민감한 설정값은 환경 변수로 주입합니다. `DB_PASSWORD`와 `GAME_API_KEY`는 기본값이 없으며, 설정하지 않으면 애플리케이션이 기동되지 않습니다.

| 변수 | 필수 | 기본값 | 설명 |
|------|:----:|--------|------|
| `DB_PASSWORD` | ✅ | – | MySQL 비밀번호 |
| `GAME_API_KEY` | ✅ | – | Eternal Return Open API 키 ([발급처](https://developer.bser.io)) |
| `DB_URL` | | `jdbc:mysql://127.0.0.1:3306/eternal_return?...` | JDBC 접속 URL |
| `DB_USERNAME` | | `er_db` | MySQL 사용자명 |
| `REDIS_HOST` | | `localhost` | Redis 호스트 |
| `REDIS_PORT` | | `6379` | Redis 포트 |
| `GAME_API_URL` | | `https://open-api.bser.io` | 게임 API 베이스 URL |

### 주입 방법

```bash
# macOS / Linux
export DB_PASSWORD=your_password
export GAME_API_KEY=your_api_key
```

```powershell
# Windows (PowerShell)
$env:DB_PASSWORD = "your_password"
$env:GAME_API_KEY = "your_api_key"
```

IntelliJ에서 실행할 경우 `Run/Debug Configurations → Environment variables`에 동일하게 등록하면 됩니다.

## 실행

```bash
./gradlew bootRun
```

## 빌드 / 테스트

```bash
./gradlew build
./gradlew test
```

## 애플리케이션 설정 (`application.yml`)

환경 변수가 아닌 고정 설정값입니다. 대부분 [동시성 설계](concurrency.md)와 직접 연관됩니다.

| 설정 | 값 | 설명 |
|------|----|------|
| `api.bucket.limits` | `10` | API 버킷 — capacity 10 / 1초당 10개 리필 (= 10 RPS) |
| `thread.max-permits` | `10` | 동시 실행 가상 스레드 상한 |
| `thread.timeout-seconds` | `60` | 퍼밋 획득 대기 한도 — 초과 시 429 |
| `sse.timeout-minutes` | `5` | SSE 연결 타임아웃 |
| `spring.threads.virtual.enabled` | `true` | 가상 스레드 활성화 |
| `cache.timeout-minutes` | `30` | 캐시 만료 시간 |
| `scheduler.tier-cut.delay-minutes` | `10` | 티어 컷 스케줄러 실행 주기 |
| `path.error-log` | `log/json/` | 에러 로그(JSON) 저장 경로 |

> `spring.jpa.hibernate.ddl-auto`는 현재 `create`로, 기동할 때마다 스키마를 새로 만듭니다. 데이터를 유지해야 하는 환경에서는 `validate` 또는 `none`으로 바꿔야 합니다.