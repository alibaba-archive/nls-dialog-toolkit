package com.alibaba.idst.nls.uds.core.context;

import com.alibaba.idst.nls.uds.context.DialogSession;
import com.alibaba.idst.nls.uds.request.DialogRequest;
import com.alibaba.idst.nlu.request.v6.context.dialog.NluDialogContext;
import com.alibaba.idst.nlu.response.common.BaseSlot;
import com.alibaba.idst.nlu.response.v6.NluResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 用户Session接口实现类，可通过该接口获得或操作session数据
 */
@Component
@Scope("prototype")
public class DialogSessionImpl implements DialogSession {

    private String cellId;
    private String sessionId;
    private String appKey;
    private DialogRequest dialogRequest;

    @Autowired
    private DialogContext dialogContext;

    @Override
    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public DialogRequest getDialogRequest() {
        return dialogRequest;
    }

    public void setDialogRequest(DialogRequest dialogRequest) {
        this.dialogRequest = dialogRequest;
    }

    @Override
    public Map<String, List<BaseSlot>> getSlots(String domain) {
        return dialogContext.getSlots(sessionId, domain);
    }

    public void removeSlot(String domain, String slotName) {
        dialogContext.removeSlot(sessionId, domain, slotName);
    }

    public NluResponse getLastNluResult() {
        return dialogContext.getLastNluResult(sessionId);
    }

    public Queue<NluResponse> getHistoryNluResults() {
        return dialogContext.getNluResults(sessionId);
    }

    public void addNluContext(NluDialogContext nluContext) {
        dialogContext.addNluContext(sessionId, nluContext);
    }

    public void removeNluContext(String domain, String intent) {
        dialogContext.removeNluContext(sessionId, domain, intent);
    }
    @Override
    public Map<String, List<BaseSlot>> getSlots() {
        return dialogContext.getSlots(sessionId);
    }

}
