package com.alibaba.idst.nls.uds.core.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nls.uds.core.context.DialogContext;
import com.alibaba.idst.nls.uds.core.context.DialogSessionImpl;
import com.alibaba.idst.nls.uds.core.model.DialogResultCode;
import com.alibaba.idst.nls.uds.core.nlu.NluService;
import com.alibaba.idst.nls.uds.request.DialogRequest;
import com.alibaba.idst.nls.uds.response.DialogResponse;
import com.alibaba.idst.nls.uds.response.DialogResultElement;
import com.alibaba.idst.nls.uds.util.SpringUtil;
import com.alibaba.idst.nlu.request.v6.NluRequest;
import com.alibaba.idst.nlu.request.v6.context.dialog.NluDialogContext;
import com.alibaba.idst.nlu.response.common.NluResultElement;
import com.alibaba.idst.nlu.response.v6.NluResponse;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class FuncEngine {

    @Autowired
    private FuncInvoker invoker;

    @Autowired
    private DialogContext dialogContext;

    @Autowired
    private FuncContainer funcContainer;

    @Autowired
    private NluService nluService;

    private static final String NLU_SOURCE_JSGF = "jsgf";

    private static final String NLU_SOURCE_MODEL = "model";

    public boolean load(String pkg) {
        return funcContainer.load(pkg);
    }

    public DialogResponse query(Map<String, String> params, DialogRequest dialogRequest) {
        String appKey = dialogRequest.getAppKey();
        if (Strings.isNullOrEmpty(appKey)) {
            return new DialogResponse(DialogResultCode.INVALID_ARGUMENT.getCode(), "no appKey found");
        }

        String sessionId = dialogRequest.getContent().getSessionId();
        if (Strings.isNullOrEmpty(sessionId)) {
            return new DialogResponse(DialogResultCode.INVALID_ARGUMENT.getCode(), "no sessionId found");
        }

        //get nlu result
        NluRequest nluRequest = this.buildNluRequest(dialogRequest);
        NluResponse nluResponse = nluService.invokeNlu(params, nluRequest);
        log.info("nlu result:{}, cost:{}ms", JSON.toJSONString(nluResponse));
        if (!isNluResultValid(nluResponse)) {
            return new DialogResponse(DialogResultCode.NLU_FAILED.getCode(), "nlu service error");
        }

        //nlu result selection
        NluResultElement nluResultElement = select(nluResponse);
        if(nluResultElement == null) {
            return new DialogResponse(DialogResultCode.NLU_FAILED.getCode(), "nlu result invalid");
        }

        return invokeFunc(dialogRequest, nluResultElement);
    }

    private NluResultElement select(NluResponse nluResponse) {
        log.info("default selection");
        List<NluResultElement> elements = nluResponse.getElements()
            .stream()
            .filter(e -> e.getSource().equals(NLU_SOURCE_JSGF))
            .sorted(Comparator.comparing(NluResultElement::getScore).reversed())
            .limit(1)
            .collect(Collectors.toList());

        if(elements.isEmpty()) {
            elements = nluResponse.getElements()
                .stream()
                .filter(e -> e.getSource().equals(NLU_SOURCE_MODEL))
                .sorted(Comparator.comparing(NluResultElement::getScore).reversed())
                .limit(1)
                .collect(Collectors.toList());
        }

        nluResponse.setElements(elements);
        return nluResponse.getElements().get(0);
    }

    /**
     * 通过NLU输出结果调用脚本
     *
     * @param dialogRequest
     * @param nluResultElement
     * @return
     */
    private DialogResponse invokeFunc(DialogRequest dialogRequest, NluResultElement nluResultElement) {
        String appKey = dialogRequest.getAppKey();
        String sessionId = dialogRequest.getContent().getSessionId();

        DialogSessionImpl session = SpringUtil.getBean(DialogSessionImpl.class);
        session.setSessionId(sessionId);
        session.setAppKey(appKey);
        session.setDialogRequest(dialogRequest);

        dialogContext.addSlots(sessionId, nluResultElement.getDomain(), nluResultElement.slots());

        DialogResultElement dialogResultElement = invoker.invokeDialogFunc(nluResultElement, session);

        dialogResultElement.setNluResultElement(nluResultElement);

        DialogResponse dialogResponse = new DialogResponse();
        dialogResponse.setRequestId(dialogRequest.getRequestId());
        dialogResponse.setResults(Lists.newArrayList(dialogResultElement));

        return dialogResponse;
    }

    private NluRequest buildNluRequest(DialogRequest dialogRequest) {
        String sessionId = dialogRequest.getContent().getSessionId();

        List<NluDialogContext> nluContexts = this.buildNluContext(sessionId);
        return NluRequest.builder()
            .appKey(dialogRequest.getAppKey())
            .requestId(dialogRequest.getRequestId())
            .query(dialogRequest.getContent().getQuery())
            .nluDialogContexts(nluContexts)
            .version("6.0")
            .build();
    }

    private List<NluDialogContext> buildNluContext(String sessionId) {
        Queue<NluResponse> queue = dialogContext.getNluResults(sessionId);
        List<NluDialogContext> list = new ArrayList<>();
        for(NluResponse resp : queue) {
            for(NluResultElement element : resp.getElements()) {
                NluDialogContext ctx = new NluDialogContext();
                ctx.setDomain(element.getDomain());
                ctx.setIntent(element.getIntent());
                list.add(ctx);
            }
        }

        return list;
    }

    private boolean isNluResultValid(NluResponse nluResponse) {
        return nluResponse != null && nluResponse.getElements() != null && !nluResponse.getElements().isEmpty();
    }

}


