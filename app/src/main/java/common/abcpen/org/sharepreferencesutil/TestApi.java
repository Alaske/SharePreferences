package common.abcpen.org.sharepreferencesutil;

import android.content.Context;

import com.abcpen.zc.sp.annotaion.SPData;
import com.abcpen.zc.sp.annotaion.SPItem;

/**
 * Created by zhaocheng on 2018/4/28.
 */

@SPData(mode = Context.MODE_PRIVATE)
@SPItem(key = "token",valueType = String.class)
public class TestApi {
    private String userData;

}


