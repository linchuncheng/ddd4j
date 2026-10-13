package com.ddd4j.cloud.contract;

/**
 * 业务枚举约定：实现该接口的枚举可作为 R.code / BizException.code 的来源
 *
 * @author Jensen
 */
public interface IEnum {

    String getCode();

    String getMsg();
}
