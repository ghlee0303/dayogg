package eternal_return.dayogg.core.annotation.controller_logging;

/**
 * {@link ControllerLogging} 이 정상 요청을 어느 레벨로 남길지 결정한다.
 *
 * <p>문제 판정(예외·4xx/5xx·지연)과 그 레벨은 두 모드가 공유한다 — 모드가 바꾸는 것은
 * <b>아무 문제 없이 끝난 요청</b>의 레벨뿐이다.
 */
public enum LoggingMode {
    /** 정상 요청도 INFO 로 남긴다. */
    ALWAYS,
    /** 정상 요청은 DEBUG 로 내린다. 운영 레벨(info)에서는 나가지 않아 수집 비용이 0 이다. */
    ON_ERROR,
}
