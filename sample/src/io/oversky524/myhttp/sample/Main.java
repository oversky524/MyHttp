package io.oversky524.myhttp.sample;

import com.squareup.okhttp.*;
import io.oversky524.myhttp.*;
import io.oversky524.myhttp.Headers;
import io.oversky524.myhttp.Request;

import java.io.*;

import static io.oversky524.myhttp.sample.OriginalSocketHttp.*;

/**
 * Created by gaochao on 2016/4/5.
 */
public class Main {
    private static MyHttpClient client = new MyHttpClient.Builder().build();
    private static final String sUrl = "http://121.41.111.181/shuxin/index.php/qaInfo/question";

    public static void main(String[] args) {
        /*try {
            getQuestions();
            Thread.sleep(20 * 1000);
            getQuestions();
        }catch (Throwable e){
            e.printStackTrace();
        }*/
        multipartTest();
    }

    private static void multipartTest() {
        try {
            System.out.println(new String(client.execute(new Request.Builder("http://posttestserver.com/post.php")
                    .method(HttpUtils.Method.POST, new Request.MultipartBody()
                            .addPart(Headers.of("aa", "bb"),
                                    Request.createJsonBody(HttpUtils.getJsonBody("uid", "0", "length", 1)))
                            .addFormPart("myfile", "Android性能优化.doc",
                                    new Request.FileBody("application/msword", new File("d:/Android性能优化.doc"))))
                    .build())
                    .getBytesBody()));
            Thread.sleep(10 * 1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void getQuestions() throws IOException {
        String body = HttpUtils.getJsonBody("uid", "0", "length", 1);
        System.out.println(new String(client.execute(new Request.Builder(sUrl)
                .method(HttpUtils.Method.POST, Request.FormBody.build("data", body,
                        "device", DEVICE_INFO,
                        "signature", sign(body, DEVICE_INFO)))
                .build()).getBytesBody()));
        System.out.println();
    }

    private static void getSmsCode() {
        String body = HttpUtils.getJsonBody("phone", "18628144638",
                "region", "86",
                "uid", "0");
        try {
            System.out.println(new String(client.execute(new Request.Builder("http://121.41.111.181/shuxin/index.php/smsInfo/smsCode")
                    .method(HttpUtils.Method.POST, Request.FormBody.build("data", body,
                            "device", DEVICE_INFO,
                            "signature", sign(body, DEVICE_INFO)))
                    .build()).getBytesBody()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
