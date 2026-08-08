# 코드 컨벤션

[← README로 돌아가기](../README.md)

## 계층 구조

Controller → Service → Repository 순으로만 의존합니다. 역방향 의존이나 계층 건너뛰기는 하지 않습니다.

## 영속성

- Repository의 select 쿼리는 전부 QueryDSL로 작성합니다.
- 단순 단건 조회·저장은 Spring Data JPA 메서드를 사용해도 됩니다.

## DTO 변환

MapStruct를 사용합니다. 수동 매핑 코드는 작성하지 않습니다.

## 로깅

AOP 기반으로 처리합니다. 메서드에 어노테이션만 붙이고 로깅 코드를 직접 넣지 않습니다.

- `@ControllerLogging`
- `@ServiceLogging`

정상 경로에 남길 정보가 없는 엔드포인트(인메모리 메타 조회 등)는 `mode = LoggingMode.ON_ERROR` 를 줍니다.
평상시엔 아무것도 남기지 않고 예외·4xx/5xx·지연일 때만 올라옵니다 — [로그 레벨 표](logging.md#컨트롤러-로그-레벨).

```java
@ControllerLogging(mode = LoggingMode.ON_ERROR)
```

## 예외 처리

`BusinessException`으로 통일합니다.

```java
throw new BusinessException(ExceptionResponseEnum.XXX, LogMessageEnum.YYY.format(...));
```

## 외부 호출

호출 직전에 반드시 해당 버킷의 토큰을 소비합니다. 버킷을 거치지 않는 외부 호출은 10 RPS 상한을 깨뜨립니다.

- 외부 API 호출 전 — `BucketService.apiBucketBlocking()`
- 크롤링 전 — `BucketService.crawlingBucketBlocking()`