package eternal_return.statistics.common.utils.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * {@code List<Integer>} ↔ JSON 문자열 변환.
 *
 * <p>{@link tools.jackson.databind.ObjectMapper}로 직렬화하여 단일 컬럼에 저장한다.</p>
 */
@Converter
public class IntegerListConverter implements AttributeConverter<List<Integer>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Integer>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        if (attribute == null) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return OBJECT_MAPPER.readValue(dbData, TYPE);
    }
}
