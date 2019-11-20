package com.alibaba.idst.nls.sdm.log;

import com.alibaba.idst.nls.dm.annotation.DialogLogger;
import lombok.Data;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author jianghaitao
 * @date 2018/11/23
 */
@Component
@Data
public class DialogLoggerImpl implements DialogLogger {
    @Autowired
    private DialogLoggerHub dialogLoggerHub;

    @Override
    public Logger getLogger() {
        return dialogLoggerHub.getLogger();
    }

    @Override
    public void debug(String message) {
        String content = this.buildContent(message);
        this.dialogLoggerHub.getLogger().debug(content);
    }

    @Override
    public void warn(String message) {
        String content = this.buildContent(message);
        this.dialogLoggerHub.getLogger().warn(content);
    }

    @Override
    public void info(String message) {
        String content = this.buildContent(message);
        this.dialogLoggerHub.getLogger().info(content);
    }

    @Override
    public void error(String message) {
        String content = this.buildContent(message);
        this.dialogLoggerHub.getLogger().error(content);
    }

    @Override
    public void error(String s, Throwable throwable) {

    }

    @Override
    public void access(String requestId, String appkey, long latency, String funcName, String query, String response){

    }

    private String buildContent(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("|").append(System.currentTimeMillis()).append("|").append(message);
        return sb.toString();
    }
}
