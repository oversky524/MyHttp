package io.oversky524.myhttp;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Created by gaochao on 2016/4/6.
 */
public class MyHttpClient {

    private MyHttpClient(Builder builder){
    }

    public Response execute(Request request){
        Response response = new Response();
        final String host = request.getHost();
        final int port = request.getPort();
        try(Socket socket = new Socket(host, port)){
            writeRequest(request, socket);
            parseResponse(socket, response);
            if(DEBUG){
                Map<String, String> headers = response.getHeaders();
                for (String key : headers.keySet()){
                    System.out.println(key + ": " + headers.get(key));
                }
            }
            response.getBytesBody();
        } catch (IOException e) {
            e.printStackTrace();
            response.setErrorThrowable(e);
        }
        return response;
    }

    private static void writeRequest(Request request, Socket socket) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        outputStream.write(request.getHeaders().getBytes());
        outputStream.write(request.getBody());
        outputStream.flush();
    }

    private static void parseResponse(Socket socket, Response response) throws IOException {
        PushbackInputStream inputStream = new PushbackInputStream(socket.getInputStream());
        int readByte;
        boolean firstLine = true;
        StringBuffer sb = new StringBuffer();
        while ((readByte = inputStream.read()) != -1){
            if(readByte == '\r'){
                inputStream.read();//consume '\n'
                if(firstLine){
                    firstLine = false;
                    parseResponseLine(sb.toString(), response);
                }else{
                    parseHeader(sb.toString(), response);
                }
                sb.setLength(0);
                readByte = inputStream.read();
                if(readByte == '\r'){
                    inputStream.read();//consume '\n'
                    break;
                }else{
                    inputStream.unread(readByte);//push back
                }
            }else{
                sb.append((char)readByte);
            }
        }
        response.setInputStream(inputStream);
    }

    private static void parseResponseLine(String line, Response response){
        int index = line.indexOf(' ');
        String scheme = line.substring(0, index);
        response.setVersion(scheme.substring(scheme.indexOf('/') + 1));
        while (line.charAt(++index) == ' ');
        String code = line.substring(index, line.indexOf(' ', index));
        response.setStatusCode(Integer.valueOf(code));
    }

    private static void parseHeader(String pair, Response response){
        int index = pair.indexOf(':');
        String key = pair.substring(0, index).trim();
        String value = pair.substring(index + 1).trim();
        response.putHeader(key, value);
    }

    private static final boolean DEBUG = true;

    public static class Builder{
        /*private ConnectionPool connectionPool;

        public Builder setConnectionPool(ConnectionPool pool){
            connectionPool = pool;
            return this;
        }*/

        public MyHttpClient build(){ return new MyHttpClient(this); }
    }
}
