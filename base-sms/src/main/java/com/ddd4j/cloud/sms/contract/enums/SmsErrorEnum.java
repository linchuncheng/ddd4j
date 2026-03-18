package com.ddd4j.cloud.sms.contract.enums;

import com.ddd4j.cloud.core.contract.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信异常枚举类
 *
 * @author zhouhengzhe
 */

@Getter
@AllArgsConstructor
public enum SmsErrorEnum implements IEnum<Integer> {
    CREATE_SMS_FACTORY_ERROR(2001, "创建短信工厂失败"),
    SEND_SMS_ERROR(2002, "发送短信失败"),
    PHONE_NUMBER_IS_NULL(2003, "手机号码不能为空"),
    TEMPLATE_IS_NULL(2004, "模板不能为空"),
    GET_TEMPLATE_ERROR(2005, "获取分页模板报错"),
    ADD_TEMPLATE_ERROR(2006, "添加短信模板错误"),
    TEMPLATE_PARAMS_IS_NULL(2007, "模板填充内容不能为空"),
    SIGN_NAME_IS_NULL(2008, "签名不能为空"),
    QUERY_TEMPLATE_ERROR(2009, "查询短信模板错误"),
    QUERY_SMS_RECORD_ERROR(2010, "查询短信发送记录错误"),
    QUERY_SMS_SEND_STATISTICS_ERROR(2011, "查询短信发送量详情错误"),
    QUERY_SMS_SIGN_ERROR(2012, "分页查询短信签名错误"),
    ALIYUN_CONFIG_IS_NULL(2013, "阿里云短信配置不能为空"),
    BOSHITONG_CONFIG_IS_NULL(2014, "博士通短信配置不能为空"),
    ;
    private final Integer code;
    private final String desc;
}
