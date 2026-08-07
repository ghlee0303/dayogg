package eternal_return.dayogg;

import eternal_return.dayogg.meta.meta.season.SeasonMeta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SimpleTest {
    @Autowired
    SeasonMeta seasonMeta;

    @Test
    void test1() {
        System.out.println("?????");
    }
}
