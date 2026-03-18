package com.ddd4j.cloud.sms.service;


import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.contract.enums.ResultCode;
import com.ddd4j.cloud.sms.contract.command.SmsTemplateAddCommand;
import com.ddd4j.cloud.sms.contract.command.SmsTemplateUpdateCommand;
import com.ddd4j.cloud.sms.contract.enums.SmsErrorEnum;
import com.ddd4j.cloud.sms.contract.exception.SmsException;
import com.ddd4j.cloud.sms.contract.query.SendDetailPageQuery;
import com.ddd4j.cloud.sms.contract.query.SendStatisticsPageQuery;
import com.ddd4j.cloud.sms.contract.query.SmsSignPageQuery;
import com.ddd4j.cloud.sms.contract.query.SmsTemplatePageQuery;
import com.ddd4j.cloud.sms.contract.vo.*;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * sms抽象缓冲层
 *
 * @author zhouhengzhe
 * @version 1.0
 */
public abstract class AbstractSmsTypeOperator implements SmsTypeOperator {

    /**
     * 检验参数
     *
     * @param phoneNumber    手机号
     * @param template       模板code
     * @param templateParams 模板参数
     * @param signName       签名
     */
    public void checkParam(String phoneNumber, String template, Map<String, String> templateParams, String signName) {
        if (StringUtils.isEmpty(phoneNumber)) {
            throw new SmsException(SmsErrorEnum.PHONE_NUMBER_IS_NULL);
        }
        if (StringUtils.isEmpty(template)) {
            throw new SmsException(SmsErrorEnum.TEMPLATE_IS_NULL);
        }
        if (templateParams.isEmpty()) {
            throw new SmsException(SmsErrorEnum.TEMPLATE_PARAMS_IS_NULL);
        }
        if (StringUtils.isEmpty(signName)) {
            throw new SmsException(SmsErrorEnum.SIGN_NAME_IS_NULL);
        }
    }

    /**
     * 分页查询短信模板
     *
     * @param request 查询条件
     * @return 分页结果
     */
    @Override
    public Page<SmsTemplatePageVO> pageSmsTemplate(SmsTemplatePageQuery request) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 添加短信模板
     *
     * @param command 模板参数
     * @return 模板code
     */
    @Override
    public String addSmsTemplate(SmsTemplateAddCommand command) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 删除短信模板
     *
     * @param templateCode 模板code
     * @return true/false 成功/失败
     */
    @Override
    public Boolean deleteSmsTemplate(String templateCode) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 查询短信模板审核状态
     *
     * @param templateCode 模板code
     */
    @Override
    public SmsTemplateQueryVO querySmsTemplateAuditStatus(String templateCode) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 修改未审核通过的短信模板信息，并重新提交审核
     *
     * @param command
     */
    @Override
    public Boolean modifyAndReSubmitSmsTemplate(SmsTemplateUpdateCommand command) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 分页查询短信发送记录和发送状态等信息
     *
     * @param query 查询参数
     * @return 短信发送记录和发送状态等信息
     */
    @Override
    public Page<SendDetailPageVO> pageSendDetail(SendDetailPageQuery query) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 查询短信发送量详情
     *
     * @param query 查询参数
     * @return 短信发送量详情
     */
    @Override
    public Page<SendStatisticsPageVO> pageSendStatistics(SendStatisticsPageQuery query) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 查询短信签名列表详情，分页获取短信签名列表
     *
     * @param query 查询参数
     * @return 短信签名列表详情
     */
    @Override
    public Page<SmsSignPageVO> pageSmsSign(SmsSignPageQuery query) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }

    /**
     * 查询短信签名申请状态
     *
     * @param signName 签名名称
     * @return 短信签名申请状态
     */
    @Override
    public SmsSignDetailVO querySmsSign(String signName) {
        throw new SmsException(ResultCode.FUTURE_DEVELOPMENT);
    }
}
