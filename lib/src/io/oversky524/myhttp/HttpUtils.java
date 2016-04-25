package io.oversky524.myhttp;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by gaochao on 2016/2/17.
 */
public class HttpUtils {
    public interface Header{
        String CONTENT_LENGTH = "content-length";
        String CONTENT_TYPE = "content-type";
        String CONTENT_DISPOSITION = "content-disposition";

        String SET_COOKIE = "set-cookie";
        String SET_COOKIE2 = "set-cookie2";
        String COOKIE = "cookie";
        String COOKIE2 = "cookie2";
    }
    private HttpUtils(){ throw new AssertionError("No instances"); }

    public interface Method{
        String POST = "POST";
        String PATCH = "PATCH";
        String PUT = "PUT";
        String DELETE = "DELETE";
        String GET = "GET";
        String OPTIONS = "OPTIONS";
        String HEAD = "HEAD";
        String TRACE = "TRACE";
        String CONNECT = "CONNECT";
    }

    public interface ContentType{
        String JSON = "application/json";
        String MULTIPART = "multipart/mixed";
        String FORM_URLENCODED = "application/x-www-form-urlencoded";
    }

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
