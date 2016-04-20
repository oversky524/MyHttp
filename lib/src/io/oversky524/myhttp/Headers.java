package io.oversky524.myhttp;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gaochao on 2016/4/19.
 */
public class Headers {
    private TreeMap<String,Object> mHeaders = new TreeMap<>();

    public Headers header(@NotNull String header, Object value){
        header = header.toLowerCase();
        if(header.equals("content-type") || header.equals("content-length")){
            throw new RuntimeException("header " + header + " should be added by a body");
        }
        mHeaders.put(header, value);
        return this;
    }

    public Headers headers(@NotNull Request.Body body) throws IOException {
        mHeaders.put(HttpUtils.Header.CONTENT_LENGTH, body.getContentLength());
        mHeaders.put(HttpUtils.Header.CONTENT_TYPE, body.getContentType());
        return this;
    }

    public void write(@NotNull OutputStream outputStream) throws IOException {
        final String newLine = "\r\n";
        for(Map.Entry<String, Object> entry : mHeaders.entrySet()){
            outputStream.write(entry.getKey().getBytes());
            outputStream.write(": ".getBytes());
            outputStream.write(entry.getValue().toString().getBytes());
            outputStream.write(newLine.getBytes());
        }
    }

    public Object header(@NotNull String header){ return mHeaders.get(header); }

    public static Headers of(@NotNull String header, Object...args){
        if(args.length < 1) throw new RuntimeException("There is a header value at least!");

        Headers headers = new Headers();
        headers.header(header, args[0]);
        for(int i=1; i<args.length; i+=2){
            header = (String)args[i];
            if(header == null) throw new RuntimeException("Header can't be null!");
            headers.header(header, args[i+1]);
        }
        return headers;
    }
}
