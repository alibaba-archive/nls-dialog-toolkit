package com.alibaba.idst.nls.sdm.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jianghaitao
 * @date 2019/11/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceContext {
    @JSONField(name = "device_uuid")
    private String deviceUuid;

    @JSONField(name = "uid")
    private String uid;
}
