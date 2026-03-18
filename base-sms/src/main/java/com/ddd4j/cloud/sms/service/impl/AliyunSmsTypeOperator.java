package com.ddd4j.cloud.sms.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.ddd4j.cloud.core.contract.Page;
import com.ddd4j.cloud.core.kit.JsonKit;
import com.ddd4j.cloud.kit.lang.DateKit;
import com.ddd4j.cloud.sms.config.BaseSmsProperties;
import com.ddd4j.cloud.sms.contract.command.SmsTemplateAddCommand;
import com.ddd4j.cloud.sms.contract.command.SmsTemplateUpdateCommand;
import com.ddd4j.cloud.sms.contract.constant.SmsConstant;
import com.ddd4j.cloud.sms.contract.enums.BusinessTypeEnum;
import com.ddd4j.cloud.sms.contract.enums.SmsErrorEnum;
import com.ddd4j.cloud.sms.contract.enums.SmsTypeEnum;
import com.ddd4j.cloud.sms.contract.exception.SmsException;
import com.ddd4j.cloud.sms.contract.query.SendDetailPageQuery;
import com.ddd4j.cloud.sms.contract.query.SendStatisticsPageQuery;
import com.ddd4j.cloud.sms.contract.query.SmsSignPageQuery;
import com.ddd4j.cloud.sms.contract.query.SmsTemplatePageQuery;
import com.ddd4j.cloud.sms.contract.vo.*;
import com.ddd4j.cloud.sms.service.AbstractSmsTypeOperator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 阿里云发送短信
 *
 * @author zhouhengzhe
 * @version 1.0
 */
@Slf4j
public class AliyunSmsTypeOperator extends AbstractSmsTypeOperator {


    private Client client;

    @Override
    public String getName() {
        return SmsTypeEnum.ALIYUN.getCode();
    }


    @Override
    @SneakyThrows
    public void init(BaseSmsProperties baseSmsProperties) {
        if (Objects.isNull(baseSmsProperties)
                || Objects.isNull(baseSmsProperties.getAliyun())
                || Objects.isNull(baseSmsProperties.getAliyun().getAccessKeyId())
                || Objects.isNull(baseSmsProperties.getAliyun().getAccessKeySecret())) {
            throw new SmsException(SmsErrorEnum.ALIYUN_CONFIG_IS_NULL);
        }
        Config config = new Config()
                .setAccessKeyId(baseSmsProperties.getAliyun().getAccessKeyId())
                .setAccessKeySecret(baseSmsProperties.getAliyun().getAccessKeySecret());
        this.client = new Client(config);
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
        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(phoneNumber);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(template);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        //{"name":"张三","number":"1390000****"}
        request.setTemplateParam(JsonKit.toJson(templateParams));
        //hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse;
        try {
            sendSmsResponse = client.sendSms(request);
        } catch (Exception error) {
            throw new SmsException(error);
        }
        log.info("调用aliyun发送短信入参为：{}，返回值为：{}", JsonKit.toJson(request), JsonKit.toJson(sendSmsResponse));
        return SmsConstant.SUCCESS_CODE.equals(sendSmsResponse.getStatusCode())
                && SmsConstant.SUCCESS.equals(sendSmsResponse.getBody().getCode());
    }

