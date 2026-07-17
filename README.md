# DAYO.GG

배틀로얄 게임 **Eternal Return**의 전적을 수집하고 통계로 가공해 제공하는 백엔드 서비스입니다.

- **개인 프로젝트** — 백엔드 설계·구현 단독 담당 (프론트엔드는 별도 저장소)
- **현재 상태** — 로컬 실행 단계, 미배포
- Java 21 · Spring Boot 4.0.0 · MySQL · Redis / 자바 파일 약 190개, 8개 도메인 패키지

공식 API에서 플레이어 전적을 수집해 저장하고, 이를 티어·캐릭터별 통계로 집계해 조회 API로 제공합니다. 수집은 수십 초가 걸리는 작업이라 SSE로 결과를 전달합니다.

## 이 프로젝트에서 봐주셨으면 하는 것

| | 주제 | 핵심 판단 |
|---|------|-----------|
| 1 | [동시성 설계](#1-동시성-설계) | 외부 API 10 RPS 제약 아래에서 응답성을 확보하기 위해 가상 스레드 + SSE + 세마포어 + 멱등 처리를 조합 |
| 2 | [통계 집계 설계](#2-통계-집계-설계) | 기간·티어·MMR 조합 조건을 QueryDSL 조건 객체로 분리하고, 병합 로직은 응답 타입이 직접 소유하게 설계 |
| 3 | [인프라 · 공통 모듈 설계](#3-인프라--공통-모듈-설계) | 로깅·분산 락·예외·레이트 리밋을 AOP와 `core` 패키지로 분리해 도메인 코드에서 걷어냄 |

## 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 (Virtual Threads) |
| Framework | Spring Boot 4.0.0 |
| Build | Gradle |
| DB | MySQL 8.0+ (`eternal_return` 스키마) |
| Cache / Lock | Redis, Redisson (분산 락) |
| ORM | Spring Data JPA + QueryDSL 7.0 |
| Mapping | MapStruct |
| Rate Limit | Bucket4j (API / 크롤링 버킷 분리) |
| Crawling | Jsoup |

## 시스템 개요

플레이어 전적 수집 요청이 들어왔을 때의 흐름입니다.

```
Client
  │  GET /player/sse/info?name=...
  ▼
PlayerSseController
  │
  ▼
SseJobDispatcher ──── 멱등 키 검사 ────▶ 이미 진행 중이면 기존 작업의 SSE에 합류하고 종료
  │                   (Redis + 분산 락)
  ├─ ThreadExecutor.submit()  ──▶ 가상 스레드에서 아래 작업 수행
  │                                 │
  └─ SseEmitter 즉시 반환            ├─ BattleResultApiService : next 커서로 페이징하며 공식 API 호출
     (HTTP 커넥션 해제)              │     └─ 매 호출 전 Bucket4j 토큰 소비 (10 RPS)
                                     ├─ 현재 시즌 · 랭크 게임만 필터링
                                     ├─ MySQL 저장 (BattleResult)
                                     ▼
                            작업 완료 → 대기 중인 모든 SSE 구독자에게 결과 push
```

## 1. 동시성 설계

### 배경 — 10 RPS라는 낮은 호출 상한

이터널리턴 공식 API는 **초당 10회(10 RPS)** 라는 낮은 호출 상한을 가집니다. 반면 플레이어 한 명의 전적을 수집하려면 페이징을 돌며 API를 수십 번 호출해야 하므로, 요청 하나가 완료되기까지 수 초에서 수십 초가 걸립니다.

이때 소요 시간의 대부분은 연산이 아니라 **토큰을 기다리는 블로킹**입니다. `ApiService`는 호출 직전마다 Bucket4j 토큰을 소비하고, 토큰이 없으면 다음 리필까지 대기합니다.

```java
// BucketService — 유일한 토큰 소비 지점
bucket.asBlocking().consume(1);   // 토큰이 없으면 다음 리필까지 블로킹
```

즉 이 서비스의 처리량은 CPU나 DB가 아니라 **외부 API의 10 RPS에 고정**됩니다. 스레드는 일하는 대신 대부분 멈춰서 기다립니다. 아래 세 가지는 이 제약을 전제로 한 선택입니다.

### 해결 1 — 가상 스레드로 대기 비용 제거

플랫폼 스레드로 이 구조를 만들면, 대기하는 동안 OS 스레드가 그대로 묶여 낭비됩니다. 가상 스레드는 블로킹 시 캐리어 스레드를 반납하므로, 적은 수의 OS 스레드로 다수의 대기를 감당할 수 있습니다. **대기가 길다는 제약이 오히려 가상 스레드에 유리하게 작용하는 지점**입니다.

```java
// ThreadConfig — 태스크마다 새 가상 스레드 생성
ThreadFactory factory = Thread.ofVirtual().name("vt-worker-", 0).factory();
return Executors.newThreadPerTaskExecutor(factory);
```

### 해결 2 — SSE로 HTTP 요청과 작업을 분리

수십 초짜리 작업을 HTTP 요청에 그대로 물려두면 커넥션이 장시간 점유됩니다. 컨트롤러는 작업을 가상 스레드에 제출한 뒤 **`SseEmitter`를 즉시 반환**하고, 작업이 끝나면 SSE로 결과를 push합니다.

`SseJobDispatcher`는 제출 실패 시 emitter와 멱등 키를 되돌리는 정리 경로까지 포함합니다. emitter가 컨트롤러로 반환되기 전에는 async가 시작되지 않아 타임아웃 콜백이 돌지 않고, 이때 정리하지 않으면 emitter 맵에 영구히 남기 때문입니다.

### 해결 3 — 동시 실행 상한과 중복 제거

가상 스레드는 수십만 개까지 만들 수 있지만, 외부 API와 DB 부하를 고려해 상한을 둡니다.

- **`ThreadLimiter`** — `Semaphore(max-permits)`로 동시 실행 수를 제한합니다. 제한 시간 내에 퍼밋을 얻지 못하면 `ThreadTimeoutException` → `429 Too Many Requests`로 응답합니다.
- **`IdempotentService`** — Redis 기반 멱등 키로, 동일 요청이 이미 진행 중이면 새로 실행하지 않고 해당 작업의 SSE 결과에 합류시킵니다. 중복 호출 자체를 없애 한정된 토큰을 아낍니다.

멱등 처리에서 **조회와 생성은 반드시 하나의 락 구간 안에 있어야 합니다.** 두 메서드로 나누면 그 사이에 락이 풀려, 동시 요청이 모두 "진행 중인 작업 없음"으로 판단하고 각자 멱등 키를 생성(= 서로 덮어쓰기)하면서 중복 실행되고 먼저 등록된 `sseKey`가 유실됩니다. 그래서 `joinOrCreate`라는 단일 메서드로 묶었습니다.

```java
@DistributedLock(value = "#key")
public boolean joinOrCreate(String key, String sseKey) { ... }  // 합류 true / 신규 생성 false
```

### 관련 설정

버킷 용량, 동시 실행 상한, 타임아웃 등의 설정값은 [시작하기 — 애플리케이션 설정](docs/getting-started.md#애플리케이션-설정-applicationyml)에 정리했습니다.

## 2. 통계 집계 설계

통계 조회 조건은 **기간 × 티어 구간 × MMR 구간**의 조합으로 들어옵니다. 조건에 맞는 `BattleResult`를 QueryDSL로 조회한 뒤, 응답 객체에 병합해 캐릭터·티어별 통계를 만듭니다.

```java
// StatisticsAggregationService — QueryDSL로 조건 조회 후 인메모리 병합
battleResultDslRepository.findByRange(condition)
        .forEach(response::merge);
```

설계상 신경 쓴 지점입니다.

- **응답 타입 분기** — 요청 범위가 여러 티어에 걸치면 `StatisticsResponse.Range`, 단일 티어면 `StatisticsResponse.Tier`로 병합 대상을 나눕니다. 병합 로직은 각 타입이 `merge()`로 직접 소유해, 집계 서비스는 조회와 분기만 담당합니다.
- **시즌 전체 집계** — `EnumMap<TierEnum, …>`에 티어별로 모은 뒤, 랭크 티어만 `SeasonTotal`로 다시 합칩니다. 티어 순서가 곧 `TierEnum` 선언 순서라 정렬 비용이 없습니다.
- **조회 조건 객체화** — `BattleResultRangeCondition`과 `StatisticsPredicate`로 QueryDSL 조건을 분리해, 서비스 코드에 쿼리 조건이 흩어지지 않게 했습니다.

티어 커트라인은 성격이 달라 별도로 처리합니다. `TopTierCutScheduler`가 주기적으로 커트라인을 수집·저장하고, 티어 계산 시에는 **게임 플레이 시각 기준**으로 가장 가까운 커트라인을 찾습니다. 같은 MMR이라도 시점에 따라 티어가 달라지기 때문입니다.

## 3. 인프라 · 공통 모듈 설계

도메인 코드가 로깅·락·예외 처리로 오염되지 않도록 `core` 패키지에 모았습니다.

| 모듈 | 역할 | 구현 |
|------|------|------|
| `annotation/distributed_lock` | 분산 락 | Redisson `RLock` + SpEL로 키 추출 → `@DistributedLock("#key")` |
| `annotation/*_logging` | 계층별 로깅 | `@ControllerLogging` / `@ServiceLogging` AOP — 로깅 코드가 비즈니스 로직에 섞이지 않음 |
| `bucket` | 레이트 리밋 | 공식 API용(10회/초)과 크롤링용(1회/2초) 버킷을 분리 — 성격이 다른 트래픽이 서로의 토큰을 잠식하지 않게 함 |
| `sse` | SSE 수명 관리 | emitter 등록·브로드캐스트·정리, 멱등 키 단위 다중 구독자 전송 |
| `thread` | 실행 제어 | 가상 스레드 executor + 세마포어 기반 동시 실행 상한 |
| `idempotent` | 중복 요청 처리 | Redis 저장 + 분산 락으로 원자적 합류/생성 |
| `exception` | 예외 일원화 | `BusinessException` + `@RestControllerAdvice` — 응답 코드와 로그 메시지를 Enum으로 분리 |

예외는 응답용(`ExceptionResponseEnum`)과 로그용(`LogMessageEnum`)을 나눴습니다. 클라이언트에 나갈 메시지와 서버에 남길 메시지의 목적이 다르고, 후자에만 식별자 같은 내부 값이 들어가기 때문입니다.

```java
throw new BusinessException(
        ExceptionResponseEnum.ROUTE_AUTH_DUPLICATE,
        LogMessageEnum.DUPLICATE_ROUTE_AUTH.format(routeId, playerId)
);
```

## API

| Method | Path | 설명 |
|--------|------|------|
| `GET` | `/player/sse/info` | 플레이어 정보 수집 (SSE) |
| `GET` | `/player/sse/refresh` | 플레이어 정보 갱신 (SSE) |
| `GET` | `/player/season` | 플레이어 시즌 상세 조회 |
| `POST` | `/battle/range` | 기간별 전투 결과 조회 |
| `POST` | `/battle/range/page` | 기간별 전투 결과 페이징 조회 |
| `GET` | `/statistics/season` | 시즌 전체 통계 조회 |
| `POST` | `/statistics/range` | 기간별 통계 조회 |
| `GET` | `/tier/range` | 티어 구간 조회 |
| `GET` | `/meta/equip` `/meta/locale` `/meta/season` `/meta/trait` `/meta/tier_range` | 게임 메타데이터 조회 |

## 프로젝트 구조

```
src/main/java/eternal_return/statistics/
├── StatisticsApplication.java   # 앱 진입점
├── battle_result/               # 외부 API 호출 → 전투 결과 저장
├── statistics/                  # 전투 결과 집계 → 통계 산출
├── player/                      # 플레이어 프로필 및 시즌 스냅샷
├── tier/                        # MMR 기반 티어 계산, 커트라인 수집 스케줄러
├── route_auth/                  # 루트 인증 기반 본인 확인 → 데이터 삭제
├── meta/                        # 게임 메타데이터 (시즌·로케일 등)
├── common/                      # 공용 Enum, 로깅 컨텍스트, 유틸
└── core/                        # 인프라 (AOP, SSE, Redis, 스레드, 예외 처리 등)
```

## 문서

- [시작하기](docs/getting-started.md) — 요구 사항, 환경 변수, 실행·빌드, `application.yml` 설정값
- [코드 컨벤션](docs/conventions.md) — 계층 구조, QueryDSL·MapStruct·AOP 로깅, 예외 처리, 버킷 사용 규칙
