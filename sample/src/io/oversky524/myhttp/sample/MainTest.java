package io.oversky524.myhttp.sample;

import io.oversky524.myhttp.HttpUtils;
import io.oversky524.myhttp.MyHttpClient;
import io.oversky524.myhttp.Request;
import org.junit.Test;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.Assert.*;

/**
 * Created by gaochao on 2016/4/22.
 */
public class MainTest {
    private MyHttpClient myHttpClient = new MyHttpClient.Builder().build();

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    @Test
    public void sslSocketDemo() throws Exception{
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new MyTrustManager()}, null);
        String host = "www.baidu.com";
        SSLSocket socket = (SSLSocket) sslContext.getSocketFactory().createSocket();
        socket.connect(new InetSocketAddress(host, 443));
        SSLSession sslSession = socket.getSession();
        System.out.println(sslSession.getCipherSuite());
        System.out.println(sslSession.getProtocol());
        for(String name : sslSession.getValueNames()){
            System.out.println(name);
        }

        Certificate[] certificates = sslSession.getPeerCertificates();
        for(Certificate certificate : certificates){
            System.out.println(certificate);
        }

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write("GET / HTTP/1.1\r\n");
        writer.write("Host: " + host + "\r\n");
        writer.write("\r\n");
        writer.flush();

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String s;
        while ((s = reader.readLine()) != null){
            if(s.length() < 1) break;
            System.out.println(s);
        }

    }

    @Test
    public void urlTest() throws Exception{
        Provider[] providers = Security.getProviders();
        for(Provider provider : providers){
            System.out.println(provider);
        }
    }

    @Test
    public void myhttpSslTest() throws Exception{
        System.out.println(myHttpClient.execute(new Request.Builder("https://www.baidu.com")
                .method(HttpUtils.Method.GET, null).build())
        .stringBody());
        /*System.out.println(myHttpClient.execute(new Request.Builder("https://localhost:8443/simple")
                .method(HttpUtils.Method.GET, null).build())
        .stringBody());*/
    }

    private static class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}