package io.oversky524.myhttp;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Created by gaochao on 2016/4/6.
 */
public class MyHttpClient {
    private static final boolean DEBUG = false;
    private ConnectionPool mPool = new ConnectionPool();

    private MyHttpClient(Builder builder){
    }

    public Response execute(Request request){
        final String host = request.getHost();
        final int port = request.getPort();
        Response response;
        while (true){
            Connection connection = mPool.getConnection(host, port);
            System.out.println(connection);
            response = connection.put(request);
            try{
                Socket socket = connection.getSocket();
                writeRequest(request, socket);
                parseResponse(socket, response);
                if(DEBUG){
                    Map<String, String> headers = response.getHeaders();
                    for (String key : headers.keySet()){
                        System.out.println(key + ": " + headers.get(key));
                    }
                }
                response.setInputStream(new ReleaseConnectionInputStream(connection, request));
                break;
            } catch (IOException e) {
                e.printStackTrace();
                response.setErrorThrowable(e);
                break;
            } catch (RemoveConnectionRetryException e){
                e.printStackTrace();
                mPool.removeConnection(host, port);
            }
        }
        return response;
    }

    private static void writeRequest(Request request, Socket socket) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        request.writeRequestLineAndHeaders(outputStream);
        Request.Body body = request.getBody();
        if(body != null) body.write(outputStream);
        outputStream.flush();
    }

    private static void parseResponse(Socket socket, Response response) throws IOException {
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
    }

    private static void parseResponseLine(String line, Response response){
        int index = line.indexOf(' ');
        String scheme = line.substring(0, index);
        response.setVersion(scheme.substring(scheme.indexOf('/') + 1));
        while (line.charAt(++index) == ' ');
        String code = line.substring(index, line.indexOf(' ', index));
        response.setStatusCode(Integer.valueOf(code));
    }

    public static class Builder{

        public MyHttpClient build(){ return new MyHttpClient(this); }
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
