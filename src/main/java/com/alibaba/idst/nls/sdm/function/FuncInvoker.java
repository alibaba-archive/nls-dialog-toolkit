package com.alibaba.idst.nls.sdm.function;

import com.alibaba.idst.nls.dm.common.DialogState;
import com.alibaba.idst.nls.dm.common.NameOntology;
import com.alibaba.idst.nls.dm.function.AbstractFetch;
import com.alibaba.idst.nls.dm.function.FunctionBase;
import com.alibaba.idst.nls.dm.function.FunctionResult;
import com.alibaba.idst.nls.dm.function.NameResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author jianghaitao
 * @date 2019/11/7
 */
@Component
@Log4j2
public class FuncInvoker implements ApplicationContextAware {
    @Autowired
    private FuncContainer funcContainer;

    private static ApplicationContext applicationContext = null;

    public static FuncInvoker getInstance() {
        return applicationContext.getBean(FuncInvoker.class);
    }

    /**
     * 通过反射调用函数
     * @param funcName
     * @param dialogState
     * @param param
     * @return
     */
    public FunctionResult invokeDialogFunc(String funcName, DialogState dialogState, String param) {
        FunctionResult functionResult = null;

        try {
            Class<? extends FunctionBase> clazz = (Class<? extends FunctionBase>)Class.forName(funcName);
            FunctionBase fn = (FunctionBase)funcContainer.createInstance(clazz, null);
            functionResult = fn.exec(param, dialogState);
        } catch (Exception e) {
            log.error("execute function " + funcName + " failed, param is " + param, e);
        }

        return functionResult;
    }

    public NameResult invokeFetch(String funcName, NameOntology.NameInfo nameInfo, DialogState dialogState,
                                  String param) {
        NameResult nameResult = null;
        try {
            Class<? extends AbstractFetch>clazz = (Class<? extends AbstractFetch>)Class.forName(funcName);
            AbstractFetch fn = (AbstractFetch)funcContainer.createInstance(clazz, null);
            fn.setNameInfo(nameInfo);
            nameResult = fn.fetch(param, dialogState);
        } catch (Exception e) {
            log.info(funcName + " is not local nlg function...");
        }

        return nameResult;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        FuncInvoker.applicationContext = applicationContext;
    }
}
