package com.ddd4j.cloud.sms.service;

import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.kit.Operator;
import com.ddd4j.cloud.sms.config.BaseSmsProperties;
import com.ddd4j.cloud.sms.contract.command.SmsTemplateAddCommand;
import com.ddd4j.cloud.sms.contract.command.SmsTemplateUpdateCommand;
import com.ddd4j.cloud.sms.contract.query.SendDetailPageQuery;
import com.ddd4j.cloud.sms.contract.query.SendStatisticsPageQuery;
import com.ddd4j.cloud.sms.contract.query.SmsSignPageQuery;
import com.ddd4j.cloud.sms.contract.query.SmsTemplatePageQuery;
import com.ddd4j.cloud.sms.contract.vo.*;

import java.util.Map;

/**
 * 短信类型操作
 *
 * @author zhouhengzhe
 */
public interface SmsTypeOperator extends Operator<String> {

    void init(BaseSmsProperties baseSmsProperties);

    /**
     * 发送单条信息
     *
     * @param phoneNumber    手机号码
     * @param template       模板
     * @param templateParams 模板参数
     * @param signName       签名
     * @return 发送是否成功
     */
    Boolean sendSingle(String phoneNumber, String template, Map<String, String> templateParams, String signName);

    /**
     * 分页查询短信模板
     *
     * @param request 查询条件
     * @return 分页结果
     */
    Page<SmsTemplatePageVO> pageSmsTemplate(SmsTemplatePageQuery request);

    /**
     * 添加短信模板
     *
     * @param command 模板参数
     * @return 模板code
     */
    String addSmsTemplate(SmsTemplateAddCommand command);

    /**
     * 删除短信模板
     *
     * @param templateCode 模板code
     * @return true/false 成功/失败
     */
    Boolean deleteSmsTemplate(String templateCode);

    /**
     * 查询短信模板审核状态
     *
     * @param templateCode 模板code
     */
    SmsTemplateQueryVO querySmsTemplateAuditStatus(String templateCode);

    // 修改未审核通过的短信模板信息，并重新提交审核
    Boolean modifyAndReSubmitSmsTemplate(SmsTemplateUpdateCommand command);

    /**
     * 分页查询短信发送记录和发送状态等信息
     *
     * @param query 查询参数
     * @return 短信发送记录和发送状态等信息
     */
    Page<SendDetailPageVO> pageSendDetail(SendDetailPageQuery query);

    /**
     * 查询短信发送量详情
     *
     * @param query 查询参数
     * @return 短信发送量详情
     */
    Page<SendStatisticsPageVO> pageSendStatistics(SendStatisticsPageQuery query);

    /**
     * 查询短信签名列表详情，分页获取短信签名列表
     *
     * @param query 查询参数
     * @return 短信签名列表详情
     */
    Page<SmsSignPageVO> pageSmsSign(SmsSignPageQuery query);

    /**
     * 查询短信签名申请状态
     *
     * @param signName 签名名称
     * @return 短信签名申请状态
     */
    SmsSignDetailVO querySmsSign(String signName);
}
