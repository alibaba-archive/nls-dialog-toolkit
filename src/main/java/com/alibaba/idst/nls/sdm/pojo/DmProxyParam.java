package com.alibaba.idst.nls.sdm.pojo;

import com.alibaba.idst.nls.dm.common.io.RequestContent;
import com.alibaba.idst.nls.dm.common.io.RequestContext;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

/**
 * @author jianghaitao
 * @date 2019/11/7
 */
@Data
@Builder
public class DmProxyParam {
    @SerializedName("app_key")
    String appkey;
    @SerializedName("request_id")
    String requestId;
    RequestContext context;
    RequestContent content;
}
