package com.ddd4j.cloud.excel.converters;


import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import com.ddd4j.cloud.excel.annotation.ExcelEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 通用枚举转换器
 * <p>
 * 如果需要使用，需要提供一个ofCode方法，此方法是通过code返回的是对应的枚举类，然后还要有个desc获取对应的类型
 *
 * @author zhouhengzhe
 */
public class EStringConverter implements Converter<String> {
    @Override
    public Class supportJavaTypeKey() {
        //指定转换器接收参数类型
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        //指定返回的参数类型
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        //value:状态码  contentProperty：字段属性  globalConfiguration：全局配置
        //获取字段属性中的注解
        Field field = contentProperty.getField();
        //获取该字段所属枚举
        ExcelEnum excelEnum = field.getAnnotation(ExcelEnum.class);
        //获取注解中的枚举信息
        Class<? extends Enum> type = excelEnum.type();
        //获取枚举类的方法名 “codeOf”就是自己编写的函数，Integer.class 指定入参类型
        Method codeOf = type.getMethod("ofCode", String.class);
        //反射执行方法，此方法得到的是一个枚举实例（具体得到什么，结合自身项目）
        Object invoke = codeOf.invoke(type, value);
        //枚举实例调用getname方法，得到name的值
        Method getDesc = invoke.getClass().getMethod("getDesc");
        String name = String.valueOf(getDesc.invoke(invoke));
        //将转换的值进行返回
        return new WriteCellData(name);
    }
}
