package com.alibaba.idst.nls.sdm.common;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author jianghaitao
 * @date 2019/11/7
 */
public class HttpUtil {
    private static final String MEDIA_TYPE = "application/json; charset=utf-8";

    public static String post(String url, String payload) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(payload, "UTF-8");

        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", MEDIA_TYPE);
        httpPost.setHeader("Content-type", MEDIA_TYPE);

        try (CloseableHttpResponse response = client.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
