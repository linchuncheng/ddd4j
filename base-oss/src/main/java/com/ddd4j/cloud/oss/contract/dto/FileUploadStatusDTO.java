package com.ddd4j.cloud.oss.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传进度
 *
 * @author zhouhengzhe
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileUploadStatusDTO {

    private Double avgSpeed;
    private String pct;
}
