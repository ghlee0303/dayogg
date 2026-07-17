package eternal_return.statistics.meta.meta.trait;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TraitInfo(
        String code,
        TraitTypeEnum type,
        @JsonProperty("group_code") String groupCode
) {
    public enum TraitTypeEnum {
        CORE, SUB
    }
}