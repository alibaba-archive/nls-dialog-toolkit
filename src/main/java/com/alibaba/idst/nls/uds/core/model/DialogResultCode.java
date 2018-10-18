package com.alibaba.idst.nls.uds.core.model;

import lombok.Getter;

public enum DialogResultCode {
    SUCCESS("success", 0),
    INTERNAL_ERROR("internal_error", -1),
    NLU_FAILED("nlu failed", -2),
    INVALID_ARGUMENT("invalid argument", -4),
    FAILED("failed", -99);

    @Getter
    private final String description;

    @Getter
    private final int code;

    DialogResultCode(String description, int code) {
        this.description = description;
        this.code = code;
    }
}
