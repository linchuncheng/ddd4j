package com.ddd4j.cloud.excel.handler.listener;

import cn.idev.excel.event.AnalysisEventListener;
import com.ddd4j.cloud.excel.vo.ErrorMessage;

import java.util.List;

/**
 * list analysis EventListener
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
public abstract class ListAnalysisEventListener<T> extends AnalysisEventListener<T> {

    /**
     * 获取 excel 解析的对象列表
     *
     * @return 集合
     */
    public abstract List<T> getList();

    /**
     * 获取异常校验结果
     *
     * @return 集合
     */
    public abstract List<ErrorMessage> getErrors();

}
