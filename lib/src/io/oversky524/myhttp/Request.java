package io.oversky524.myhttp;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by gaochao on 2016/4/6.
 */
public class Request {
    private String mHost = "";
    private int mPort = -1;
    private String mEssential;
    private byte[] mBody;

    private Request(Builder builder){
        try {
            URL url = new URL(builder.mUrl);
            mPort = getPort(url);
            mHost = url.getHost();
            Map<String, Object> headers = builder.mHeaders;
//            if(!headers.containsKey("connection")) headers.put("connection", "keep-alive");
            mEssential = buildHttpHeaders(builder.mMethod, url.getPath(), headers, mHost);
            Body body = builder.mBody;
            if(body != null) mBody = body.getBody();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static int getPort(URL url){
        String scheme = url.getProtocol();
        if(!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")){
            throw new RuntimeException("scheme " + scheme + " is illegal");
        }
        int port = url.getPort();
        if(port == -1) {
            if (scheme.equalsIgnoreCase("http")) {
                port = 80;
            }else{
                port = 443;
            }
        }
        return port;
    }

    private static String buildHttpHeaders(String method, String path, Map<String, Object> headers, String host){
        final String newLine = "\r\n";
        StringBuffer sb = new StringBuffer();
        sb.append(method).append(" ").append(path).append(" HTTP/1.0").append(newLine);
        if(!headers.containsKey("host")) headers.put("Host", host);
        Set<String> keySet = headers.keySet();
        for(String key : keySet){
            if(key == null) continue;
            String value = headers.get(key).toString();
            sb.append(key).append(": ").append(value).append(newLine);
        }
        sb.append(newLine);
        return sb.toString();
    }

    public String getHeaders(){ return mEssential; }

    public byte[] getBody(){ return mBody; }

    public String getHost(){ return mHost; }

    public int getPort(){ return mPort; }

    public static class Builder{
        private String mUrl;
        private String mMethod;
        private Body mBody;
        private TreeMap<String,Object> mHeaders = new TreeMap<>();

        public Builder(String url){ mUrl = url; }

        public Builder addHeader(@NotNull String header, @NotNull Object value){
            header = header.toLowerCase();
            if(header.equals("content-type") || header.equals("content-length")){
                throw new RuntimeException("header " + header + " should be added by a body");
            }
            mHeaders.put(header, value);
            return this;
        }

        public Builder method(@NotNull String method, @Nullable Body body){
            mMethod = method;
            switch (method){
                case HttpUtils.METHOD_HEAD:
                case HttpUtils.METHOD_GET:
                case HttpUtils.METHOD_OPTIONS:
                    if(body != null) throw new RuntimeException("method " + method + " shouldn't have any body");
                    break;

                default:
                    if(body == null) throw new RuntimeException("method " + method + " must have a body");
                    mHeaders.put("Content-Type", body.getContentType());
                    mHeaders.put("Content-Length", body.getContentLength());
                    mBody = body;
            }
            return this;
        }

        public Request build(){ return new Request(this); }
    }

    public interface Body{
        byte[] getBody();
        String getContentType();
        int getContentLength();
    }

    public static class FormBody implements Body{
        private StringBuffer mSb = new StringBuffer();
        private byte[] mBody;

        public void add(@NotNull String key, String value){
            if(mSb.length() > 0) mSb.append('&');
            if(key == null || key.length() < 1) throw new RuntimeException("form key can't be empty");
            try {
                mSb.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] getBody() {
            body();
            return mBody;
        }

        @Override
        public String getContentType() { return HttpUtils.CONTENT_TYPE_FORM_URLENCODED; }

        @Override
        public int getContentLength() {
            body();
            return mBody.length;
        }

        private void body(){ if(mBody == null) mBody = mSb.toString().getBytes(); }

        public static FormBody build(String...kvPairs){
            int length = kvPairs.length;
            if((length & 1) == 1) throw new RuntimeException("kvPairs should be key-value pairs");
            FormBody body = new FormBody();
            for(int i=0; i<length; i+=2){
                String key = kvPairs[i];
                if(key == null) continue;
                String value = kvPairs[i+1];
                if(value == null) value = "";
                body.add(key, value);
            }
            return body;
        }
    }

    public static class JsonBody implements Body{
        private byte[] mBody;

        public JsonBody(@NotNull String body){ mBody = body.getBytes(); }

        public JsonBody(@NotNull byte[] body){ mBody = body; }

        @Override
        public byte[] getBody() { return mBody; }

        @Override
        public String getContentType() { return HttpUtils.CONTENT_TYPE_JSON; }

        @Override
        public int getContentLength() { return mBody.length; }

        public static JsonBody build(Object...kvPairs){
            int length = kvPairs.length;
            if((length & 1) == 1) throw new RuntimeException("kvPairs should be key-value pairs");
            JSONObject jsonObject = new JSONObject();
            for(int i=0; i<length; i+=2){
                Object key = kvPairs[i];
                if(key == null) continue;
                Object value = kvPairs[i+1];
                if(value == null) value = "";
                jsonObject.put((String)key, value);
            }
            return new JsonBody(jsonObject.toString());
        }
    }
}
