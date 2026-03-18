package com.ddd4j.cloud.excel.validate;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * 校验工具
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
public final class Validators {

    private Validators() {
    }

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    /**
     * Validates all constraints on {@code object}.
     *
     * @param object object to validate
     * @param <T>    the type of the object to validate
     * @return constraint violations or an empty set if none
     * @throws IllegalArgumentException if object is {@code null} or if {@code null} is
     *                                  passed to the varargs groups
     * @throws ValidationException      if a non recoverable error happens during the
     *                                  validation process
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return VALIDATOR.validate(object);
    }

}
