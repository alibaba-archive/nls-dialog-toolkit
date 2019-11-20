package com.alibaba.idst.nls.sdm.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.nls.dm.common.QasClient;
import com.alibaba.idst.nls.dm.common.io.DialogRequest;
import com.alibaba.idst.nls.dm.common.io.ParamItem;
import com.alibaba.idst.nls.dm.common.io.QaAnswer;
import com.alibaba.idst.nls.sdm.pojo.QasRequest;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jianghaitao
 * @date 2019/11/13
 */
@Service
public class QasClientImpl implements QasClient {
    @Value("${sts.access.id}")
    private String accessId;

    @Value("${sts.access.key}")
    private String accessKey;

    @Override
    public List<QaAnswer> getQasResult(String appkey, DialogRequest dialogRequest) {
        String optional = getOptional(dialogRequest);
        QasRequest qasRequest = QasRequest.builder()
            .requestId(dialogRequest.getRequestId())
            .requestType("")
            .appKey(appkey)
            .optional(optional)
            .rawQuery(dialogRequest.getContent().getQuery())
            .build();

        return queryQas(qasRequest);
    }

    private List<QaAnswer> queryQas(QasRequest qasRequest){
        DefaultProfile profile = DefaultProfile.getProfile("center", accessId, accessKey);
        IAcsClient client = new DefaultAcsClient(profile);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("app_key", qasRequest.getAppKey());
        jsonObject.put("raw_query", qasRequest.getRawQuery());
        jsonObject.put("request_id", qasRequest.getRequestId());
        String reqBody = JSON.toJSONString(jsonObject);

        System.out.println("requestSync reqBody : " + reqBody);

        // 创建API请求并设置参数
        CommonRequest request = new CommonRequest();
        request.setDomain("smartr.aliyuncs.com");
        request.setVersion("2019-06-05");
        request.setAction("GetQasResponse");
        request.setMethod(MethodType.POST);
        request.putQueryParameter("Request", reqBody);

        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println("response : " + response.getData());
            JSONObject result = JSON.parseObject(response.getData());
            JSONObject answerObj = JSON.parseObject(result.getString("Data"));
            return answerObj.getJSONArray("answers").stream().map(obj -> {
                JSONObject jsonObj = (JSONObject)obj;
                QaAnswer tmpAnswer = new QaAnswer();
                tmpAnswer.setScore(jsonObj.getDouble("score"));
                tmpAnswer.setQuestion(jsonObj.getString("question"));
                tmpAnswer.setOptional(jsonObj.get("optional"));
                tmpAnswer.setDomain(jsonObj.getString("domain"));
                tmpAnswer.setAnswer(jsonObj.getString("answer"));
                return tmpAnswer;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getOptional(DialogRequest dialogRequest) {
        List<ParamItem> params = dialogRequest.getContent().getQueryParams();
        if (params == null || params.isEmpty()) {
            return null;
        }

        JSONObject root = new JSONObject();
        for (ParamItem item : params) {
            String name = item.getName();
            if ("domains".equals(name) || "top".equals(name) || "health".equals(name)) {
                root.put(name, item.getValue());
            }
        }
        return root.toJSONString();
    }
}
