package com.abcpen.zc.sp.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhaocheng on 2018/4/28.
 */
//Context.getSharedPreferences(name,mode);
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface SPData {

    String name() default "sp_data";

    int mode();
}
