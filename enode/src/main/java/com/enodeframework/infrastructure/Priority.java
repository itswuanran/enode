package com.enodeframework.infrastructure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {

    int value() default 0;
}
