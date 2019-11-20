package com.alibaba.idst.nls.sdm.pojo;

import com.alibaba.idst.nls.dm.common.DialogState;
import com.alibaba.idst.nls.dm.common.io.DebugModeEnum;
import com.alibaba.idst.nls.dm.common.io.RequestContent;
import com.alibaba.idst.nls.dm.common.io.RequestContext;
import lombok.Builder;
import lombok.Data;

/**
 * @author jianghaitao
 * @date 2019/11/7
 */
@Data
@Builder
public class QueryParam {
    String query;
    String appkey;
    DebugModeEnum debugMode;
    String funcName;
    DialogState dialogState;
    String codePackage;
    String env;
    RequestContent requestContent;
    RequestContext requestContext;
}
