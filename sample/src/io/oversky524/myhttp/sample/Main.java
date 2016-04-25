package io.oversky524.myhttp.sample;

import com.squareup.okhttp.*;
import io.oversky524.myhttp.*;
import io.oversky524.myhttp.Headers;
import io.oversky524.myhttp.Request;
import io.oversky524.myhttp.Response;
import io.oversky524.myhttp.cookie.Cookie;
import io.oversky524.myhttp.utils.TimeUtils;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static io.oversky524.myhttp.sample.OriginalSocketHttp.*;

/**
 * Created by gaochao on 2016/4/5.
 */
public class Main {
    private static MyHttpClient client = new MyHttpClient.Builder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();
    private static final String sUrl = "http://121.41.111.181/shuxin/index.php/qaInfo/question";

    public static void main(String[] args) {

    }

    private static void test(){
        String url = "http://imp.optaim.com/201511/629e48fb9a63d1c4509b2e3084c6a4c2.php?a=99&timestamp=1461295911222";
        try {
            client.execute(new Request.Builder(url)
                    .method(HttpUtils.Method.GET, null)
                    .build()).body();
            System.out.println();
            System.out.println();
            System.out.println();
            Response response = client.execute(new Request.Builder(url)
                    .method(HttpUtils.Method.GET, null)
                    .build());
            System.out.println(response.headers());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testCookie(){}

    private static void testConnectionPool() {
        try {
            getQuestions();
            Thread.sleep(20 * 1000);
            getQuestions();
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
                    .body()));
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
                .build()).body()));
        System.out.println();
    }
}
