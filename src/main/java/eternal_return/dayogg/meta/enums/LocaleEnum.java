package eternal_return.dayogg.meta.enums;

import eternal_return.dayogg.common.enums.LocaleMappable;
import eternal_return.dayogg.tier.enums.TierEnum;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum LocaleEnum {
    SKIN("Skin/Name/"),
    CHARACTER("Character/Name/"),
    WEAPON("WeaponType/", WeaponEnum.class),
    TIER(TierEnum.class),
    SEASON("meta/locale/season.json", true)
    ;

    private final String path;
    private final boolean isStatic;
    private Class<? extends LocaleMappable> otherEnum;

    LocaleEnum(String path) {
        this.path = path;
        this.isStatic = false;
    }

    LocaleEnum(String path, boolean isStatic) {
        this.path = path;
        this.isStatic = isStatic;
    }

    LocaleEnum(Class<? extends LocaleMappable> otherEnum) {
        this.path = null;
        this.isStatic = false;
        this.otherEnum = otherEnum;
    }

    LocaleEnum(String path, Class<? extends LocaleMappable> otherEnum) {
        this.path = path;
        this.isStatic = false;
        this.otherEnum = otherEnum;
    }

    public static LocaleEnum getByPrefix(String prefix) {
        for (LocaleEnum type : LocaleEnum.values()) {
            if (Objects.equals(type.path, prefix)) {
                return type;
            }
        }

        return null;
    }

    public static LocaleEnum getByEnumConstant(String name) {
        for (LocaleEnum type : LocaleEnum.values()) {
            if (type.otherEnum == null) continue;

            for (LocaleMappable constant : type.otherEnum.getEnumConstants()) {
                if (constant.apiName().equals(name)) {
                    return type;
                }
            }
        }

        return null;
    }

    /**
     * API 토큰을 저장 키로 변환한다.
     * <p>
     * {@code otherEnum}이 없으면 토큰을 그대로 반환하고, 있으면 매칭되는 상수의
     * {@link LocaleMappable#localeKey()}를 반환한다. 매칭되는 상수가 없으면 {@code null}.
     */
    public String resolveKey(String token) {
        if (otherEnum == null) {
            return token;
        }

        for (LocaleMappable constant : otherEnum.getEnumConstants()) {
            if (constant.apiName().equals(token)) {
                return constant.localeKey();
            }
        }

        return null;
    }
}
