package com.alibaba.idst.nls.sdm.sdc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author jianghaitao
 * @date 2019/11/5
 */
@Component
public class SdcService {
    @Value("${sts.region.id}")
    private String regionId;

    @Value("${sts.access.id}")
    private String accessId;

    @Value("${sts.access.key}")
    private String accessKey;

    @Value("${sdc.endpoint}")
    private String sdcEndpoint;

    public JSONObject getStcInfo(String appkey, String projectToken) throws ClientException {
        DefaultProfile profile = DefaultProfile.getProfile(
            regionId,      // 地域ID
            accessId,      // RAM账号的AccessKey ID
            accessKey); // RAM账号Access Key Secret
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setDomain("nls-sdc-pop.aliyuncs.com");
        request.setVersion("2019-11-13");
        request.setAction("GetStsAuthority");
        request.setMethod(MethodType.POST);
        request.putQueryParameter("Appkey", appkey);
        request.putQueryParameter("ProjectToken", projectToken);

        CommonResponse response = client.getCommonResponse(request);
        JSONObject resultJson = JSON.parseObject(response.getData());
        if (resultJson.getInteger("ResultCode") != 0){
            return null;
        }
        return JSON.parseObject(resultJson.getString("Data"));
    }

    public boolean reloadApp(String appkey, String projectToken) throws ClientException {
        DefaultProfile profile = DefaultProfile.getProfile(
            regionId,      // 地域ID
            accessId,      // RAM账号的AccessKey ID
            accessKey); // RAM账号Access Key Secret
        IAcsClient client = new DefaultAcsClient(profile);
        // 创建API请求并设置参数
        CommonRequest request = new CommonRequest();
        request.setDomain("nls-sdc-pop.aliyuncs.com");
        request.setVersion("2019-11-13");
        request.setAction("ReloadApplication");
        request.setMethod(MethodType.POST);
        request.putQueryParameter("Appkey", appkey);
        request.putQueryParameter("ProjectToken", projectToken);

        CommonResponse response = client.getCommonResponse(request);
        JSONObject result = JSON.parseObject(response.getData());
        if (result.getInteger("ResultCode") != 0){
            return false;
        }
        return true;
    }
}
