package com.ddd4j.cloud.sms.service.impl;

import cn.hutool.http.HttpUtil;
import com.ddd4j.cloud.sms.config.BaseSmsProperties;
import com.ddd4j.cloud.sms.contract.constant.SmsConstant;
import com.ddd4j.cloud.sms.contract.enums.SmsErrorEnum;
import com.ddd4j.cloud.sms.contract.enums.SmsTypeEnum;
import com.ddd4j.cloud.sms.contract.exception.SmsException;
import com.ddd4j.cloud.sms.service.AbstractSmsTypeOperator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 博士通发送短信接口
 *
 * @author zhouhengzhe
 * @version 1.0
 */
public class BoshitongSmsTypeOperator extends AbstractSmsTypeOperator {

    private BaseSmsProperties.BoshitongSmsProperties boshitongSmsProperties;

    @Override
    public String getName() {
        return SmsTypeEnum.BOSHITONG.getCode();
    }

    @Override
    public void init(BaseSmsProperties baseSmsProperties) {
        if (Objects.isNull(baseSmsProperties)
                || Objects.isNull(baseSmsProperties.getBoshitong())
                || Objects.isNull(baseSmsProperties.getBoshitong().getUid())
                || Objects.isNull(baseSmsProperties.getBoshitong().getPwd())
                || Objects.isNull(baseSmsProperties.getBoshitong().getSrcphone())) {
            throw new SmsException(SmsErrorEnum.BOSHITONG_CONFIG_IS_NULL);
        }
        boshitongSmsProperties = baseSmsProperties.getBoshitong();
    }

    /**
     * 发送单条信息
     *
     * @param phoneNumber    手机号码
     * @param template       模板
     * @param templateParams 模板参数
     * @param signName       签名
     * @return 发送是否成功
     */
    @Override
    public Boolean sendSingle(String phoneNumber, String template, Map<String, String> templateParams, String signName) {
        super.checkParam(phoneNumber, template, templateParams, signName);
        Map<String, Object> params = createBaseParams();
        params.put("mobile", phoneNumber);
        try {
            String content = template;
            //替换成如下
            for (Map.Entry<String, String> entry : templateParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                content = content.replace(key, value);
            }
            params.put("msg", URLEncoder.encode(signName + content, String.valueOf(StandardCharsets.UTF_8)));
        } catch (UnsupportedEncodingException e) {
            throw new SmsException(e);
        }
        String response = HttpUtil.post(SmsConstant.BOSHITONG_URL, params);
        String[] result = response.split(",");
        return "0".equals(result[0]);
    }

    public Map<String, Object> createBaseParams() {
        Map<String, Object> param = new HashMap<>();
        param.put("uid", boshitongSmsProperties.getUid());
        param.put("pwd", boshitongSmsProperties.getPwd());
        param.put("srcphone", boshitongSmsProperties.getSrcphone());
        return param;
    }
}
