package eternal_return.statistics.common.enums;

/**
 * 외부 API의 l10n 라인을 도메인 enum 으로 매핑하기 위한 공통 계약.
 * <p>
 * {@link #apiName()}으로 API 라인의 토큰과 매칭하고, {@link #localeKey()}로 저장할 키를 결정한다.
 */
public interface LocaleMappable {

    /**
     * API 라인에서 매칭할 토큰. (예: WeaponType/HighAngleFire 의 "HighAngleFire")
     */
    String apiName();

    /**
     * locale map 에 저장할 키. (예: 무기는 코드, 티어는 enum 이름)
     */
    String localeKey();
}
