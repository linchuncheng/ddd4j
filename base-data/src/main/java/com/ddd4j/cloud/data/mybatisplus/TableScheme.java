package com.ddd4j.cloud.data.mybatisplus;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.ddd4j.cloud.data.annotation.OnCreate;
import com.ddd4j.cloud.data.annotation.OnUpdate;
import com.ddd4j.cloud.data.annotation.OrderBy;
import com.ddd4j.cloud.data.annotation.TenantId;
import lombok.Data;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class TableScheme {
    private String tableName;
    private Map<String, String> field2Column;
    private Field id;
    private Field tenantId;
    private Field tableLogic;
    private List<Field> onCreateFields = new ArrayList<>();
    private List<Field> onUpdateFields = new ArrayList<>();
    private String[] defaultOrderBy;

    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    /**
     * 驼峰转下划线
     */
    protected static String toUnderline(String humpString) {
        if (humpString == null || humpString.isEmpty()) return humpString;
        Matcher matcher = HUMP_PATTERN.matcher(humpString);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    protected boolean containsField(String field) {
        return field2Column != null && field2Column.containsKey(field.toLowerCase());
    }

    protected String getField(String field) {
        return field2Column.get(field.toLowerCase());
    }

    protected boolean containsColumn(String column) {
        return field2Column != null && field2Column.containsValue(column.toLowerCase());
    }

    protected static <T> T findFieldValue(Object object, Field field) {
        return (T) ReflectionKit.getFieldValue(object, field.getName());
    }

    protected static String getColumn(Field field) {
        TableField tableField = field.getAnnotation(TableField.class);
        return tableField != null && tableField.value() != null && !tableField.value().isEmpty() ? tableField.value() : TableScheme.toUnderline(field.getName());
    }

    protected static TableScheme build(Class<?> poClass) {
        if (poClass == null) {
            return null;
        } else {
            TableScheme tableScheme = new TableScheme();
            TableName table = poClass.getAnnotation(TableName.class);
            if (table == null) {
                throw new IllegalArgumentException("PO class must annotated with @TableName(value = \"table_name\", autoResultMap = true)");
            } else {
                tableScheme.setTableName(table.value());
                List<Field> poFields = FieldUtils.getAllFieldsList(poClass);
                poFields = poFields.stream().filter((p) -> !Modifier.isStatic(p.getModifiers())).collect(Collectors.toList());
                if (!poFields.isEmpty()) {
                    tableScheme.field2Column = new HashMap<>(poFields.size());
                    for (Field poField : poFields) {
                        poField.setAccessible(true);
                        String fieldName = poField.getName();
                        // 优先读取TableField.value字段，否则把字段从驼峰式转换为下划线
                        TableField tableField = poField.getAnnotation(TableField.class);
                        String column = tableField != null && tableField.value() != null && !tableField.value().isEmpty() ? tableField.value() : TableScheme.toUnderline(fieldName);
                        tableScheme.field2Column.put(fieldName.toLowerCase(), column);
                        if (poField.isAnnotationPresent(TableId.class)) {
                            tableScheme.id = poField;
                        }
                        if (poField.isAnnotationPresent(TenantId.class)) {
                            tableScheme.tenantId = poField;
                        }
                        if (poField.isAnnotationPresent(TableLogic.class)) {
                            tableScheme.tableLogic = poField;
                        }
                        if (poField.isAnnotationPresent(OnCreate.class)) {
                            tableScheme.getOnCreateFields().add(poField);
                        }
                        if (poField.isAnnotationPresent(OnUpdate.class)) {
                            tableScheme.getOnUpdateFields().add(poField);
                        }
                    }

                }
                OrderBy orderBy = poClass.getAnnotation(OrderBy.class);
                if (orderBy == null && poClass.getSuperclass() != null) {
                    orderBy = poClass.getSuperclass().getAnnotation(OrderBy.class);
                }
                if (orderBy != null && orderBy.value() != null && orderBy.value().length != 0) {
                    tableScheme.setDefaultOrderBy(orderBy.value());
                }
                return tableScheme;
            }
        }
    }

}