    /**
     * 分页获取短信模版
     *
     * @param request 参数
     */
    @Override
    public Page<SmsTemplatePageVO> pageSmsTemplate(SmsTemplatePageQuery request) {
        QuerySmsTemplateListRequest querySmsTemplateListRequest = new QuerySmsTemplateListRequest();
        querySmsTemplateListRequest.setPageIndex(request.getCurrent().intValue());
        querySmsTemplateListRequest.setPageSize(request.getSize().intValue());
        querySmsTemplateListRequest.setOwnerId(request.getOwnerId());
        querySmsTemplateListRequest.setResourceOwnerId(request.getResourceOwnerId());
        querySmsTemplateListRequest.setResourceOwnerAccount(request.getResourceOwnerAccount());
        QuerySmsTemplateListResponse querySmsTemplateListResponse;
        try {
            querySmsTemplateListResponse = client.querySmsTemplateList(querySmsTemplateListRequest);
        } catch (Exception e) {
            log.error("获取分页模板报错");
            throw new SmsException(e);
        }
        QuerySmsTemplateListResponseBody body = querySmsTemplateListResponse.getBody();
        log.info("调用aliyun获取短信模板入参为：{}，\n 返回值为：{}", JsonKit.toJson(request), JsonKit.toJson(body));
        if (!SmsConstant.SUCCESS_CODE.equals(querySmsTemplateListResponse.getStatusCode())
                || !SmsConstant.SUCCESS.equals(body.getCode())) {
            throw new SmsException(SmsErrorEnum.GET_TEMPLATE_ERROR);
        }
        //正常返回咯，分页怎么返回这里就怎么返回,可以看一下com.zshy.base.core.contract.Page
        List<QuerySmsTemplateListResponseBody.QuerySmsTemplateListResponseBodySmsTemplateList> smsTemplateList =
                body.getSmsTemplateList();
        List<SmsTemplatePageVO> smsTemplatePageVos = smsTemplateList.stream().map(smsTemplate -> {
            SmsTemplatePageVO.SmsTemplatePageInfo templatePageInfo = null;
            if (Objects.nonNull(smsTemplate.getReason())) {
                templatePageInfo = SmsTemplatePageVO.SmsTemplatePageInfo
                        .builder()
                        .rejectDate(DateKit.parseLocalDateTime(smsTemplate.getReason().getRejectDate()))
                        .rejectInfo(smsTemplate.getReason().getRejectInfo())
                        .rejectSubInfo(smsTemplate.getReason().getRejectSubInfo())
                        .build();
            }
            return SmsTemplatePageVO
                    .builder()
                    .auditStatus(smsTemplate.getAuditStatus())
                    .createDate(DateKit.parseLocalDateTime(smsTemplate.getCreateDate()))
                    .orderId(smsTemplate.getOrderId())
                    .outerTemplateType(smsTemplate.getOuterTemplateType())
                    .reason(templatePageInfo)
                    .templateCode(smsTemplate.getTemplateCode())
                    .templateContent(smsTemplate.getTemplateContent())
                    .templateName(smsTemplate.getTemplateName())
                    .build();
        }).collect(Collectors.toList());
        return Page.of(smsTemplatePageVos, body.getTotalCount(), request.getCurrent(), request.getSize());
    }

