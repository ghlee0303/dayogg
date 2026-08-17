# DayoGG_Back - Eternal Return 통계 서비스

## 프로젝트 개요

배틀로얄 게임 "Eternal Return"의 플레이어 전투 통계를 수집·처리·제공하는 Spring Boot 백엔드 서비스.

- 외부 게임 API에서 전투 결과를 가져와 MySQL에 저장
- 전투 데이터를 집계하여 캐릭터/티어별 통계 산출
- SSE(Server-Sent Events)로 처리 진행 상황을 클라이언트에 실시간 전달
- dak.gg 라우트 페이지 크롤링으로 외부 라우트 인증 정보 수집

## 1. 코딩 전에 생각하기

**추측하지 마세요. 혼란을 숨기지 마세요. 트레이드오프를 드러내세요.**

구현 전에:
- 가정을 명시적으로 밝히세요. 불확실하면 질문하세요.
- 여러 해석이 가능하면 제시하세요 - 임의로 선택하지 마세요.
- 더 간단한 방법이 있다면 말하세요. 필요할 때는 반론을 제기하세요.
- 불명확한 부분이 있으면 멈추세요. 무엇이 혼란스러운지 짚고 질문하세요.

## 2. 단순함 우선

**문제를 해결하는 최소한의 코드. 추측성 코드는 금지.**

- 요청받은 것 이상의 기능을 추가하지 마세요.
- 한 번만 쓰이는 코드에 추상화를 적용하지 마세요.
- 요청되지 않은 "유연성"이나 "설정 가능성"을 넣지 마세요.
- 발생할 수 없는 시나리오에 대한 에러 처리를 하지 마세요.
- 200줄을 작성했는데 50줄로 가능하다면, 다시 작성하세요.

스스로에게 물어보세요: "시니어 엔지니어가 이걸 보고 과하게 복잡하다고 할까?" 그렇다면, 단순화하세요.

## 3. 외과적 수정

**반드시 필요한 부분만 수정하세요. 자신이 만든 문제만 정리하세요.**

기존 코드를 수정할 때:
- 인접한 코드, 주석, 포맷팅을 "개선"하지 마세요.
- 고장나지 않은 것을 리팩토링하지 마세요.
- 본인이라면 다르게 했을지라도 기존 스타일을 따르세요.
- 관련 없는 죽은 코드를 발견하면 언급만 하고 삭제하지 마세요.

본인의 변경으로 고아가 된 코드가 있을 때:
- 본인의 변경으로 사용되지 않게 된 import/변수/함수는 제거하세요.
- 요청받지 않은 한 기존에 있던 죽은 코드는 제거하지 마세요.

검증 기준: 변경된 모든 줄은 사용자의 요청에 직접적으로 연결되어야 합니다.

## 4. 목표 중심 실행

**성공 기준을 정의하세요. 검증될 때까지 반복하세요.**

작업을 검증 가능한 목표로 변환하세요:
- "유효성 검사 추가" → "잘못된 입력에 대한 테스트를 작성하고, 통과시키기"
- "버그 수정" → "재현하는 테스트를 작성하고, 통과시키기"
- "X 리팩토링" → "리팩토링 전후로 테스트가 통과하는지 확인"

다단계 작업의 경우, 간략한 계획을 명시하세요:
```
1. [단계] → 검증: [확인 사항]
2. [단계] → 검증: [확인 사항]
3. [단계] → 검증: [확인 사항]
```

## 코드 컨벤션

- 계층 구조: Controller → Service → Repository
- DTO 변환: MapStruct 사용
- **Repository select 쿼리는 전부 QueryDSL로 작성** (JPQL/네이티브 금지, 단순 단건 조회·저장은 Spring Data JPA 메서드 OK)
- 동적 쿼리: QueryDSL 사용
- 공용 로깅: AOP 기반 (`@ControllerLogging`, `@ServiceLogging`)
- 멱등성 보장: `IdempotentService` (Redis 기반)
- Rate Limit: 외부 API는 `BucketService.apiBucketBlocking()`, 크롤링은 `BucketService.crawlingBucketBlocking()` 호출 후 요청
- 예외: `BusinessException(ExceptionResponseEnum, LogMessageEnum.format(...))` 패턴 통일
- 접미사 명명 규칙: List 사용시 ~List, Map 사용시 ~Map

## 커밋 메시지 규칙

- 형식: `type: 한국어 제목` (type 은 `feat` / `fix` / `refactor` / `chore` / `test` / `docs`)
- 제목은 **명사형 종결**로 끝낸다: `~수정`, `~대응`, `~변경`, `~추가`, `~삭제`, `~분리`
- **대화형·서술형 종결은 금지**: `~손본다`, `~바꾼다`, `~더한다`, `~고친다`, `~했습니다`
- 본문에 항목을 나열할 때도 같은 종결 규칙을 따른다.

