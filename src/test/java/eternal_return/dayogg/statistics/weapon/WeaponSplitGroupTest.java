package eternal_return.dayogg.statistics.weapon;

import eternal_return.dayogg.statistics.dto.response.CharacterStatisticsResponse;
import eternal_return.dayogg.statistics.dto.response.CharacterStatisticsResponse.WeaponResponse;
import eternal_return.dayogg.statistics.extend.AverageStatistics;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 착용 무기 아이템 코드가 등록된 무기군이면 {@code weaponNum}이 같아도 대표 코드별로 분리,
 * 아니면 기존대로 {@code weaponNum}만으로 합산되는지 검증.
 *
 * <p>{@link WeaponGroupEnum}의 placeholder 코드({@code -1xx})를 실제 코드로 교체하면 기대값도 같이 수정한다.</p>
 */
class WeaponSplitGroupTest {

    private static final int WEAPON_NUM = 4;

    private static final Integer GROUP_1_REP = WeaponGroupEnum.BLACK_MAMBA.representativeCode();
    private static final Integer GROUP_1_OTHER_GRADE = WeaponGroupEnum.BLACK_MAMBA.itemCodeList().get(2);
    private static final Integer GROUP_2_REP = WeaponGroupEnum.SIDEWINDER.representativeCode();

    @Test
    void representativeOf_등록되지_않은_코드면_null() {
        assertNull(WeaponSplitGroup.representativeOf(null));
        assertNull(WeaponSplitGroup.representativeOf(999999));
    }

    @Test
    void representativeOf_그룹멤버는_대표코드로_멱등() {
        assertEquals(GROUP_1_REP, WeaponSplitGroup.representativeOf(GROUP_1_OTHER_GRADE));
        assertEquals(GROUP_1_REP, WeaponSplitGroup.representativeOf(GROUP_1_REP));
    }

    @Test
    void 서로_다른_무기군은_분리되고_합산_행도_함께_생긴다() {
        CharacterStatisticsResponse character = new CharacterStatisticsResponse(1);

        character.merge(WEAPON_NUM, GROUP_1_REP, stat());
        character.merge(WEAPON_NUM, GROUP_2_REP, stat());

        assertEquals(3, character.getWeaponList().size()); // 합산 1 + 분리 2
    }

    @Test
    void 같은_무기군의_세부아이템_차이는_대표코드로_합산된다() {
        CharacterStatisticsResponse character = new CharacterStatisticsResponse(1);

        character.merge(WEAPON_NUM, GROUP_1_REP, stat());
        character.merge(WEAPON_NUM, GROUP_1_OTHER_GRADE, stat());

        assertEquals(2, character.getWeaponList().size()); // 합산 1 + 분리 1
        WeaponResponse split = weaponOf(character, GROUP_1_REP);
        assertEquals(2, split.getTotalGames());
        assertEquals(2, weaponOf(character, null).getTotalGames());
    }

    @Test
    void 에키온_3종은_분리3_합산1_로_4개() {
        CharacterStatisticsResponse character = new CharacterStatisticsResponse(1);

        for (WeaponGroupEnum group : WeaponGroupEnum.values()) {
            character.merge(WEAPON_NUM, group.representativeCode(), stat());
        }

        assertEquals(4, character.getWeaponList().size());
        assertEquals(3, weaponOf(character, null).getTotalGames());
    }

    @Test
    void 롤업_시_합산행이_이중계상되지_않는다() {
        CharacterStatisticsResponse child = new CharacterStatisticsResponse(1);
        for (WeaponGroupEnum group : WeaponGroupEnum.values()) {
            child.merge(WEAPON_NUM, group.representativeCode(), stat());
        }

        CharacterStatisticsResponse parent = new CharacterStatisticsResponse(1);
        parent.merge(child);

        assertEquals(4, parent.getWeaponList().size());
        assertEquals(3, weaponOf(parent, null).getTotalGames());
        assertEquals(3, parent.getTotalGames());
    }

    @Test
    void 등록되지_않은_무기는_weaponNum만으로_합산되고_equipWeaponCode는_null() {
        CharacterStatisticsResponse character = new CharacterStatisticsResponse(1);

        character.merge(WEAPON_NUM, null, stat());
        character.merge(WEAPON_NUM, 12345, stat());

        assertEquals(1, character.getWeaponList().size());
        assertNull(character.getWeaponList().getFirst().getEquipWeaponCode());
    }

    private static WeaponResponse weaponOf(CharacterStatisticsResponse character, Integer equipWeaponCode) {
        return character.getWeaponList().stream()
                .filter(weapon -> Objects.equals(weapon.getEquipWeaponCode(), equipWeaponCode))
                .findFirst()
                .orElseThrow();
    }

    private static AverageStatistics stat() {
        AverageStatistics stat = new AverageStatistics();
        stat.setTotalGames(1);
        return stat;
    }
}
