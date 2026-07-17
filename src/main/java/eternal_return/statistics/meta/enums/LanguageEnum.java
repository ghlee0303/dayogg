package eternal_return.statistics.meta.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageEnum {
    KO("Korean"),
    EN("English")
    ;

    private final String name;

}
