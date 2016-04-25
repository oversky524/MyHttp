package io.oversky524.myhttp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by gaochao on 2016/4/20.
 */
public class TimeUtils {
    private TimeUtils(){ throw new AssertionError("No instance!"); }

    private static final String[] GMT_FORMATS = {"EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd-MMM-yyyy HH:mm:ss z",
            "EEE, dd-MMM-yy HH:mm:ss z"};

    /**
     * 将Http的绝对时间格式字符串转换为自格林尼治时间1970.01.01 00:00:00以来的毫秒数
     * */
    public static long http(String gmt) {
        int i=0;
        long time = -1;
        again:
        while (i<GMT_FORMATS.length) {
            try {
                time = date(gmt, GMT_FORMATS[i], Locale.US, "GMT");
                break;
            } catch (ParseException e) {
                e.printStackTrace();
                ++i;
                continue again;
            }
        }
        return time;
    }

    /**
     * 将当前时间转换为Http协议要求的时间字符串
     * */
    public static String http(){
        return http(new Date().getTime());
    }
    public static String http(long time){
        return date(time, GMT_FORMATS[0], Locale.US, "GMT");
    }

    /**
     * 根据TimeZone，Locale和DateFormat字符串，生成时间字符串
     * */
    public static String date(long time, String format, Locale locale, String zone){
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone(zone));
        return dateFormat.format(new Date(time));
    }

    /**
     * 根据TimeZone，Locale以及DateFormat字符串，将时间字符串转换为
     * 自格林尼治时间1970.01.01 00:00:00以来的毫秒数
     * */
    public static long date(String time, String format, Locale locale, String zone) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone(zone));
        return dateFormat.parse(time).getTime();
    }
}
