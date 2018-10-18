package com.alibaba.idst.nls.uds;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nls.uds.core.engine.FuncEngine;
import com.alibaba.idst.nls.uds.request.DialogRequest;
import com.alibaba.idst.nls.uds.request.RequestContent;
import com.alibaba.idst.nls.uds.request.RequestContext;
import com.alibaba.idst.nls.uds.response.DialogResponse;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * description
 */
@Slf4j
@Component
public class DialogEngine {

    private Map<String, String> params;

    @Autowired
    private FuncEngine funcEngine;

    public boolean init(Map<String, String> params) {
        this.params = params;
        String appKey = params.get("appKey");
        if(Strings.isNullOrEmpty(appKey)) {
            log.error("appKey不能为空");
            return false;
        }

        String pkg = params.get("package");
        if(Strings.isNullOrEmpty(pkg)) {
            log.error("请指定function所在的包名");
            return false;
        }

        String akId = params.get("accessKeyId");
        if(Strings.isNullOrEmpty(akId)) {
            log.error("accessKeyId不能为空");
            return false;
        }

        String akSecret = params.get("accessKeySecret");
        if(Strings.isNullOrEmpty(akSecret)) {
            log.error("accessKeySecret");
            return false;
        }

        return funcEngine.load(params.get("package"));
    }

    public String ask(String query) {
        RequestContent content = RequestContent.builder()
            .sessionId(params.get("sessionId"))
            .query(query)
            .build();

        RequestContext context = RequestContext.builder()
            .optional(params.get("optional"))
            .build();

        DialogRequest dialogRequest = DialogRequest.builder()
            .appKey(params.get("appKey"))
            .requestId(UUID.randomUUID().toString().replaceAll("-",""))
            .content(content)
            .context(context)
            .build();

        DialogResponse response = funcEngine.query(params, dialogRequest);
        return JSON.toJSONString(response);
    }

}
