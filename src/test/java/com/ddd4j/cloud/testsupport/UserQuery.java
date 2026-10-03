package com.ddd4j.cloud.testsupport;

import com.ddd4j.cloud.data.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 测试查询对象：演示后缀约定
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends Query {

    private String username;

    private String usernameLike;

    private List<Long> idIn;

    private Integer ageGe;

    private Integer ageLe;
}
