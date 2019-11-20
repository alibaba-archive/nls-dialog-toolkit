package com.alibaba.idst.nls.sdm.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jianghaitao
 * @date 2019/11/13
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class QasRequest implements Serializable {
    @JSONField(name = "request_id")
    private String requestId;

    @JSONField(name = "request_type")
    private String requestType;

    @JSONField(name = "app_key")
    private String appKey;

    @JSONField(name = "raw_query")
    private String rawQuery;

    private String optional;

    @JSONField(name = "device_context")
    private DeviceContext deviceContext;
}
