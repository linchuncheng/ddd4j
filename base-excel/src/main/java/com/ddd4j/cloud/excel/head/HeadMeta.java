package com.ddd4j.cloud.excel.head;

import lombok.Data;

import java.util.List;
import java.util.Set;


/**
 * 自定义头部信息
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 16:53
 */
@Data
public class HeadMeta {

    /**
     * <p>
     * 自定义头部信息
     * </p>
     * 实现类根据数据的class信息，定制Excel头<br/>
     * 具体方法使用参考：<a href="https://www.yuque.com/easyexcel/doc/write#b4b9de00">...</a>
     */
    private List<List<String>> head;
    // 忽略头对应字段名称
    private Set<String> ignoreHeadFields;

}
