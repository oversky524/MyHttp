package io.oversky524.myhttp;

/**
 * Created by gaochao on 2016/4/13.
 */
public class HeaderUtils {

    public static void parseHeader(String pair, Response response){
        int index = pair.indexOf(':');
        String key = pair.substring(0, index).trim().toLowerCase();
        String value = pair.substring(index + 1).trim();
        response.putHeader(key, value);
    }

}
