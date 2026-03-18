package com.ddd4j.cloud.sms.service;

import com.ddd4j.cloud.core.kit.Operator;
import com.ddd4j.cloud.core.kit.OperatorRouter;

import org.springframework.stereotype.Component;

/**
 * 短信类型操作路由
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/10/19 18:03
 */
@Component
public class SmsTypeOperatorRouter extends OperatorRouter<String, SmsTypeOperator> {
    /**
     * 返回{@link Operator}的子类的{@link Class}
     *
     * @return {@link SmsTypeOperator}
     */
    @Override
    public Class<SmsTypeOperator> getOperatorClass() {
        return SmsTypeOperator.class;
    }
}
