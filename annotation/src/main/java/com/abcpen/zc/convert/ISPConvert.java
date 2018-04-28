package com.abcpen.zc.convert;

/**
 * Created by zhaocheng on 2018/4/28.
 */

public interface ISPConvert<T> {

    String convertToObject(T t);

    T unConvertData(String data,Class<T> t);

}
