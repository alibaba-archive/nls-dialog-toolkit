package com.alibaba.idst.nls.uds.core.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.nls.uds.nlu.NluSlotProcessor;
import com.alibaba.idst.nlu.request.v6.context.dialog.NluDialogContext;
import com.alibaba.idst.nlu.response.common.Slot;
import com.alibaba.idst.nlu.response.slot.BasicSlot;
import com.alibaba.idst.nlu.response.slot.DateTimeSlot;
import com.alibaba.idst.nlu.response.slot.GeoSlot;
import com.alibaba.idst.nlu.response.slot.NumberSlot;
import com.alibaba.idst.nlu.response.slot.TimeDurationSlot;
import com.alibaba.idst.nlu.response.v6.NluResponse;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dialogue Context
 */
@Slf4j
@Component
public class DialogContext {

    private static final String SLOT_PREFIX = "SLOT_";
    private static final String NLU_CTX_PREFIX = "NLU_CTX_";
    private static final String NLU_RESULT_PREFIX = "NLU_RESULT_";

    /**
     * 队列最大长度，只记录最近5轮对话
     */
    private static final int MAX_QUEUE_SIZE = 5;

    @Autowired
    private DialogCache dialogCache;


    /**
     * 添加NLU上下文
     * @param sessionId
     * @param ctx
     */
    public void addNluContext(String sessionId, NluDialogContext ctx) {
        String ctxId = NLU_CTX_PREFIX + sessionId;
        if (dialogCache.getQueueLen(ctxId) == MAX_QUEUE_SIZE) {
            dialogCache.popFromQueue(ctxId);
        }
        dialogCache.pushToQueue(ctxId, JSON.toJSONString(ctx));
    }

    /**
     * 删除NLU上下文
     * @param sessionId
     * @param domain
     * @param intent
     */
    public void removeNluContext(String sessionId, String domain, String intent) {
        String ctxId = NLU_CTX_PREFIX + sessionId;

        List<NluDialogContext> contextList = this.getNluContexts(sessionId);
        contextList = contextList.stream().filter(c -> c.getDomain().equals(domain) && c.getIntent().equals(intent)).collect(
            Collectors.toList());

        dialogCache.removeCache(ctxId);
        for(NluDialogContext ctx : contextList) {
            dialogCache.pushToQueue(ctxId, JSON.toJSONString(ctx));
        }
    }

    public List<NluDialogContext> getNluContexts(String sessionId) {
        String ctxId = NLU_CTX_PREFIX + sessionId;
        Queue<String> dataList = dialogCache.getQueue(ctxId);
        if(dataList == null || dataList.isEmpty()) {
            return null;
        }

        List<NluDialogContext> contexts = new ArrayList<>();
        for (String data : dataList) {
            contexts.add(JSON.parseObject(data, NluDialogContext.class));
        }

        return contexts;
    }

    /**
     * add default nlu response
     * @param sessionId
     * @param response
     */
    public void addNluResult(String sessionId, NluResponse response) {
        String nluId = NLU_RESULT_PREFIX + sessionId;
        if (dialogCache.getQueueLen(nluId) == MAX_QUEUE_SIZE) {
            dialogCache.popFromQueue(nluId);
        }
        dialogCache.pushToQueue(nluId, JSON.toJSONString(response));
    }

    public Queue<NluResponse> getNluResults(String sessionId) {
        String nluId = NLU_RESULT_PREFIX + sessionId;
        Queue<NluResponse> nluResponses = new LinkedList<>();
        Queue<String> results = dialogCache.getQueue(nluId);
        if (results != null && !results.isEmpty()) {
            for (String str : results) {
                NluSlotProcessor processor = new NluSlotProcessor();
                NluResponse nluResponse = JSON.parseObject(str, NluResponse.class, processor);
                nluResponses.add(nluResponse);
            }
        }
        return nluResponses;
    }

    public NluResponse getLastNluResult(String sessionId) {
        String nluId = NLU_RESULT_PREFIX + sessionId;
        String data = dialogCache.getQueueByIndex(nluId, 0);
        if (Strings.isNullOrEmpty(data)) {
            return null;
        }
        NluSlotProcessor processor = new NluSlotProcessor();
        return JSON.parseObject(data, NluResponse.class, processor);
    }

