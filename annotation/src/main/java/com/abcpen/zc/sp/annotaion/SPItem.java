package com.abcpen.zc.sp.annotaion;

import com.abcpen.zc.convert.DefaultGsonConvert;
import com.abcpen.zc.convert.ISPConvert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhaocheng on 2018/4/28.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SPItem {


    String key() default "";

    Class valueType() default String.class;


    Class<? extends ISPConvert> convert() default DefaultGsonConvert.class;
}
