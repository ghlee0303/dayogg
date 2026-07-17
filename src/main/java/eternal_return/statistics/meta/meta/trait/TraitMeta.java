package eternal_return.statistics.meta.meta.trait;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class TraitMeta {

    private final List<TraitInfo> traitList;
    private final Map<String, TraitInfo> codeMap;

    public TraitMeta(List<TraitInfo> traitList) {
        this.traitList = traitList;
        this.codeMap = indexByCode(traitList);
    }

    private static Map<String, TraitInfo> indexByCode(List<TraitInfo> traitList) {
        Map<String, TraitInfo> result = new HashMap<>();
        for (TraitInfo info : traitList) {
            result.put(info.code(), info);
        }
        return result;
    }

    public TraitInfo getByCode(String code) {
        return codeMap.get(code);
    }
}