package com.alibaba.idst.nls.uds.core.log;

import com.alibaba.idst.nls.uds.context.DialogSession;
import com.alibaba.idst.nls.uds.core.context.DialogSessionImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DialogLoggerHub {

    private Map<String, Logger> logHub = new ConcurrentHashMap<>();

    public Logger getLogger(DialogSession session) {
        DialogSessionImpl sessionImpl = (DialogSessionImpl) session;
        String appkey = sessionImpl.getAppKey();

        if(logHub.containsKey(appkey)) {
            return logHub.get(appkey);
        } else {
            final String filename = "dialog";
            Logger logger = DialogLoggerUtil.createLogger(filename);
            if(logger != null) {
                logHub.put(appkey, logger);
                return logger;
            }
        }

        return null;
    }

}
