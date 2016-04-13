package io.oversky524.myhttp;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by gaochao on 2016/2/17.
 */
public class HttpUtils {
    public interface Header{
        String CONTENT_LENGTH = "content-length";
    }
    private HttpUtils(){ throw new AssertionError("No instances"); }

    public static final String METHOD_POST = "POST";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_TRACE = "TRACE";
    public static final String METHOD_CONNECT = "CONNECT";

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static String getJsonBody(Object ... keyValuePairs){
        final int len = keyValuePairs.length;
//        CheckUtils.checkState((len & 1) == 1, "Key and Value have to be paired");
        JSONObject jsonObject = new JSONObject();
        for(int i=0; i<len; i+=2){
            Object key = keyValuePairs[i];
            if(key == null) continue;
            jsonObject.put(key.toString(), keyValuePairs[i + 1]);
        }
        return jsonObject.toJSONString();
    }
}
