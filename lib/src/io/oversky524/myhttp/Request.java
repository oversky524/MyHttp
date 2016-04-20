package io.oversky524.myhttp;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by gaochao on 2016/4/6.
 */
public class Request {
    private static final byte[] CRLF = {'\r', '\n'};
    private static final byte[] COLONSPACE = {':', ' '};
    private static final byte[] DASHDASH = {'-', '-'};

    private String mHost = "";
    private int mPort = -1;
    private Body mBody;
    private String mUrl;
    private String mMethod;
    private String mPath;
    private Headers mHeaders;

    private Request(Builder builder) {
        try {
            mUrl = builder.mUrl;
            mMethod = builder.mMethod;
            URL url = new URL(builder.mUrl);
            mPath = url.getPath();
            mPort = getPort(url);
            mHost = url.getHost();
            mBody = builder.mBody;
            mHeaders = builder.mHeaders;
            if (mHeaders.header("connection") == null) mHeaders.header("connection", "keep-alive");
            if (mHeaders.header("host") == null) mHeaders.header("host", mHost);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    public void writeRequestLineAndHeaders(OutputStream outputStream) throws IOException {
        outputStream.write(mMethod.getBytes());
        outputStream.write(' ');
        outputStream.write(mPath.getBytes());
        final byte[] version = {' ', 'H', 'T', 'T', 'P', '/', '1', '.', '1'};
        outputStream.write(version);
        outputStream.write(CRLF);

        mHeaders.write(outputStream);

        outputStream.write(CRLF);
    }

    private static int getPort(URL url) {
        String scheme = url.getProtocol();
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new RuntimeException("scheme " + scheme + " is illegal");
        }
        int port = url.getPort();
        if (port == -1) {
            if (scheme.equalsIgnoreCase("http")) {
                port = 80;
            } else {
                port = 443;
            }
        }
        return port;
    }

    public Headers getHeaders() { return mHeaders; }

    public Body getBody() { return mBody; }

    public String getHost() { return mHost; }

    public int getPort() { return mPort; }

    @Override
    public int hashCode() {
        return mUrl.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;

        if (((Request) obj).mUrl.equals(mUrl)) {
            return true;
        } else {
            return false;
        }
    }

    public static class Builder {
        private String mUrl;
        private String mMethod;
        private Body mBody;
        private Headers mHeaders = new Headers();

        public Builder(String url) {
            mUrl = url;
        }

        public Builder addHeader(@NotNull String header, @NotNull Object value) {
            mHeaders.header(header, value);
            return this;
        }

        public Builder method(@NotNull String method, @Nullable Body body) throws IOException {
            mMethod = method;
            switch (method) {
                case HttpUtils.Method.HEAD:
                case HttpUtils.Method.GET:
                case HttpUtils.Method.OPTIONS:
                    if (body != null) throw new RuntimeException("method " + method + " shouldn't have any body");
                    break;

                default:
                    if (body == null) throw new RuntimeException("method " + method + " must have a body");
                    mHeaders.headers(body);
                    mBody = body;
            }
            return this;
        }

        public Request build() {
            return new Request(this);
        }
    }

    public interface Body {
        String getContentType();

        long getContentLength() throws IOException;

        void write(OutputStream outputStream) throws IOException;
    }

    private static class ByteBody implements Body{
        private String mContentType;
        private byte[] mBody;

        public ByteBody(@NotNull String contentType, @NotNull byte[] bytes){
            if(contentType == null || contentType.length() < 1){
                throw new RuntimeException("content type can't be empty!");
            }
            if(bytes == null) throw new NullPointerException("bytes can't be null!");

            mContentType = contentType;
            mBody = bytes;
        }

        @Override
        public String getContentType() { return mContentType; }

        @Override
        public long getContentLength() throws IOException { return mBody != null ? mBody.length : 0; }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            if(mBody != null) outputStream.write(mBody);
        }
    }

    public static class FormBody implements Body {
        private StringBuffer mSb = new StringBuffer();
        private byte[] mBody;

