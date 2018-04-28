package common.abcpen.org.sharepreferencesutil;

import android.app.Application;

import com.zc.util.SharePreferencesUtil;

/**
 * Created by zhaocheng on 2018/4/28.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化一次即可
        SharePreferencesUtil.init(this);


        //set操作
        SharePreferencesUtil.getInstance().setToken("test token");

        //get 操作
        String token = SharePreferencesUtil.getInstance().getToken();
    }
}
