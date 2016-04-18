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
    private LinkedHashMap<String, String> mHeaders = new LinkedHashMap<>();
    private InputStream mInputStream;
    private byte[] mByteBody;

    Response(){}

    void setVersion(String version){ mVersion = version; }

    public boolean isNonhttpError(){ return mErrorThrowable != null; }

    void setErrorThrowable(Throwable errorThrowable){ mErrorThrowable = errorThrowable; }

    public int getStatusCode(){ return mStatusCode; }

    void setStatusCode(int code){ mStatusCode = code; }

    public Throwable getErrorThrowable(){ return mErrorThrowable; }

    void putHeader(String header, String value){ mHeaders.put(header, value); }

    void setInputStream(InputStream inputStream){ mInputStream = inputStream; }

    public Map<String, String> getHeaders(){ return mHeaders; }

    public byte[] getBytesBody() throws IOException {
        if(mByteBody == null) {
            long length = Long.valueOf(mHeaders.get(HttpUtils.Header.CONTENT_LENGTH));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            BufferedOutputStream outputStream = new BufferedOutputStream(byteArrayOutputStream);
            BufferedInputStream inputStream = new BufferedInputStream(getInputStreamBody());
            StreamUtils.copy(inputStream, false, outputStream, false, length);
            byte[] body = byteArrayOutputStream.toByteArray();
            outputStream.close();
            inputStream.close();
            mByteBody = body;
        }
        return mByteBody;
    }

    public InputStream getInputStreamBody(){ return new BufferedInputStream(mInputStream); }

    public boolean isInvalide(){ return mStatusCode == Integer.MIN_VALUE; }
}
