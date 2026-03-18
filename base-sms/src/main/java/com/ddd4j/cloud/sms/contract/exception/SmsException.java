package com.ddd4j.cloud.sms.contract.exception;


import com.ddd4j.cloud.core.contract.enums.IEnum;
import com.ddd4j.cloud.core.contract.exception.ServiceException;

/**
 * 短信异常
 *
 * @author zhouhengzhe
 */
public class SmsException extends ServiceException {

    public SmsException(IEnum<Integer> iEnum) {
        super(iEnum);
    }

    /**
     * 固定异常
     *
     * @param e 异常
     */
    public SmsException(Throwable e) {
        super(e);
    }
}
