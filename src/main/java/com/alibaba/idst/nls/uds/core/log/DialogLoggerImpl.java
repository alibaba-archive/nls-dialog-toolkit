package com.alibaba.idst.nls.uds.core.log;

import com.alibaba.idst.nls.uds.context.DialogSession;
import com.alibaba.idst.nls.uds.core.context.DialogSessionImpl;
import com.alibaba.idst.nls.uds.log.DialogLogger;
import com.alibaba.idst.nls.uds.util.DateUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Scope("prototype")
public class DialogLoggerImpl implements DialogLogger {

    private DialogSessionImpl session;

    @Autowired
    private DialogLoggerHub loggerHub;

    @Deprecated
    @Override
    public Logger getLogger(DialogSession session) {
        return loggerHub.getLogger(session);
    }

    @Override
    public void debug(String message) {
        String content = buildContent(message);
        loggerHub.getLogger(session).debug(content);
    }

    @Override
    public void warn(String message) {
        String content = buildContent(message);
        loggerHub.getLogger(session).warn(content);
    }

    @Override
    public void info(String message) {
        String content = buildContent(message);
        loggerHub.getLogger(session).info(content);
    }

    @Override
    public void error(String message) {
        String content = buildContent(message);
        loggerHub.getLogger(session).error(content);
    }

    /**
     * time|request_id|app_key|timestamp|host_ip|cell_id|msg
     * @return
     */
    private String buildContent(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(DateUtil.getCurrentTime()).append("|")
            .append(session.getDialogRequest().getRequestId()).append("|")
            .append(session.getAppKey()).append("|")
            .append(System.currentTimeMillis()).append("|")
            .append("127.0.0.1").append("|")
            .append(session.getCellId()).append("|")
            .append(message);
        return sb.toString();
    }

    public DialogSessionImpl getSession() {
        return session;
    }

    public void setSession(DialogSessionImpl session) {
        this.session = session;
    }
}
