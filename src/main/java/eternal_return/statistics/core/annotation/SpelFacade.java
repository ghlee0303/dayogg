package eternal_return.statistics.core.annotation;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SpelFacade {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * <p>Spring Expression Language 파서
     * - 어노테이션 대상 함수의 매개변수를 가져올 때 사용함
     *
     * <p> ex) value = "#key"
     *
     * @param joinPoint
     * @param valueExtractor  어노테이션 interface에 받으려는 값을 반환하는 람다식
     * @param returnType
     * @return
     * @param <T>
     */
    // ex) value = "#key"
    public <T> T getSingleValue(ProceedingJoinPoint joinPoint, Supplier<String> valueExtractor, Class<T> returnType) {
        EvaluationContext context = buildContext(joinPoint);
        return parser.parseExpression(valueExtractor.get()).getValue(context, returnType);
    }

    // ex) value = "{#key, #id}"
    @SuppressWarnings("unchecked")
    public List<Object> getListValue(ProceedingJoinPoint joinPoint, Supplier<String> valueExtractor) {
        EvaluationContext context = buildContext(joinPoint);
        return (List<Object>) parser.parseExpression(valueExtractor.get()).getValue(context, List.class);
    }

    // ex) values = {"#playerId", "#seasonId"} → {"playerId": 123, "seasonId": 1}
    public Map<String, Object> getMapValue(ProceedingJoinPoint joinPoint, Supplier<String[]> valuesExtractor) {
        EvaluationContext context = buildContext(joinPoint);
        Map<String, Object> result = new LinkedHashMap<>();
        for (String expression : valuesExtractor.get()) {
            String key = expression.replace("#", "");
            result.put(key, parser.parseExpression(expression).getValue(context));
        }
        return result;
    }

    // 어노테이션 지정 없이 메서드의 모든 파라미터를 자동으로 수집
    public Map<String, Object> getAllParams(ProceedingJoinPoint joinPoint) {
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            result.put(paramNames[i], args[i]);
        }
        return result;
    }

    private EvaluationContext buildContext(ProceedingJoinPoint joinPoint) {
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return context;
    }
}