        public void add(@NotNull String key, String value) {
            if (mSb.length() > 0) mSb.append('&');
            if (key == null || key.length() < 1) throw new RuntimeException("form key can't be empty");
            try {
                mSb.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getContentType() {
            return HttpUtils.ContentType.FORM_URLENCODED;
        }

        @Override
        public long getContentLength() {
            body();
            return mBody.length;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            body();
            outputStream.write(mBody);
        }

        private void body() {
            if (mBody == null) mBody = mSb.toString().getBytes();
        }

        public static FormBody build(String... kvPairs) {
            int length = kvPairs.length;
            if ((length & 1) == 1) throw new RuntimeException("kvPairs should be key-value pairs");
            FormBody body = new FormBody();
            for (int i = 0; i < length; i += 2) {
                String key = kvPairs[i];
                if (key == null) continue;
                String value = kvPairs[i + 1];
                if (value == null) value = "";
                body.add(key, value);
            }
            return body;
        }
    }

    public static class FileBody implements Body{
        private File mFile;
        private String mContentType;

        public FileBody(@NotNull String contentType, @NotNull File file){
            if(contentType == null || contentType.length() < 1){
                throw new RuntimeException("content type can't be empty!");
            }
            if(file == null) throw new NullPointerException("file can't be null!");

            mContentType = contentType;
            mFile = file;
        }

        @Override
        public String getContentType() { return mContentType; }

        @Override
        public long getContentLength() throws IOException { return mFile.length(); }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            StreamUtils.copy(new FileInputStream(mFile), true, outputStream, false);
        }
    }

    public static class MultipartBody implements Body {
        private String mBoundary = UUID.randomUUID().toString();
        private ArrayList<Object> mParts = new ArrayList<>();
        private long mContentLength = -1L;

        @Override
        public String getContentType() {
            return "multipart/mixed; boundary=" + mBoundary;
        }

        @Override
        public long getContentLength() throws IOException {
            if (mContentLength != -1L) return mContentLength;
            return mContentLength = count(null);
        }

        @Override
        public void write(OutputStream outputStream) throws IOException { count(outputStream); }

        private long count(OutputStream outputStream) throws IOException {
            boolean count = false;
            if (outputStream == null){
                outputStream = new NullOutputStream();
                count = true;
            }

            byte[] boundary = mBoundary.getBytes();
            for (int i = 0; i < mParts.size(); i += 2) {
                outputStream.write(DASHDASH);
                outputStream.write(boundary);
                outputStream.write(CRLF);

                Headers headers = (Headers) mParts.get(i);
                if (headers == null) headers = new Headers();
                Body body = (Body) mParts.get(i + 1);
                headers.headers(body);
                headers.write(outputStream);

                outputStream.write(CRLF);

                body.write(outputStream);

                outputStream.write(CRLF);
            }

            outputStream.write(DASHDASH);
            outputStream.write(boundary);
            outputStream.write(DASHDASH);
            outputStream.write(CRLF);

            return count ? ((NullOutputStream)outputStream).writtenSize() : 0L;
        }

        public MultipartBody addPart(Headers headers, @NotNull Body body) {
            if (body == null) throw new RuntimeException("body can't be null!");
            mParts.add(headers);
            mParts.add(body);
            return this;
        }

        public MultipartBody addFormPart(@NotNull String name, String fileName, @NotNull Body body) {
            if (name == null) throw new NullPointerException("name can't be null!");
            StringBuilder sb = new StringBuilder();
            try {
                sb.append("form-data; name=").append(URLEncoder.encode(name, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (fileName != null) sb.append("; filename=").append(fileName);
            return addPart(Headers.of(HttpUtils.Header.CONTENT_DISPOSITION, sb.toString()), body);
        }

        public MultipartBody addFormPart(@NotNull String name, @NotNull Body body) { return addFormPart(name, null, body); }
    }

    public static Body create(@NotNull String contentType, @NotNull String content){
        return new ByteBody(contentType, content.getBytes());
    }

    public static Body create(@NotNull String contentType, @NotNull byte[] content){
        return new ByteBody(contentType, content);
    }

    public static Body createJsonBody(@NotNull String body){
        return new ByteBody(HttpUtils.ContentType.JSON, body.getBytes());
    }
}
