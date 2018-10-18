package com.alibaba.idst.nls.uds.core.engine;

import com.alibaba.idst.nls.uds.core.context.DialogSessionImpl;
import com.alibaba.idst.nls.uds.response.DialogResultElement;
import com.alibaba.idst.nlu.response.common.NluResultElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * 调用dialogue function，先从cache中
 */
@Slf4j
@Component
public class FuncInvoker {

    @Autowired
    private FuncContainer funcContainer;

    /**
     * 通过反射调用函数
     *
     * @param element nlu result element
     * @param session dialog session
     * @return
     */
    public DialogResultElement invokeDialogFunc(NluResultElement element, DialogSessionImpl session) {
        String domain = element.getDomain();
        String intent = element.getIntent();


        Object instance = funcContainer.createInstance(domain, intent, session);
        if(instance == null) {
            String msg = "call " + domain + "." + intent + " failed";
            return DialogResultElement.builder().displayText(msg).build();
        }

        Function<NluResultElement, DialogResultElement> func = (Function)instance;
        return func != null ? func.apply(element) : null;
    }

}
