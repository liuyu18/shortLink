package com.ysl.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {
    enum Type {PARAM, TOKEN}

    Type limtType() default Type.PARAM;

    long lockTime() default 5;
}
