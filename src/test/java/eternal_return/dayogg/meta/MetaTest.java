package eternal_return.dayogg.meta;

import eternal_return.dayogg.meta.meta.LocaleMeta;
import eternal_return.dayogg.meta.meta.phase.PhaseDuration;
import eternal_return.dayogg.meta.meta.phase.PhaseMeta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class MetaTest {
    @Autowired
    LocaleMeta localeMeta;
    @Autowired
    PhaseMeta phaseMeta;

    @Test
    void phaseMetaTest() {
        LocalDateTime ldt1 = LocalDateTime.of(
                2026, 5, 10, 10, 30
        );
        PhaseDuration p1 = phaseMeta.resolve(ldt1);

        System.out.println(p1.durations());


        LocalDateTime ldt2 = LocalDateTime.of(
                2026, 7, 10, 10, 30
        );
        PhaseDuration p2 = phaseMeta.resolve(ldt2);

        System.out.println(p2.durations());
    }
}
