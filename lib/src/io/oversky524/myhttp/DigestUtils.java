package io.oversky524.myhttp;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by gaochao on 2015/10/26.
 */
public class DigestUtils {
    private DigestUtils(){}

    private static String encode(String str, String method) {
        MessageDigest md = null;
        String dstr = null;
        try {
            md = MessageDigest.getInstance(method);
            md.update(str.getBytes());
            dstr = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String pre = "0000000000000000000000000000000000000000";
        dstr = pre.substring(0,pre.length()-dstr.length())+dstr;
        return dstr;
    }

    /**
     * 计算SHA1摘要
     * */
    public static String getSha1(String val){
        return encode(val, "SHA1");
    }

    /**
     * 计算MD5摘要
     * */
    public static String getMD5(String val){
        return encode(val, "MD5");
    }
}
