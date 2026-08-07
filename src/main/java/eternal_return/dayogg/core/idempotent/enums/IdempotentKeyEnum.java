package eternal_return.dayogg.core.idempotent.enums;

public enum IdempotentKeyEnum {
    SEARCH,
    INFO,
    ;

    public String toKey(String name) {
        return name + "-" + this.name();
    }
}