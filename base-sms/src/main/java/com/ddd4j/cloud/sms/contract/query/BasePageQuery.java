package com.ddd4j.cloud.sms.contract.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 基础分页查询参数
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/12/14 17:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasePageQuery implements Serializable {
    // 当前页码：从1开始，默认1
    private Long current = 1L;
    // 每页显示记录数：默认10
    private Long size = 10L;
}
