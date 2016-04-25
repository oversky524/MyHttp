package io.oversky524.myhttp;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.CookieHandler;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by gaochao on 2016/4/6.
 */
public class MyHttpClient {
    private static final boolean DEBUG = true;
    private ConnectionPool mPool = new ConnectionPool();
    private CookieHandler mCookieHandler;
    private SSLSocketFactory mSslSocketFactory;

    private MyHttpClient(Builder builder){
        mCookieHandler = builder.cookieHandler;
        mSslSocketFactory = builder.sslSocketFactory;
    }

    public SSLSocketFactory sslSocketFactory(){
        if(null == mSslSocketFactory){
            mSslSocketFactory = getDefaultSslSocketFactory();
        }
        return mSslSocketFactory;
    }

    private static SSLSocketFactory getDefaultSslSocketFactory(){
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            try {
                sslContext = SSLContext.getInstance("SSL");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
        }
        return sslContext != null ? sslContext.getSocketFactory() : (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public Response execute(Request request){
        Response response;
        while (true){
            Connection connection = mPool.getConnection(request);
            System.out.println(connection);
            response = connection.put(request);
            try{
                Socket socket = connection.getSocket();
                writeRequest(request, socket);
                parseResponse(socket, response, request.uri());
                if(DEBUG) System.out.println(response.headers());
                response.setInputStream(new ReleaseConnectionInputStream(connection, request));
                break;
            } catch (IOException e) {
                e.printStackTrace();
                response.setErrorThrowable(e);
                break;
            } catch (RemoveConnectionRetryException e){
                e.printStackTrace();
                mPool.removeConnection(request);
            }
        }
        return response;
    }

    private void writeRequest(Request request, Socket socket) throws IOException {
        if(null != mCookieHandler) HeaderUtils.addCookies(request, mCookieHandler);
        BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        request.writeRequestLineAndHeaders(outputStream);
        Request.Body body = request.body();
        if(body != null) body.write(outputStream);
        outputStream.flush();
    }

    private void parseResponse(Socket socket, Response response, URI uri) throws IOException {
        PushbackInputStream inputStream = new PushbackInputStream(socket.getInputStream());
        int readByte = 0;
        boolean firstLine = true;
        StringBuffer sb = new StringBuffer();

        while ((readByte = inputStream.read()) != -1){
            if(readByte == '\r'){//正常情况下，一个header应该以\r\n结尾
                inputStream.read();//consume '\n'
                if(firstLine){
                    firstLine = false;
                    parseResponseLine(sb.toString(), response);
                }else{
                    HeaderUtils.parseHeader(sb.toString(), response);
                }
                sb.setLength(0);
                readByte = inputStream.read();
                if(readByte == '\r'){
                    inputStream.read();//consume '\n'
                    break;
                }else{
                    inputStream.unread(readByte);//push back
                }
            }else if(readByte == '\n'){//有时，一个header也以\n结尾
                if(firstLine){
                    firstLine = false;
                    parseResponseLine(sb.toString(), response);
                }else{
                    HeaderUtils.parseHeader(sb.toString(), response);
                }
                sb.setLength(0);
                readByte = inputStream.read();
                if(readByte == '\n'){
                    break;
                }else{
                    inputStream.unread(readByte);//push back
                }
            }else{
                sb.append((char)readByte);
            }
        }
        if(response.isInvalide()) throw new RemoveConnectionRetryException();
        if(null != mCookieHandler) mCookieHandler.put(uri, response.headers().toMap());
    }

    private static void parseResponseLine(String line, Response response){
        if(DEBUG) System.out.println(line);
        int index = line.indexOf(' ');
        String scheme = line.substring(0, index);
        response.setVersion(scheme.substring(scheme.indexOf('/') + 1));
        while (line.charAt(++index) == ' ');
        String code = line.substring(index, line.indexOf(' ', index));
        response.setStatusCode(Integer.valueOf(code));
    }

    public static class Builder{
        private CookieHandler cookieHandler;
        private SSLSocketFactory sslSocketFactory;

        public Builder cookieHandler(CookieHandler handler){
            cookieHandler = handler;
            return this;
        }

        public MyHttpClient build(){ return new MyHttpClient(this); }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory){
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }
    }

    private static class ReleaseConnectionInputStream extends FilterInputStream{
        private Connection mConnection;
        private Request mRequest;

        ReleaseConnectionInputStream(Connection connection, Request request) throws IOException {
            super(connection.getSocket().getInputStream());
            mConnection = connection;
            mRequest = request;
        }

        @Override
        public int read() throws IOException {
            if(mConnection == null) return -1;
            return release(super.read());
        }

        @Override
        public int read(byte[] b) throws IOException {
            if(mConnection == null) return -1;
            return release(super.read(b));
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if(mConnection == null) return -1;
            return release(super.read(b, off, len));
        }

        private int release(int result){
            if(result == -1){
                System.out.println("result=" + result);
                mConnection.release(mRequest);
                mConnection = null;
            }
            return result;
        }

        @Override
        public void close() throws IOException {
            release(-1);
        }
    }
}
