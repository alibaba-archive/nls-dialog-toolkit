package com.alibaba.idst.nls.sdm.log;

import org.apache.logging.log4j.core.Logger;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author jianghaitao
 * @date 2018/11/12
 */
@Component
public class DialogLoggerHub {
    Logger logger;

    public Logger getLogger() {
        if (Objects.isNull(logger)){
            logger =  DialogLoggerUtil.createLogger("dialog");
        }
        return logger;
    }
}
