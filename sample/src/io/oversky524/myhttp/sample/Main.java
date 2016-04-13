package io.oversky524.myhttp.sample;

import io.oversky524.myhttp.*;

import java.io.*;

import static io.oversky524.myhttp.sample.OriginalSocketHttp.*;

/**
 * Created by gaochao on 2016/4/5.
 */
public class Main {
    private static MyHttpClient client = new MyHttpClient.Builder().build();
    private static final String sUrl = "http://121.41.111.181/shuxin/index.php/qaInfo/question";
    public static void main(String[] args) throws IOException, InterruptedException {
        getQuestions();
//        Thread.sleep(6 * 60 * 1000);
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
}
