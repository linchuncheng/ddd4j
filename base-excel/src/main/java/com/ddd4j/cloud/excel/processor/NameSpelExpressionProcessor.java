package com.ddd4j.cloud.excel.processor;

import com.ddd4j.cloud.excel.utils.StrPoolUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * el表达式解析器
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:40
 */
public class NameSpelExpressionProcessor implements NameProcessor {
    // 参数发现器
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    // Express语法解析器
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Override
    public String doDetermineName(Object[] args, Method method, String key) {

        if (!key.contains(StrPoolUtils.HASH)) {
            return key;
        }

        EvaluationContext context = new MethodBasedEvaluationContext(null, method, args, NAME_DISCOVERER);
        final Object value = PARSER.parseExpression(key).getValue(context);
        return value == null ? null : value.toString();
    }

}
