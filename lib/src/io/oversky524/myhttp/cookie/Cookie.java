package io.oversky524.myhttp.cookie;

import io.oversky524.myhttp.utils.TimeUtils;

/**
 * Created by gaochao on 2016/4/20.
 */
public class Cookie {
    private static final String PATH = "path";
    private static final String DOMAIN = "domain";
    private static final String SECURE = "secure";
    private static final String EXPIRES = "expires";
    private String mName;
    private String mValue;
    private String mDomain;
    private String mPath;
    private String mExpires;
    private long mExpireMs;
    private boolean mSecure;

    private Cookie(){}

    public boolean isSecure() { return mSecure; }

    public static Cookie parse(String cookieLine, String urlHost, String urlPath){
        Cookie cookie = new Cookie();
        String[] segments = cookieLine.split(";");
        for(int i=0; i<segments.length; ++i){
            String segment = segments[i].trim();
            String[] kv = segment.split("=");
            String key = kv[0].trim().toLowerCase();
            if(kv.length == 1){
                if(!key.equals(SECURE)) throw new RuntimeException("now only secure property is single!");
                cookie.mSecure = true;
                continue;
            }
            String value = kv[1].trim();
            switch (key){
                case PATH:
                    cookie.mPath = value;
                    break;

                case DOMAIN:
                    cookie.mDomain = value;
                    break;

                case EXPIRES:
                    cookie.mExpires = value;
                    cookie.mExpireMs = TimeUtils.http(value);
                    break;

                default:
                    cookie.mName = kv[0].trim();
                    cookie.mValue = value;
            }
        }
        if(cookie.mDomain == null) cookie.mDomain = urlHost;
        if(cookie.mPath == null) cookie.mPath = urlPath;
        return cookie;
    }
}
