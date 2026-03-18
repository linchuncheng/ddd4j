package com.ddd4j.cloud.sms.contract.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询短信发送量详情返回值
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendStatisticsPageVO implements Serializable {
    /**
     * 未收到回执的短信条数。
     * <p>
     * 示例值:
     * 1
     */
    private Long noRespondedCount;

    /**
     * 接收到回执失败的短信条数。
     * <p>
     * 示例值:
     * 2
     */
    private Long respondedFailCount;

    /**
     * 接收到回执成功的短信条数。
     * <p>
     * 示例值:
     * 17
     */
    private Long respondedSuccessCount;

    /**
     * 短信发送日期，格式为 yyyyMMdd，例如 20181225。
     * <p>
     * 示例值:
     * 20201010
     */
    private String sendDate;

    /**
     * 发送成功的短信条数。
     * <p>
     * 示例值:
     * 20
     */
    private Long totalCount;
}
