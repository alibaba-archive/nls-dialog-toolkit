package com.alibaba.idst.nls.sdm.function;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nls.dm.common.DialogState;
import com.alibaba.idst.nls.dm.common.NameOntology;
import com.alibaba.idst.nls.dm.function.FunctionBase;
import com.alibaba.idst.nls.dm.function.FunctionResult;
import com.alibaba.idst.nls.dm.function.NameResult;
import com.alibaba.idst.nls.sdm.pojo.QueryParam;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author jianghaitao
 * @date 2019/11/7
 */
@Component
public class FuncEngine {
    @Autowired
    private FuncInvoker invoker;

    @Autowired
    private FuncContainer funcContainer;

    public boolean load(String pkg) {
        return funcContainer.load(pkg, false);
    }

    public FunctionResult query(QueryParam queryParam) {
        String appKey = queryParam.getAppkey();
        if (Strings.isEmpty(appKey)) {
            return FunctionResult.builder().retCode(FunctionBase.RET_VALUE_FALSE).retValue("no appKey found").build();
        }

        String sessionId = queryParam.getRequestContent().getSessionId();
        if (Strings.isEmpty(sessionId)) {
            return FunctionResult.builder().retCode(FunctionBase.RET_VALUE_FALSE).retValue("no sessionId found").build();
        }

        /**
         * todo 调用sdk的接口获取调用函数时需要的参数
         */

        return invoker.invokeDialogFunc(queryParam.getFuncName(), queryParam.getDialogState(), JSON
            .toJSONString(queryParam.getRequestContent().getQueryParams()));
    }

    public FunctionResult runLocalFunc(String param, String funcName, DialogState dialogState){
        return invoker.invokeDialogFunc(funcName, dialogState, param);
    }

    public NameResult runNlgFunc(String param, NameOntology.NameInfo nameInfo, String funcName, DialogState dialogState){
        return invoker.invokeFetch(funcName, nameInfo, dialogState, param);
    }
}
