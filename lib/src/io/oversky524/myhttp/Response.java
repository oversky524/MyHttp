package io.oversky524.myhttp;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by gaochao on 2016/4/6.
 */
public class Response {
    private int mStatusCode = Integer.MIN_VALUE;
    private String mVersion;
    private Throwable mErrorThrowable;
    private Headers mHeaders = new Headers(true);
    private InputStream mInputStream;
    private byte[] mByteBody;

    Response(){}

    void setVersion(String version){ mVersion = version; }

    public boolean isNonhttpError(){ return mErrorThrowable != null; }

    void setErrorThrowable(Throwable errorThrowable){ mErrorThrowable = errorThrowable; }

    public int statusCode(){ return mStatusCode; }

    void setStatusCode(int code){ mStatusCode = code; }

    public Throwable getErrorThrowable(){ return mErrorThrowable; }

    void putHeader(String header, String value){ mHeaders.header(header, value); }

    void setInputStream(InputStream inputStream){ mInputStream = inputStream; }

    public Headers headers(){ return mHeaders; }

    public byte[] bytesBody() throws IOException {
        if(mErrorThrowable != null){
            mErrorThrowable.printStackTrace();
            return mErrorThrowable.getMessage().getBytes();
        }
        try {
            if(mByteBody == null) {
                long length = Long.valueOf(mHeaders.header(HttpUtils.Header.CONTENT_LENGTH));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                BufferedOutputStream outputStream = new BufferedOutputStream(byteArrayOutputStream);
                BufferedInputStream inputStream = new BufferedInputStream(getInputStreamBody());
                StreamUtils.copy(inputStream, false, outputStream, false, length);
                byte[] body = byteArrayOutputStream.toByteArray();
                outputStream.close();
                mByteBody = body;
                inputStream.close();
            }
        }catch (Throwable e){
            e.printStackTrace();
            mByteBody = "".getBytes();
        }
        return mByteBody;
    }

    public String stringBody() throws IOException { return new String(bytesBody()); }

    public byte[] body() throws IOException { return bytesBody(); }

    public InputStream getInputStreamBody(){ return new BufferedInputStream(mInputStream); }

    public boolean isInvalide(){ return mStatusCode == Integer.MIN_VALUE; }
}
