package eternal_return.dayogg.tier.top_tier_cut;

import eternal_return.dayogg.meta.meta.top_tier_cut.TopTierCutInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class TopTierCut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer demiGodCut;   // DEMIGOD 티어 최소 RP (상위 1000위 기준)
    private Integer eternityCut;  // ETERNITY 티어 최소 RP (상위 300위 기준)
    private LocalDateTime dateTime; // 수집 시각

    /**
     * API에서 수집한 커트라인 값으로 인스턴스를 생성한다.
     * 수집 시각은 현재 시각으로 자동 설정된다.
     */
    public TopTierCut(Integer demiGodCut, Integer eternityCut) {
        this.demiGodCut = demiGodCut;
        this.eternityCut = eternityCut;
        this.dateTime = LocalDateTime.now();
    }

    public TopTierCut(TopTierCutInfo info) {
        this.demiGodCut = info.demiGodCut();
        this.eternityCut = info.eternityCut();
        this.dateTime = null;
    }
}