## 문서 규칙

- **작업 예정 계획서**(아직 구현되지 않은 설계·계획 문서)는 `claude/docs/`에 작성한다.
  작성 규칙과 템플릿은 [CLAUDE_PLAN.md](claude/CLAUDE_PLAN.md)를 따른다.
- **구현 완료된 시스템 설명서**(현재 동작을 기술하는 문서)는 프로젝트 루트 `docs/`에 둔다.
- 계획서의 구현이 끝나면 내용을 정리해 `docs/`로 옮기는 것을 검토한다.
- 그 외 문서 종류별 작성 규칙: [CLAUDE-REPORT.md](claude/CLAUDE-REPORT.md)(`claude/report/`) ·
  [CLAUDE-CODE_REVIEW.md](claude/CLAUDE-CODE_REVIEW.md)(`claude/code_review/`)

## 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 (Virtual Threads 활성화) |
| Framework | Spring Boot 4.0.0 |
| Build | Gradle |
| DB | MySQL 8.0+ (`eternal_return` 스키마) |
| Cache | Redis (localhost:6379) |
| ORM | Spring Data JPA + QueryDSL 7.0 (openfeign) |
| Mapping | MapStruct 1.5.5 |
| JSON | Jackson 3 (`tools.jackson.*`) |
| HTML Parsing | Jsoup 1.18.3 |
| Rate Limit | Bucket4j 8.8.0 (API 버킷 / 크롤링 버킷 분리) |
| Distributed Lock | Redisson 4.3.0 (Redis 기반) |

## 프로젝트 구조

```
CLAUDE.md                             # 프로젝트 규칙 (자동 로딩)

claude/                               # 클로드 파일
├── docs/                             # 작업 예정 계획서 (미구현 설계·계획 문서)
└── result/                           # 클로드가 생성한 파일

src/main/java/eternal_return/dayogg/
├── DayoGGBackApplication.java        # 앱 진입점 (@EnableScheduling)
├── battle_result/                    # 외부 API 호출 → 전투 결과 저장
├── statistics/                       # 전투 결과 집계 → 통계 산출
│   ├── condition/ predicate/ extend/ map_key/
│   ├── dto/ enums/ repository/ service/
│   └── StatisticsController.java
├── player/                           # 플레이어 프로필 관리
│   ├── client/                       # 외부 사용자 API 클라이언트
│   ├── controller/ dto/ repository/ service/
│   └── player_season/                # 시즌별 플레이어 스냅샷
├── tier/                             # MMR 기반 티어 계산
│   ├── dto/ enums/
│   ├── top_tier_cut/                 # 최상위 티어 컷 관리
│   └── TierService.java
├── meta/                             # 게임 메타데이터 (시즌·로케일 등)
│   ├── MetaController.java
│   ├── dto/ enums/ json/ meta/ service/
├── route_auth/                       # dak.gg 라우트 크롤링 기반 인증
│   ├── RouteAuth.java
│   ├── client/ enums/ exception/ repository/ service/
├── common/                           # 공용 Enum, 로깅 컨텍스트, 유틸
│   ├── enums/
│   ├── log/                          # LogContext (MDC)
│   └── utils/                        # 공용 유틸 (커스텀 역직렬화 포함)
└── core/                             # 인프라 컴포넌트
    ├── annotation/                   # AOP 어노테이션 및 Aspect
    │   ├── SpelFacade.java           # SpEL 평가 헬퍼
    │   ├── controller_logging/       # @ControllerLogging
    │   ├── service_logging/          # @ServiceLogging
    │   └── distributed_lock/         # @DistributedLock (Redisson 기반)
    ├── api/                          # 외부 API/크롤링 클라이언트 (ApiService, CrawlingService)
    ├── bucket/                       # Bucket4j Rate Limiter (API/크롤링 버킷 분리)
    ├── config/                       # GlobalMetaConfig, QueryDslConfig, WebConfig
    ├── exception/                    # GlobalExceptionHandler, BusinessException
    ├── file/                         # JsonFileService (메타 JSON 로딩)
    ├── idempotent/                   # Redis 기반 멱등성 처리
    ├── redis/                        # Redis 캐시 설정 및 저장소
    ├── sse/                          # SSE 비동기 잡 실행기
    └── thread/                       # ThreadConfig, ThreadExecutor, ThreadLimiter

src/main/resources/
├── application.yml                   # DB/Redis/Scheduler 설정
├── json/                             # 정적 JSON 리소스
├── static/                           # 정적 웹 리소스
├── templates/                        # 뷰 템플릿
└── meta/                             # 게임 메타데이터 JSON
    ├── tier/                         # 티어 관련 메타
    │   ├── tier_range.json
    │   └── top_tier_cut.json
    └── locale/                       # 로케일별 정적 메타
        ├── season.json
        └── tier.json
```
