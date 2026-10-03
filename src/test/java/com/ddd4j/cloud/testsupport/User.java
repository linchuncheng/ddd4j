package com.ddd4j.cloud.testsupport;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ddd4j.cloud.data.annotation.OnCreate;
import com.ddd4j.cloud.data.annotation.OnUpdate;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试模型：模型即实体，直接标注 MP 注解
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private Integer age;

    private Long tenantId;

    @OnCreate
    private LocalDateTime createTime;

    @OnUpdate
    private LocalDateTime updateTime;
}
