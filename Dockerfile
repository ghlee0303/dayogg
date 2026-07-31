# syntax=docker/dockerfile:1

### 1) Build stage ─ Gradle 래퍼로 부트 JAR 생성
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 래퍼/빌드 스크립트 먼저 복사 (의존성 캐시 레이어 분리)
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --version

# 의존성 미리 내려받아 캐시 (소스 변경 시 재다운로드 방지)
RUN ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true

# 소스 복사 후 빌드 (테스트는 CI에서 돌린다고 가정하고 제외)
COPY src ./src
RUN ./gradlew clean bootJar --no-daemon -x test

### 2) Runtime stage ─ JRE만 담은 경량 이미지
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# 비루트 유저로 실행
RUN useradd --system --uid 10001 --create-home appuser

COPY --from=build /workspace/build/libs/*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser
EXPOSE 8080

# 컨테이너 메모리에 맞춰 힙 자동 조정
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