    public void addSlots(String sessionId, String domain, Map<String, List<Slot>> slots) {
        if(slots == null || slots.isEmpty()) {
            return;
        }
        String slotId = SLOT_PREFIX + sessionId;

        Map<String, Map<String, List<Slot>>> slotMap = new HashMap<>();
        String jsonData = dialogCache.readString(slotId);
        if (Strings.isNullOrEmpty(jsonData)) {
            slotMap.put(domain, slots);
            dialogCache.saveString(slotId, JSON.toJSONString(slotMap));
        } else {
            JSONObject root = JSON.parseObject(jsonData);
            JSONObject slotObjs = root.getJSONObject(domain);
            if (slotObjs == null) {
                slotObjs = JSON.parseObject(JSON.toJSONString(slots));
                root.put(domain, slotObjs);
            } else {
                Map<String, List<Slot>> cachedSlots = getCachedSlot(slotObjs);
                cachedSlots.putAll(slots);

                root.put(domain, JSON.parseObject(JSON.toJSONString(cachedSlots)));
            }
            dialogCache.saveString(slotId, JSON.toJSONString(root));
        }
    }

    public void removeSlot(String sessionId, String domain, String slotName) {
        String slotId = SLOT_PREFIX + sessionId;

        String jsonData = dialogCache.readString(slotId);
        if (Strings.isNullOrEmpty(jsonData)) {
            return;
        }

        JSONObject root = JSON.parseObject(jsonData);
        JSONObject slots = root.getJSONObject(domain);
        if (slots == null) {
            return;
        }

        Map<String, List<Slot>> cachedSlots = getCachedSlot(slots);
        cachedSlots.remove(slotName);
        root.put(domain, JSON.parseObject(JSON.toJSONString(cachedSlots)));

        dialogCache.saveString(slotId, JSON.toJSONString(root));
    }

    public Map<String, List<Slot>> getSlots(String sessionId, String domain) {
        String slotId = SLOT_PREFIX + sessionId;
        String jsonData = dialogCache.readString(slotId);
        if (Strings.isNullOrEmpty(jsonData)) {
            return null;
        }

        JSONObject root = JSON.parseObject(jsonData);
        JSONObject slots = root.getJSONObject(domain);
        if (slots == null) {
            return null;
        }

        return getCachedSlot(slots);
    }

    public Map<String, List<Slot>> getSlots(String sessionId) {
        String slotId = SLOT_PREFIX + sessionId;

        Map<String, List<Slot>> slotMap = new HashMap<>();
        String jsonData = dialogCache.readString(slotId);
        if (Strings.isNullOrEmpty(jsonData)) {
            return null;
        }

        JSONObject root = JSON.parseObject(jsonData);
        Set<String> keys = root.keySet();
        for (String key : keys) {
            JSONObject slots = root.getJSONObject(key);
            if (slots == null) {
                continue;
            }

            Map<String, List<Slot>> cachedSlots = getCachedSlot(slots);
            if (cachedSlots == null || cachedSlots.isEmpty()) {
                continue;
            }
            slotMap.putAll(cachedSlots);
        }

        return slotMap;
    }


    public void clean(String sessionId) {
        String slotId = SLOT_PREFIX + sessionId;
        dialogCache.removeCache(slotId);

        String nluCtxId = NLU_CTX_PREFIX + sessionId;
        dialogCache.removeCache(nluCtxId);

        String nluResultId = NLU_RESULT_PREFIX + sessionId;
        dialogCache.removeCache(nluResultId);
    }

    private Map<String, List<Slot>> getCachedSlot(JSONObject slotList) {
        Map<String, List<Slot>> cachedSlots = new HashMap<>();
        for (Entry<String, Object> item : slotList.entrySet()) {
            List<Slot> list = new ArrayList<>();
            JSONArray arr = (JSONArray)item.getValue();
            for (int i = 0; i < arr.size(); i++) {
                Slot slot = getSlot(arr.getJSONObject(i));
                list.add(slot);
            }
            cachedSlots.put(item.getKey(), list);
        }
        return cachedSlots;
    }

    private Slot getSlot(JSONObject slot) {
        if (!slot.containsKey("level_1") && !slot.containsKey("level_2") && !slot.containsKey("level_3") && !slot
            .containsKey("level_4") && !slot.containsKey("level_5") && !slot.containsKey("location")) {
            if (slot.containsKey("norm") && slot.get("norm") instanceof JSONArray) {
                if (slot.containsKey("error_code") || slot.containsKey("relative_mode") || slot.containsKey("time")
                    || slot.containsKey("timex") || slot.containsKey("use_week") || slot.containsKey("date")) {
                    return JSON.parseObject(JSON.toJSONString(slot), DateTimeSlot.class);
                }

                if (slot.containsKey("type")) {
                    return JSON.parseObject(JSON.toJSONString(slot), NumberSlot.class);
                }
            } else if (slot.containsKey("timex")) {
                return JSON.parseObject(JSON.toJSONString(slot), TimeDurationSlot.class);
            }
            return JSON.parseObject(JSON.toJSONString(slot), BasicSlot.class);
        } else {
            return JSON.parseObject(JSON.toJSONString(slot), GeoSlot.class);
        }
    }
}
