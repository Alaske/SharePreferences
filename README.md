# SharePreferencesUtil
>- 主要为了减少sharePreferences操作的繁琐性
使用SharePreferencesUtil之后所有的put get 操作只需要一个注解搞定 确实不行那就两个

>- 核心实现  利用android AbstractProcessor （注解解析器）生成操作sharePreferences代码


### 注解类介绍

```java
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface SPData {

    String name() default "sp_data";

    int mode();
}


```
>  SPData主要生成SharePreferencesUtil对象,注意SPData 只能存在一个 多个util会直接忽略


```java
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface SPItem {


    String key() default "";

    Class valueType() default String.class;


    Class<? extends ISPConvert> convert() default DefaultGsonConvert.class;
}

```


 ·| ·
---|---
key | 对应SP中 put get 中的 Key 值（如果注解定义在变量中不写key 默认值为变量名称 如果定义在class中不写默认为className）
valueType | 此字段主要为了不想定义变量的一些put set操作 直接指定put set 的类型 类型可以是object 这边默认会转成json string存入sp中
convert | 可自定义数据类型转换成sp所需要的String即可 具体参考DefaultGsonConvert 


### simple 
>- 注解代码
```java
/**
 * Created by zhaocheng on 2018/4/28.
 */

@SPData(mode = Context.MODE_PRIVATE)
public class TestApi {
    @SPItem
    private String userData;

    @SPItem(convert = DefaultGsonConvert.class)
    public static class UserMo {
        private String id;
        private String name;

    }

}
```
>- 对应生成代码

```java
public class SharePreferencesUtil {
  private static Context mContext;

  private SharePreferencesUtil() {
  }

  SharedPreferences getSharedPreferences() {
    return mContext.getSharedPreferences("sp_data",0);
  }

  public static void init(Context context) {
    mContext = context.getApplicationContext();
  }

  public static SharePreferencesUtil getInstance() {
    return SharePreferencesHolder.holder;
  }

  public void setUserMo(TestApi.UserMo param) {
    getSharedPreferences().edit().putString("UserMo",new DefaultGsonConvert().convertToObject(param));
  }

  public TestApi.UserMo getUserMo() {
    String data= getSharedPreferences().getString("UserMo","");
    return new DefaultGsonConvert<TestApi.UserMo>().unConvertData(data,TestApi.UserMo.class);
  }

  public void setUserData(String userData) {
    getSharedPreferences().edit().putString("userData",userData).apply();
  }

  public String getUserData() {
    return getSharedPreferences().getString("userData","");
  }

  static class SharePreferencesHolder {
    static final SharePreferencesUtil holder = new SharePreferencesUtil();
  }
}
```

>- 自定义key 以及数据类型

```java
@SPItem(key = "token",valueType = String.class)
public class TestApi {
    private String userData;

}


```

>- 对应生成util类

```java
@SuppressWarnings("unchecked")
public class SharePreferencesUtil {
  private static Context mContext;

  private SharePreferencesUtil() {
  }

  SharedPreferences getSharedPreferences() {
    return mContext.getSharedPreferences("sp_data",0);
  }

  public static void init(Context context) {
    mContext = context.getApplicationContext();
  }

  public static SharePreferencesUtil getInstance() {
    return SharePreferencesHolder.holder;
  }

  public void setToken(String token) {
    getSharedPreferences().edit().putString("token",token).apply();
  }

  public String getToken() {
    return getSharedPreferences().getString("token","");
  }

  static class SharePreferencesHolder {
    static final SharePreferencesUtil holder = new SharePreferencesUtil();
  }
}
```

### 使用方式
```java

        //初始化一次即可
        SharePreferencesUtil.init(this);


        //set操作
        SharePreferencesUtil.getInstance().setToken("test token");

        //get 操作
        String token = SharePreferencesUtil.getInstance().getToken();
        
```