package com.alibaba.idst.nls.uds.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static ThreadLocal<SimpleDateFormat> FORMATTER = ThreadLocal.withInitial(() -> {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    });

    public static String getCurrentTime() {
        SimpleDateFormat format = FORMATTER.get();
        return format.format(new Date());
    }

}
