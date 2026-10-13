package com.ddd4j.cloud.data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Query 条件翻译器：把查询对象上带后缀的字段翻译成 MyBatis-Plus 的 QueryWrapper。
 * <p>
 * 后缀约定（字段名 = 列名驼峰 + 后缀，均为大小写敏感）：
 * <pre>
 * （无后缀）  : 精确匹配 =
 * In          : IN，值可以是集合、数组或英文逗号分隔字符串
 * NotIn       : NOT IN，取值同 In
 * Like        : LIKE '%值%'
 * LikeLeft    : LIKE '值%'，前缀匹配
 * LikeRight   : LIKE '%值'，后缀匹配
 * Not         : !=
 * Gt / Lt     : > / <
 * Ge / Le     : >= / <=
 * Start / End : >= / <=，语义化别名，用于时间范围
 * IsNull      : true → IS NULL，false → IS NOT NULL
 * </pre>
 * <p>
 * 翻译过程是纯显式的：读字段 → 匹配后缀 → 调用 Wrapper API，
 * 没有扫描、没有缓存字节码，行为可直接单测。
 *
 * @author Jensen
 */
public final class QueryTranslator {

    // 后缀匹配优先级即数组顺序：长后缀在前，避免 NotIn 被 In 截胡
    private static final String[] SUFFIXES = {
            "NotIn", "LikeLeft", "LikeRight", "IsNull", "Start", "End", "Not", "In", "Like", "Gt", "Lt", "Ge", "Le"
    };

    private QueryTranslator() {
    }

    public static <M> QueryWrapper<M> translate(Query query) {
        QueryWrapper<M> wrapper = new QueryWrapper<>();
        apply(query, wrapper);
        return wrapper;
    }

    public static <M> void apply(Query query, QueryWrapper<M> wrapper) {
        for (Field field : collectFields(query.getClass())) {
            Object value = valueOf(field, query);
            if (isEmpty(value)) {
                continue;
            }
            String name = field.getName();
            String suffix = matchSuffix(name);
            String column = toColumn(name.substring(0, name.length() - suffix.length()));
            applyCondition(wrapper, column, suffix, value);
        }
        applyOrderBys(query, wrapper);
    }

    private static <M> void applyCondition(QueryWrapper<M> wrapper, String column, String suffix, Object value) {
        switch (suffix) {
            case "" -> wrapper.eq(column, value);
            case "In" -> wrapper.in(column, toValues(value));
            case "NotIn" -> wrapper.notIn(column, toValues(value));
            case "Like" -> wrapper.like(column, value);
            case "LikeLeft" -> wrapper.likeRight(column, value);
            case "LikeRight" -> wrapper.likeLeft(column, value);
            case "Not" -> wrapper.ne(column, value);
            case "Gt" -> wrapper.gt(column, value);
            case "Lt" -> wrapper.lt(column, value);
            case "Ge" -> wrapper.ge(column, value);
            case "Le" -> wrapper.le(column, value);
            case "Start" -> wrapper.ge(column, value);
            case "End" -> wrapper.le(column, value);
            case "IsNull" -> {
                if (Boolean.TRUE.equals(value)) {
                    wrapper.isNull(column);
                } else {
                    wrapper.isNotNull(column);
                }
            }
            default -> throw new IllegalArgumentException("不支持的查询后缀: " + suffix);
        }
    }

    private static void applyOrderBys(Query query, QueryWrapper<?> wrapper) {
        String orderBys = query.getOrderBys();
        if (orderBys == null || orderBys.isBlank()) {
            return;
        }
        for (String part : orderBys.split(",")) {
            String item = part.trim();
            if (item.isEmpty()) {
                continue;
            }
            String[] segments = item.split("_");
            String field = segments[0].trim();
            boolean desc = segments.length > 1 && "DESC".equalsIgnoreCase(segments[segments.length - 1].trim());
            String column = toColumn(field);
            if (desc) {
                wrapper.orderByDesc(column);
            } else {
                wrapper.orderByAsc(column);
            }
        }
    }

    // 按优先级匹配后缀，无后缀返回 "" 表示精确匹配
    private static String matchSuffix(String fieldName) {
        for (String suffix : SUFFIXES) {
            if (fieldName.length() > suffix.length() && fieldName.endsWith(suffix)) {
                return suffix;
            }
        }
        return "";
    }

    // 驼峰转下划线，同时校验合法性（orderBys 来自请求参数，必须防注入）
    private static String toColumn(String name) {
        String column = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        if (!column.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("非法的查询字段: " + name);
        }
        return column;
    }

    private static List<Field> collectFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            if (current == Query.class) {
                continue;
            }
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    private static Object valueOf(Field field, Query query) {
        try {
            return field.get(query);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("读取查询字段失败: " + field.getName(), e);
        }
    }

    private static boolean isEmpty(Object value) {
        return value == null || (value instanceof String s && s.isBlank());
    }

    private static Collection<Object> toValues(Object value) {
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        if (value instanceof Object[] array) {
            return Arrays.asList(array);
        }
        if (value instanceof String s) {
            return Arrays.stream(s.split(",")).map(String::trim).filter(item -> !item.isEmpty()).collect(Collectors.toList());
        }
        return List.of(value);
    }
}
