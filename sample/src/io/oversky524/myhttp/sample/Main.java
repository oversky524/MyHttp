package io.oversky524.myhttp.sample;

import io.oversky524.myhttp.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by gaochao on 2016/4/5.
 */
public class Main {
    private static MyHttpClient client = new MyHttpClient.Builder().build();
    private static final String sUrl = "http://121.41.111.181/shuxin/index.php/qaInfo/question";
    public static void main(String[] args) throws IOException, InterruptedException {
        getQuestions();
        Thread.sleep(6 * 60 * 1000);
        getQuestions();
    }

    private static void getQuestions() throws IOException {
        String body = HttpUtils.getJsonBody("uid", "0", "length", 1);
        System.out.println(new String(client.execute(new Request.Builder(sUrl)
                .method(HttpUtils.METHOD_POST, Request.FormBody.build("data", body,
                        "device", DEVICE_INFO,
                        "signature", sign(body, DEVICE_INFO)))
                .build()).getBytesBody()));
    }

    private static void getSmsCode(){
        String body = HttpUtils.getJsonBody("phone", "18628144638",
                "region", "86",
                "uid", "0");
        try {
            System.out.println(new String(client.execute(new Request.Builder("http://121.41.111.181/shuxin/index.php/smsInfo/smsCode")
                    .method(HttpUtils.METHOD_POST, Request.FormBody.build("data", body,
                            "device", DEVICE_INFO,
                            "signature", sign(body, DEVICE_INFO)))
                    .build()).getBytesBody()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void socket() throws MalformedURLException {
        URL url = new URL(sUrl);
        String scheme = url.getProtocol();
        int port = url.getPort();
        if(port == -1) {
            if (scheme.equalsIgnoreCase("http")) {
                port = 80;
            }else if(scheme.equalsIgnoreCase("https")){
                port = 443;
            }
        }
        try(Socket socket = new Socket(url.getHost(), port)){
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
            BufferedOutputStream outputStream = new BufferedOutputStream(buffer);
            writeBody(outputStream);
            outputStream.flush();
            byte[] body = buffer.toByteArray();
            String headers = buildHttpHeaders("POST", url.getPath(),
//                    "Host", "",
                    "Content-Type", "application/x-www-form-urlencoded",
                    "Content-Length", body.length);
            outputStream.close();

            outputStream = new BufferedOutputStream(socket.getOutputStream());
            outputStream.write(headers.getBytes());
            outputStream.write(body);
            outputStream.flush();
//            outputStream.close();

            buffer = new ByteArrayOutputStream(1024);
            outputStream = new BufferedOutputStream(buffer);
            BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
            byte[] temp = new byte[1024];
            int readSize = -1;
            while (true){
                readSize = inputStream.read(temp);
                if(readSize == -1) break;
                outputStream.write(temp, 0, readSize);
            }
            outputStream.flush();
            System.out.println(buffer.toString());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeBody(OutputStream outputStream){
        String body = HttpUtils.getJsonBody("uid", "0", "length", 1);
        StringBuffer sb = new StringBuffer();
        try {
            sb.append("data=").append(URLEncoder.encode(body, "UTF-8")).append("&");
            sb.append("device=").append(URLEncoder.encode(DEVICE_INFO, "UTF-8")).append("&");
            sb.append("signature=").append(sign(body, DEVICE_INFO));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            outputStream.write(sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String sign(String data, String device) {
        return DigestUtils.getSha1(data + device + "bf7c89d6a937ae877d9594d20eb714e7");
    }

    public static String buildHttpHeaders(String method, String path, Object...headers){
        final String newLine = "\r\n";
        StringBuffer sb = new StringBuffer();
        sb.append(method).append(" ").append(path).append(" HTTP/1.0").append(newLine);
        int length = headers.length;
        if((length & 1) != 0) throw new RuntimeException("http headers are key-value pairs");
        for (int i=0; i<headers.length; i+=2){
            String key = (String) headers[i];
            if(key == null) continue;
            String value = headers[i+1].toString();
            sb.append(key).append(": ").append(value).append(newLine);
        }
        sb.append(newLine);
        return sb.toString();
    }
    private static final String DEVICE_INFO = "{\"did\":\"{\\\"device_id\\\":\\\"866486024916656\\\",\\\"mac\\\":\\\"a0:86:c6:45:f2:4f\\\"}\",\"platform\":2,\"model\":\"device:HM2014112;brand:Xiaomi\",\"sys_ver\":\"4.4.4\",\"channel\":\"main\",\"app_ver\":\"v1.0.0\"}";
}