    /**
     * 添加短信模板
     *
     * @param command 参数
     * @return 短信模板 Code。
     */
    @Override
    public String addSmsTemplate(SmsTemplateAddCommand command) {
        AddSmsTemplateRequest addSmsTemplateRequest = new AddSmsTemplateRequest();
        addSmsTemplateRequest.setRemark(command.getRemark());
        addSmsTemplateRequest.setTemplateContent(command.getTemplateContent());
        addSmsTemplateRequest.setTemplateName(command.getTemplateName());
        addSmsTemplateRequest.setTemplateType(command.getTemplateType());
        AddSmsTemplateResponse addSmsTemplateResponse = null;
        try {
            addSmsTemplateResponse = client.addSmsTemplate(addSmsTemplateRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun添加短信模板入参为：{}，\n返回值为：{}",
                JsonKit.toJson(addSmsTemplateRequest), JsonKit.toJson(addSmsTemplateResponse));
        if (!SmsConstant.SUCCESS_CODE.equals(addSmsTemplateResponse.getStatusCode())
                || !SmsConstant.SUCCESS.equals(addSmsTemplateResponse.getBody().getCode())) {
            throw new SmsException(SmsErrorEnum.ADD_TEMPLATE_ERROR);
        }
        return addSmsTemplateResponse.getBody().getTemplateCode();
    }

    /**
     * 删除短信模板
     *
     * @param templateCode 模板code
     * @return true/false 成功/失败
     */
    @Override
    public Boolean deleteSmsTemplate(String templateCode) {
        DeleteSmsTemplateRequest deleteSmsTemplateRequest = new DeleteSmsTemplateRequest();
        deleteSmsTemplateRequest.setTemplateCode(templateCode);
        DeleteSmsTemplateResponse deleteSmsTemplateResponse;
        try {
            deleteSmsTemplateResponse = client.deleteSmsTemplate(deleteSmsTemplateRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun删除短信模板入参为：{}，\n返回值为：{}",
                JsonKit.toJson(deleteSmsTemplateRequest), JsonKit.toJson(deleteSmsTemplateResponse));
        return SmsConstant.SUCCESS_CODE.equals(deleteSmsTemplateResponse.getStatusCode())
                && SmsConstant.SUCCESS.equals(deleteSmsTemplateResponse.getBody().getCode());
    }

    /**
     * 查询短信模板审核状态
     *
     * @param templateCode 模板code
     */
    @Override
    public SmsTemplateQueryVO querySmsTemplateAuditStatus(String templateCode) {
        QuerySmsTemplateRequest querySmsTemplateRequest = new QuerySmsTemplateRequest();
        querySmsTemplateRequest.setTemplateCode(templateCode);
        QuerySmsTemplateResponse querySmsTemplateResponse;
        try {
            querySmsTemplateResponse = client.querySmsTemplate(querySmsTemplateRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun查询短信模板审核状态入参为：{}，\n返回值为：{}",
                JsonKit.toJson(querySmsTemplateRequest), JsonKit.toJson(querySmsTemplateResponse));
        if (!SmsConstant.SUCCESS_CODE.equals(querySmsTemplateResponse.getStatusCode())
                || !SmsConstant.SUCCESS.equals(querySmsTemplateResponse.getBody().getCode())) {
            throw new SmsException(SmsErrorEnum.QUERY_TEMPLATE_ERROR);
        }
        QuerySmsTemplateResponseBody templateResponseBody = querySmsTemplateResponse.getBody();
        return SmsTemplateQueryVO
                .builder()
                .createDate(DateKit.parseLocalDateTime(templateResponseBody.getCreateDate()))
                .reason(templateResponseBody.getReason())
                .templateCode(templateResponseBody.getTemplateCode())
                .templateContent(templateResponseBody.getTemplateContent())
                .templateName(templateResponseBody.getTemplateName())
                .templateStatus(templateResponseBody.getTemplateStatus())
                .templateType(templateResponseBody.getTemplateType())
                .build();
    }

    /**
     * 修改未审核通过的短信模板信息，并重新提交审核
     *
     * @param command
     */
    @Override
    public Boolean modifyAndReSubmitSmsTemplate(SmsTemplateUpdateCommand command) {
        ModifySmsTemplateRequest modifySmsTemplateRequest = new ModifySmsTemplateRequest();
        modifySmsTemplateRequest.setRemark(command.getRemark());
        modifySmsTemplateRequest.setTemplateContent(command.getTemplateContent());
        modifySmsTemplateRequest.setTemplateName(command.getTemplateName());
        modifySmsTemplateRequest.setTemplateType(command.getTemplateType());
        modifySmsTemplateRequest.setTemplateCode(command.getTemplateCode());
        ModifySmsTemplateResponse modifySmsTemplateResponse;
        try {
            modifySmsTemplateResponse = client.modifySmsTemplate(modifySmsTemplateRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun修改短信模板入参为：{}，\n返回值为：{}",
                JsonKit.toJson(modifySmsTemplateRequest), JsonKit.toJson(modifySmsTemplateResponse));

        return SmsConstant.SUCCESS_CODE.equals(modifySmsTemplateResponse.getStatusCode())
                && SmsConstant.SUCCESS.equals(modifySmsTemplateResponse.getBody().getCode());
    }

    /**
     * 分页查询短信发送记录和发送状态等信息
     *
     * @param query 查询参数
     * @return 短信发送记录和发送状态等信息
     */
    @Override
    public Page<SendDetailPageVO> pageSendDetail(SendDetailPageQuery query) {
        QuerySendDetailsRequest querySendDetailsRequest = new QuerySendDetailsRequest();
        querySendDetailsRequest.setBizId(query.getBizId());
        querySendDetailsRequest.setCurrentPage(query.getCurrent());
        querySendDetailsRequest.setOwnerId(query.getOwnerId());
        querySendDetailsRequest.setPageSize(query.getSize());
        querySendDetailsRequest.setPhoneNumber(query.getPhoneNumber());
        querySendDetailsRequest.setResourceOwnerAccount(query.getResourceOwnerAccount());
        querySendDetailsRequest.setResourceOwnerId(query.getResourceOwnerId());
        querySendDetailsRequest.setSendDate(query.getSendDate());
        QuerySendDetailsResponse querySendDetailsResponse;
        try {
            querySendDetailsResponse = client.querySendDetails(querySendDetailsRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun查询短信发送记录入参为：{}，\n返回值为：{}",
                JsonKit.toJson(querySendDetailsRequest), JsonKit.toJson(querySendDetailsResponse));
        if (!SmsConstant.SUCCESS_CODE.equals(querySendDetailsResponse.getStatusCode())
                || !SmsConstant.SUCCESS.equals(querySendDetailsResponse.getBody().getCode())) {
            throw new SmsException(SmsErrorEnum.QUERY_SMS_RECORD_ERROR);
        }
        List<SendDetailPageVO> detailPageVos = querySendDetailsResponse.getBody()
                .getSmsSendDetailDTOs()
                .getSmsSendDetailDTO()
                .stream()
                .map(smsSendDetailDTO -> SendDetailPageVO.builder()
                        .content(smsSendDetailDTO.getContent())
                        .errCode(smsSendDetailDTO.getErrCode())
                        .outId(smsSendDetailDTO.getOutId())
                        .phoneNum(smsSendDetailDTO.getPhoneNum())
                        .receiveDate(DateKit.parseLocalDateTime(smsSendDetailDTO.getReceiveDate()))
                        .sendDate(DateKit.parseLocalDateTime(smsSendDetailDTO.getSendDate()))
                        .sendStatus(smsSendDetailDTO.getSendStatus())
                        .templateCode(smsSendDetailDTO.getTemplateCode())
                        .build()).collect(Collectors.toList());
        return Page.of(detailPageVos,
                Long.parseLong(querySendDetailsResponse.getBody().getTotalCount()),
                query.getCurrent(),
                query.getSize());
    }

    /**
     * 查询短信发送量详情
     *
     * @param query 查询参数
     * @return 短信发送量详情
     */
    @Override
    public Page<SendStatisticsPageVO> pageSendStatistics(SendStatisticsPageQuery query) {
        QuerySendStatisticsRequest querySendStatisticsRequest = new QuerySendStatisticsRequest();
        querySendStatisticsRequest.setEndDate(query.getEndDate());
        querySendStatisticsRequest.setIsGlobe(query.getGlobe());
        querySendStatisticsRequest.setOwnerId(query.getOwnerId());
        querySendStatisticsRequest.setPageIndex(query.getCurrent().intValue());
        querySendStatisticsRequest.setPageSize(query.getSize().intValue());
        querySendStatisticsRequest.setResourceOwnerAccount(query.getResourceOwnerAccount());
        querySendStatisticsRequest.setResourceOwnerId(query.getResourceOwnerId());
        querySendStatisticsRequest.setSignName(query.getSignName());
        querySendStatisticsRequest.setStartDate(query.getStartDate());
        querySendStatisticsRequest.setTemplateType(query.getTemplateType());
        QuerySendStatisticsResponse querySendStatisticsResponse;
        try {
            querySendStatisticsResponse = client.querySendStatistics(querySendStatisticsRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun查询短信发送量详情入参为：{}，\n返回值为：{}",
                JsonKit.toJson(querySendStatisticsRequest), JsonKit.toJson(querySendStatisticsResponse));
        if (!SmsConstant.SUCCESS_CODE.equals(querySendStatisticsResponse.getStatusCode())
                || !SmsConstant.SUCCESS.equals(querySendStatisticsResponse.getBody().getCode())) {
            throw new SmsException(SmsErrorEnum.QUERY_SMS_SEND_STATISTICS_ERROR);
        }
        List<SendStatisticsPageVO> sendStatisticsPageVos = querySendStatisticsResponse.getBody()
                .getData()
                .getTargetList()
                .stream()
                .map(target -> SendStatisticsPageVO.builder()
                        .noRespondedCount(target.getNoRespondedCount())
                        .respondedFailCount(target.getRespondedFailCount())
                        .sendDate(target.getSendDate())
                        .totalCount(target.getTotalCount())
                        .build()).collect(Collectors.toList());
        return Page.of(sendStatisticsPageVos,
                querySendStatisticsResponse.getBody().getData().getTotalSize(),
                query.getCurrent(),
                query.getSize());
    }

    /**
     * 查询短信签名列表详情，分页获取短信签名列表
     *
     * @param query 查询参数
     * @return 短信签名列表详情
     */
    @Override
    public Page<SmsSignPageVO> pageSmsSign(SmsSignPageQuery query) {
        QuerySmsSignListRequest querySmsSignListRequest = new QuerySmsSignListRequest();
        querySmsSignListRequest.setOwnerId(query.getOwnerId());
        querySmsSignListRequest.setResourceOwnerAccount(query.getResourceOwnerAccount());
        querySmsSignListRequest.setResourceOwnerId(query.getResourceOwnerId());
        querySmsSignListRequest.setPageIndex(query.getCurrent().intValue());
        querySmsSignListRequest.setPageSize(query.getSize().intValue());
        QuerySmsSignListResponse querySmsSignListResponse;
        try {
            querySmsSignListResponse = client.querySmsSignList(querySmsSignListRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun查询短信签名列表详情入参为：{}，\n返回值为：{}",
                JsonKit.toJson(querySmsSignListRequest), JsonKit.toJson(querySmsSignListResponse));
        if (!SmsConstant.SUCCESS_CODE.equals(querySmsSignListResponse.getStatusCode())
                || !SmsConstant.SUCCESS.equals(querySmsSignListResponse.getBody().getCode())) {
            throw new SmsException(SmsErrorEnum.QUERY_SMS_SIGN_ERROR);
        }
        List<SmsSignPageVO> smsSignPageVos = querySmsSignListResponse.getBody()
                .getSmsSignList()
                .stream()
                .map(smsSignDTO -> SmsSignPageVO
                        .builder()
                        .auditStatus(smsSignDTO.getAuditStatus())
                        .businessType(Objects.equals(BusinessTypeEnum.SMS_CODE_TYPE.getDesc(), smsSignDTO.getBusinessType())
                                ? BusinessTypeEnum.SMS_CODE_TYPE.getCode() : BusinessTypeEnum.SMS_COMMON_TYPE.getCode())
                        .createDate(DateKit.parseLocalDateTime(smsSignDTO.getCreateDate()))
                        .orderId(smsSignDTO.getOrderId())
                        .reason(SmsSignPageVO.SmsSignPageInfo
                                .builder()
                                .rejectDate(DateKit.parseLocalDateTime(smsSignDTO.getReason().getRejectDate()))
                                .rejectInfo(smsSignDTO.getReason().getRejectInfo())
                                .rejectSubInfo(smsSignDTO.getReason().getRejectSubInfo())
                                .build())
                        .signName(smsSignDTO.getSignName())
                        .build()
                ).collect(Collectors.toList());
        return Page.of(smsSignPageVos,
                querySmsSignListResponse.getBody().getTotalCount(),
                query.getCurrent(),
                query.getSize());
    }

    /**
     * 查询短信签名申请状态
     *
     * @param signName 签名名称
     * @return 短信签名申请状态
     */
    @Override
    public SmsSignDetailVO querySmsSign(String signName) {
        QuerySmsSignRequest querySmsSignRequest = new QuerySmsSignRequest();
        querySmsSignRequest.setSignName(signName);
        QuerySmsSignResponse querySmsSignResponse;
        try {
            querySmsSignResponse = client.querySmsSign(querySmsSignRequest);
        } catch (Exception e) {
            throw new SmsException(e);
        }
        log.info("调用aliyun查询短信签名详情入参为：{}，\n返回值为：{}",
                JsonKit.toJson(querySmsSignRequest), JsonKit.toJson(querySmsSignResponse));
        if (!SmsConstant.SUCCESS_CODE.equals(querySmsSignResponse.getStatusCode())
                || !SmsConstant.SUCCESS.equals(querySmsSignResponse.getBody().getCode())) {
            throw new SmsException(SmsErrorEnum.QUERY_SMS_SIGN_ERROR);
        }
        return SmsSignDetailVO
                .builder()
                .createDate(DateKit.parseLocalDateTime(querySmsSignResponse.getBody().getCreateDate()))
                .reason(querySmsSignResponse.getBody().getReason())
                .signName(querySmsSignResponse.getBody().getSignName())
                .signStatus(querySmsSignResponse.getBody().getSignStatus())
                .build();
    }
}
