package com.alibaba.idst.nls.uds.core.nlu;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nlu.request.v6.NluRequest;
import com.alibaba.idst.nlu.response.v6.NluResponse;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NluService {

    public NluResponse invokeNlu(Map<String, String> params, NluRequest nluRequest) {
        // 创建DefaultAcsClient实例并初始化
        String akId = params.get("accessKeyId");
        String akSecret = params.get("accessKeySecret");

        DefaultProfile profile = DefaultProfile.getProfile("center", akId, akSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        String reqBody = JSON.toJSONString(nluRequest);
        log.info("nlu request: {}", reqBody);

        // 创建API请求并设置参数
        CommonRequest request = new CommonRequest();
        request.setDomain("smartr.aliyuncs.com");
        request.setVersion("2018-03-16");
        request.setAction("GetNluPlusResponse");
        request.setMethod(MethodType.GET);
        request.putQueryParameter("Request", reqBody);

        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("nlu response: {}", response.getData());
            if(response.getHttpStatus() != 200) {
                return null;
            } else {
                return NluResponse.parseNluResponse(response.getData());
            }
        } catch (Exception e) {
            log.error("invoke nlu failed");
            return null;
        }
    }

}