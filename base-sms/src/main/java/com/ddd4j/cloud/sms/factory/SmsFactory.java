package com.ddd4j.cloud.sms.factory;

import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.sms.config.BaseSmsProperties;
import com.ddd4j.cloud.sms.contract.enums.SmsErrorEnum;
import com.ddd4j.cloud.sms.contract.enums.SmsTypeEnum;
import com.ddd4j.cloud.sms.contract.exception.SmsException;
import com.ddd4j.cloud.sms.service.SmsTypeOperator;
import com.ddd4j.cloud.sms.service.SmsTypeOperatorRouter;
import com.ddd4j.cloud.sms.service.impl.AliyunSmsTypeOperator;
import com.ddd4j.cloud.sms.service.impl.BoshitongSmsTypeOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 短信工厂
 *
 * @author zhouhengzhe
 * @version 1.0
 */
@Slf4j
@Component
public class SmsFactory {
    @Autowired
    private SmsTypeOperatorRouter smsTypeOperatorRouter;
    @Autowired
    private BaseSmsProperties baseSmsProperties;

    /**
     * 生产短信发送方
     *
     * @param provider 短信类型，
     * @return 短信发送方
     * @see SmsTypeEnum
     */
    public SmsTypeOperator buildSmsClient(String provider) {
        log.info("init构建【{}】的短信发送方,配置信息为{}", provider, JsonKit.toJson(baseSmsProperties));
        SmsTypeOperator route = smsTypeOperatorRouter.route(provider);
        if (Objects.isNull(route)) {
            log.error("当前的【{}】的对应的yml配置为空，或者【{}】类型不存在", provider, provider);
            throw new SmsException(SmsErrorEnum.CREATE_SMS_FACTORY_ERROR);
        }
        route.init(baseSmsProperties);
        return route;
    }

    // 获取阿里云短信发送
    public AliyunSmsTypeOperator getAliyunSmsClient() {
        return (AliyunSmsTypeOperator) buildSmsClient(SmsTypeEnum.ALIYUN.getCode());
    }

    // 获取博士通短信发送
    public BoshitongSmsTypeOperator getBoshitongSmsClient() {
        return (BoshitongSmsTypeOperator) buildSmsClient(SmsTypeEnum.BOSHITONG.getCode());
    }
}
