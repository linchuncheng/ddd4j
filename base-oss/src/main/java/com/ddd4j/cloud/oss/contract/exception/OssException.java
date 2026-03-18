package com.ddd4j.cloud.oss.contract.exception;


import com.ddd4j.cloud.core.contract.enums.IEnum;
import com.ddd4j.cloud.core.contract.exception.ServiceException;

/**
 * oss异常
 *
 * @author zhouhengzhe
 */
public class OssException extends ServiceException {


    /**
     * 抛出配置中的国际化异常（i18配置文件下的）
     *
     * @param iEnum 错误码枚举类
     */
    public OssException(IEnum<Integer> iEnum) {
        super(iEnum);
    }

    /**
     * 自定义国际化，也就是说message是国际化后的消息
     *
     * @param errCode 错误码
     * @param msg     国际化后的消息
     */
    public OssException(Integer errCode, String msg) {
        super(errCode, msg);
    }

    /**
     * 固定异常
     *
     * @param e 异常
     */
    public OssException(Throwable e) {
        super(e);
    }
}
