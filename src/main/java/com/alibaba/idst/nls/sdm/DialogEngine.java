package com.alibaba.idst.nls.sdm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.nls.dm.common.DialogFunctionType;
import com.alibaba.idst.nls.dm.common.FunctionParamSet;
import com.alibaba.idst.nls.dm.common.SdmException;
import com.alibaba.idst.nls.dm.common.io.DialogResultElement;
import com.alibaba.idst.nls.sdm.common.ToolkitUtil;
import com.alibaba.idst.nls.sdm.function.FuncEngine;
import com.alibaba.idst.nls.sdm.oss.OssService;
import com.alibaba.idst.nls.sdm.pojo.DmProxyParam;
import com.alibaba.idst.nls.sdm.pojo.QueryParam;
import com.alibaba.idst.nls.sdm.proxy.DmProxy;
import com.alibaba.idst.nls.sdm.sdc.SdcService;
import com.aliyuncs.exceptions.ClientException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author jianghaitao
 * @date 2019/11/5
 */
@Component
@Log4j2
public class DialogEngine {
    @Autowired
    private SdcService sdcService;

    @Autowired
    private OssService ossService;

    @Autowired
    private DmProxy dmProxy;

    @Autowired
    private FuncEngine funcEngine;

    private JSONObject stcInfo;
    private String appkey;
    private String projectToken;

    public void uploadResource(String jarPath, String engineResourceDir){
        try {
            if (stcInfo == null){
                stcInfo = sdcService.getStcInfo(appkey, projectToken);
            }
            ossService.uploadJar(jarPath, stcInfo, appkey);
            ossService.uploadResources(engineResourceDir, stcInfo, appkey);
            reloadProject(appkey, projectToken);
        } catch (ClientException e) {
            log.error("get stc info failed, appkey is " + appkey + ", project token is " + projectToken, e);
        }
    }

    private void reloadProject(String appkey, String projectToken){
        try {
            sdcService.reloadApp(appkey, projectToken);
        } catch (ClientException e) {
            log.error("reload app failed, appkey is " + appkey + " project token is " + projectToken, e);
        }
    }

    private String getDebugInfo(String appkey){
        String debugPath = ossService.getDebugPath(appkey);
        String debugInfo = ossService.readWithStcInfo(stcInfo, debugPath);
        return debugInfo;
    }

    public void runLocalFunction(){
            try {
                if (stcInfo == null) {
                    stcInfo = sdcService.getStcInfo(appkey, projectToken);
                }

                String debugInfo = getDebugInfo(appkey);
                List<FunctionParamSet>functionParamSets = new Gson().fromJson(debugInfo,
                    new TypeToken<List<FunctionParamSet>>(){}.getType());
                for (FunctionParamSet functionParam : functionParamSets){
                    if (DialogFunctionType.DIALOG_FUNC.equals(functionParam.getDialogFunctionType())){
                        funcEngine.runLocalFunc(functionParam.getParam(), functionParam.getFuncName(),
                            functionParam.getDialogState());
                        continue;
                    }

                    if (DialogFunctionType.NLG_FUNC.equals(functionParam.getDialogFunctionType())){
                        funcEngine.runNlgFunc(functionParam.getParam(), functionParam.getNameInfo(),
                            functionParam.getFuncName(), functionParam.getDialogState());
                        continue;
                    }
                }
            } catch (ClientException e) {
                e.printStackTrace();
            }
    }

    public boolean init(String codePackage, String appkey, String projectToken) throws IOException {
        this.appkey = appkey;
        this.projectToken = projectToken;
        funcEngine.load(codePackage);
        return true;
    }

    public DmProxyParam buildRequest(QueryParam param){
        String requestId = ToolkitUtil.getUuid();
        DmProxyParam dmProxyParam = DmProxyParam.builder().context(param.getRequestContext())
            .content(param.getRequestContent()).appkey(param.getAppkey()).requestId(requestId)
            .build();
        return dmProxyParam;
    }

    public DialogResultElement run(QueryParam param) throws SdmException {
        if(Strings.isEmpty(param.getAppkey())) {
            log.error("appKey不能为空");
            return null;
        }
        DmProxyParam dmProxyParam = buildRequest(param);
        String result = dmProxy.invokeSdmQuery(dmProxyParam);
        log.info("result is {}", result);

        JSONObject resultObj = JSON.parseObject(result);
        log.info("task id is {}", resultObj.getString("request_id"));

        runLocalFunction();
        return null;
    }
}
