package com.alibaba.idst.nls.sdm.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.nls.sdm.common.HttpUtil;
import com.alibaba.idst.nls.sdm.pojo.DmProxyParam;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jianghaitao
 * @date 2019/11/7
 */
@Log4j2
@Service
public class DmProxy {
    @Value("${sts.access.id}")
    private String accessId;

    @Value("${sts.access.key}")
    private String accessKey;

    public String invokeSdmQuery(DmProxyParam dmProxyParam){
        DefaultProfile profile = DefaultProfile.getProfile("center", accessId, accessKey);
        IAcsClient client = new DefaultAcsClient(profile);

        // 创建API请求并设置参数
        CommonRequest request = new CommonRequest();
        request.setDomain("smartr.aliyuncs.com");
        request.setVersion("2019-06-05");
        request.setAction("GetSdmResponse");
        request.setMethod(MethodType.POST);
        request.putQueryParameter("Request", new Gson().toJson(dmProxyParam));

        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println("response : " + response.getData());
            JSONObject obj = JSON.parseObject(response.getData());
            return obj.getString("Data");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
