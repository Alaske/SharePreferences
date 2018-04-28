package com.abcpen.zc.convert;

import com.google.gson.Gson;

/**
 * Created by zhaocheng on 2018/4/28.
 */

public class DefaultGsonConvert<T> implements ISPConvert<T> {

    private Gson gson = new Gson();

    @Override
    public String convertToObject(T t) {
        return gson.toJson(t);
    }

    @Override
    public T unConvertData(String data, Class<T> t) {
        return gson.fromJson(data, t);
    }
}
