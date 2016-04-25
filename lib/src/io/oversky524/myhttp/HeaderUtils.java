package io.oversky524.myhttp;

import java.io.IOException;
import java.net.CookieHandler;
import java.util.*;

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

    public static void addCookies(Request request, CookieHandler cookieHandler){
        Headers headers = request.headers();
        try {
            Map<String, List<String>> cookies = cookieHandler.get(request.uri(), headers.toMap());
            ArrayList<String> all = new ArrayList<>();
            for(Map.Entry<String, List<String>> entry : cookies.entrySet()){
                String key = entry.getKey().toLowerCase();
                if(key.equals(HttpUtils.Header.COOKIE) || key.equals(HttpUtils.Header.COOKIE2)){
                    List<String> value = entry.getValue();
                    for(int i=0, size = value.size(); i<size; ++i) all.add(value.get(i));
                }
            }
            StringBuilder sb = new StringBuilder();
            for(int i=0, size = all.size(); i<size; ++i){
                sb.append(all.get(i));
                if(i < size - 1) sb.append(";");
            }
            headers.header(HttpUtils.Header.COOKIE, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
