package com.alibaba.idst.nls.sdm.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author jianghaitao
 * @date 2019/11/5
 */
public class ToolkitUtil {
    public static String getUuid(){
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getTimeString(){
        LocalDateTime date = LocalDateTime.now();
        return date.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String getFileNameByPath(String jarpath){
        if (jarpath.length() == 0 || !jarpath.endsWith(".jar")){
            return null;
        }

        int index = jarpath.lastIndexOf('/');
        return jarpath.substring(index + 1);
    }
}
