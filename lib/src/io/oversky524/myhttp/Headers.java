package io.oversky524.myhttp;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by gaochao on 2016/4/19.
 */
public class Headers {
    private ArrayList<String> mHeaders = new ArrayList<>();
    private boolean mBelongtoResponse;

    public Headers(boolean response){ mBelongtoResponse = response; }

    public Headers(){ this(false); }

    public Headers header(@NotNull String header, Object value){
        header = header.toLowerCase();
        if(!mBelongtoResponse && (header.equals("content-type") || header.equals("content-length"))){
            throw new RuntimeException("header " + header + " should be added by a body");
        }
        mHeaders.add(header);
        mHeaders.add(value.toString());
        return this;
    }

    public Headers headers(@NotNull Request.Body body) throws IOException {
        mHeaders.add(HttpUtils.Header.CONTENT_LENGTH);
        mHeaders.add(String.valueOf(body.getContentLength()));
        mHeaders.add(HttpUtils.Header.CONTENT_TYPE);
        mHeaders.add(body.getContentType());
        return this;
    }

    public void write(@NotNull OutputStream outputStream) throws IOException {
        final byte[] newLine = { '\r', '\n'};
        final byte[] separator = {':', ' '};
        ArrayList<String> headers = mHeaders;
        for(int i=0, size=headers.size(); i<size; i+=2){
            outputStream.write(headers.get(i).getBytes());
            outputStream.write(separator);
            outputStream.write(headers.get(i+1).getBytes());
            outputStream.write(newLine);
        }
    }

    public String header(@NotNull String header){
        ArrayList<String> headers = mHeaders;
        for(int i=0, size=headers.size(); i<size; i+=2){
            if(header.equalsIgnoreCase(headers.get(i))){
                return headers.get(i+1);
            }
        }
        return null;
    }

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

    public Map<String, List<String>> toMap(){
        Map<String, List<String>> result = new HashMap<>();
        ArrayList<String> headers = mHeaders;
        for(int i=0, size=headers.size(); i<size; i+=2){
            List<String> value = new ArrayList<>();
            value.add(headers.get(i+1));
            result.put(headers.get(i), value);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> headers = mHeaders;
        for(int i=0, size=headers.size(); i<size; i+=2){
            sb.append(headers.get(i)).append(": ").append(headers.get(i+1)).append('\n');
        }
        return sb.toString();
    }
}
