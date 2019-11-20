package com.alibaba.idst.nls.sdm.function;

import com.alibaba.idst.nls.dm.annotation.DialogFunc;
import com.alibaba.idst.nls.dm.annotation.DialogLogger;
import com.alibaba.idst.nls.dm.annotation.Inject;
import com.alibaba.idst.nls.dm.common.QasClient;
import com.alibaba.idst.nls.dm.function.FuncContainerI;
import com.alibaba.idst.nls.sdm.common.SpringUtil;
import com.alibaba.idst.nls.sdm.log.DialogLoggerImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jianghaitao
 * @date 2019/11/7
 */
@Log4j2
@Component
public class FuncContainer implements FuncContainerI {
    /**
     * key:domain_intent value: class
     */
    private Map<String, Class> funcMap = new ConcurrentHashMap<>();

    /**
     * load class
     * @param pkg
     * @return
     */
    @Override
    public boolean load(String pkg, boolean isWarmup) {
        try {
            findScriptClass(pkg, true);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean unload(String s) {
        return false;
    }

    @Override
    public Map<String, Class> getSystemFuncMap() {
        return new HashMap<>();
    }

    @Override
    public Class getDialogFunc(String appkey, String funcName) {
        return funcMap.get(funcName);
    }

    @Override
    public Object createInstance(Class clazz, String appkey) {
        Object instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("create instance failed:{}", e.getMessage());
            return null;
        }

        List<Field> fields = new ArrayList<>();
        Class parentClass = clazz;
        while (parentClass != null) {
            fields.addAll(Arrays.asList(parentClass.getDeclaredFields()));
            parentClass = parentClass.getSuperclass();
        }

        for (Field field : fields) {
            Inject inject = field.getDeclaredAnnotation(Inject.class);
            if (inject != null) {
                field.setAccessible(true);
                try {
                    if (Objects.equals(field.getType(), DialogLogger.class)) {
                        DialogLoggerImpl logger = (DialogLoggerImpl)SpringUtil.getBean(field.getType());
                        field.set(instance, logger);
                        continue;
                    }

                    if (Objects.equals(field.getType(), QasClient.class) && Objects.nonNull(appkey)) {
                        QasClient qasClient = (QasClientImpl)SpringUtil.getBean(field.getType());
                        field.set(instance, qasClient);
                        continue;
                    }
                    field.set(instance, SpringUtil.getBean(field.getType()));
                } catch (IllegalAccessException e) {
                    log.error("inject value to property failed:{}", e.getMessage());
                }
            }
        }

        parentClass = clazz;
        List<Method> methods = new ArrayList<>();
        while (parentClass != null) {
            methods.addAll(Arrays.asList(parentClass.getDeclaredMethods()));
            parentClass = parentClass.getSuperclass();
        }

        return instance;
    }

    @Override
    public ClassLoader getClassLoaderByAppkey(String s) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader;
    }

    @Override
    public Class getProxyFunc(String funcName, String proxyType){
        if (funcMap.containsKey(funcName)){
            return funcMap.get(funcName);
        }

        return null;
    }

    private List<Class<?>> findScriptClass(String pkgName , boolean isRecursive) {
        List<Class<?>> classList = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            // 按文件的形式去查找
            String strFile = pkgName.replaceAll("\\.", "/");
            Enumeration<URL> urls = loader.getResources(strFile);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    String protocol = url.getProtocol();
                    String pkgPath = url.getPath();
                    if ("file".equals(protocol)) {
                        findClass(classList, pkgName, pkgPath, isRecursive);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classList;
    }

    private void findClass(List<Class<?>> clazzList, String pkgName, String pkgPath, boolean isRecursive) {
        if(clazzList == null){
            return;
        }

        // 过滤出.class文件及文件夹
        File[] files = filterClassFiles(pkgPath);
        String funcName = null;
        if(files != null){
            for (File f : files) {
                String fileName = f.getName();
                if (f.isFile()) {
                    // .class 文件的情况
                    String clazzName = getClassName(pkgName, fileName);
                    if(!Strings.isEmpty(clazzName)) {
                        Class clazz = null;
                        try {
                            clazz = Class.forName(clazzName);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(clazz != null) {
                            Annotation[] annotations = clazz.getDeclaredAnnotations();
                            for (Annotation tmpAno : annotations){
                                String tmpName = tmpAno.annotationType().getName();
                                if (tmpName.equals(DialogFunc.class.getName())){
                                    funcName = String.format("%s", clazz.getName());
                                    funcMap.put(funcName, clazz);
                                    createInstance(clazz, null);
                                }
                            }
                        }
                    }
                } else {
                    // 文件夹的情况
                    if(isRecursive){
                        // 需要继续查找该文件夹/包名下的类
                        String subPkgName = pkgName +"."+ fileName;
                        String subPkgPath = pkgPath + File.separator + fileName;
                        findClass(clazzList, subPkgName, subPkgPath, true);
                    }
                }
            }
        }
    }

    private File[] filterClassFiles(String pkgPath) {
        if(pkgPath == null){
            return null;
        }
        // 接收 .class 文件 或 类文件夹
        return new File(pkgPath).listFiles(
            file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
    }

    private String getClassName(String pkgName, String fileName) {
        int endIndex = fileName.lastIndexOf(".");
        String clazz = null;
        if (endIndex >= 0) {
            clazz = fileName.substring(0, endIndex);
        }
        String clazzName = null;
        if (clazz != null) {
            clazzName = pkgName + "." + clazz;
        }
        return clazzName;
    }
}
