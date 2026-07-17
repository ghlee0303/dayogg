package eternal_return.statistics.meta.enums;

import eternal_return.statistics.common.enums.LocaleMappable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 이터널리턴의 무기 타입을 나타내는 Enum.
 * <p>
 * 상수명은 도메인 의미를, {@code apiName}은 외부 API의 {@code WeaponType/XXX} 내부 식별자를,
 * {@code code}는 저장 키로 사용하는 무기 코드를 가진다.
 */
@Getter
public enum WeaponEnum implements LocaleMappable, EquipType {
    GLOVE("Glove", 1),
    TONFA("Tonfa", 2),
    BAT("Bat", 3),
    WHIP("Whip", 4),
    THROW("HighAngleFire", 5),
    SHURIKEN("DirectFire", 6),
    BOW("Bow", 7),
    CROSS_BOW("CrossBow", 8),
    PISTOL("Pistol", 9),
    ASSAULT_RIFLE("AssaultRifle", 10),
    SNIPER_RIFLE("SniperRifle", 11),
    HAMMER("Hammer", 13),
    AXE("Axe", 14),
    DAGGER("OneHandSword", 15),
    TWO_HANDED_SWORD("TwoHandSword", 16),
    POLEARM("Polearm", 17),
    DUAL_SWORDS("DualSword", 18),
    SPEAR("Spear", 19),
    NUNCHAKU("Nunchaku", 20),
    RAPIER("Rapier", 21),
    GUITAR("Guitar", 22),
    CAMERA("Camera", 23),
    ARCANA("Arcana", 24),
    VF_ARM("VFArm", 25),
    ;

    private final String apiName;
    private final int code;

    WeaponEnum(String apiName, int code) {
        this.apiName = apiName;
        this.code = code;
    }

    private static final Map<String, WeaponEnum> BY_API_NAME =
            Arrays.stream(values())
                    .collect(Collectors.toMap(WeaponEnum::apiName, weapon -> weapon));

    public static WeaponEnum fromApiName(String apiName) {
        return BY_API_NAME.get(apiName);
    }

    @Override
    public EquipCategory category() {
        return EquipCategory.WEAPON;
    }

    @Override
    public String apiName() {
        return apiName;
    }

    @Override
    public String localeKey() {
        return String.valueOf(code);
    }
}
