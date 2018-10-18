package com.alibaba.idst.nls.uds.core.engine;

import com.alibaba.idst.nls.uds.annotation.DialogFunc;
import com.alibaba.idst.nls.uds.annotation.Init;
import com.alibaba.idst.nls.uds.annotation.Inject;
import com.alibaba.idst.nls.uds.context.DialogSession;
import com.alibaba.idst.nls.uds.core.context.DialogSessionImpl;
import com.alibaba.idst.nls.uds.core.log.DialogLoggerImpl;
import com.alibaba.idst.nls.uds.log.DialogLogger;
import com.alibaba.idst.nls.uds.util.SpringUtil;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FuncContainer {

    /**
     * key:domain_intent value: class
     */
    private Map<String, Class> funcMap = new ConcurrentHashMap<>();

    /**
     * load class
     * @param pkg
     * @return
     */
    public boolean load(String pkg) {
        try {
            loadLocalClass(pkg);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Object createInstance(String domain, String intent, DialogSessionImpl session) {
        String key = domain + "_" + intent;
        Class clazz = funcMap.get(key);
        if(clazz == null) {
            return null;
        }

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            clazz = classLoader.loadClass(clazz.getName());
        } catch (ClassNotFoundException e) {
            log.error("class not found, className:{}", clazz.getName());
            return null;
        }

        return createInstance(clazz, session);
    }


    private void loadLocalClass(String pkg) throws Exception {
        List<Class<?>> classList = findScriptClass(pkg, true);

        for(Class clazz : classList) {
            String className = clazz.getName();
            Annotation[] annotations = clazz.getDeclaredAnnotations();
            for(Annotation a : annotations) {
                String annotationName = a.annotationType().getName();
                if(annotationName.equals(DialogFunc.class.getName())) {
                    log.info("[dialogue func] {}", className);
                    DialogFunc dialogFunc = (DialogFunc)a;
                    String funcName = dialogFunc.domain() + "_" + dialogFunc.intent();
                    funcMap.put(funcName, clazz);

                    initInstance(clazz);
                } else {
                    continue;
                }
            }
        }
    }

    private void initInstance(Class clazz) {
        createInstance(clazz, null);
    }

    private Object createInstance(Class clazz, DialogSessionImpl session) {
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
                    if (session != null && field.getType() == DialogSession.class) {
                        field.set(instance, session);
                    } else if(session != null && field.getType() == DialogLogger.class) {
                        DialogLoggerImpl logger = (DialogLoggerImpl)SpringUtil.getBean(field.getType());
                        logger.setSession(session);
                        field.set(instance, logger);
                    } else {
                        field.set(instance, SpringUtil.getBean(field.getType()));
                    }
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
        for (Method method : methods) {
            Init init = method.getDeclaredAnnotation(Init.class);
            if (init != null) {
                try {
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("invoke init method failed, class:{} method:{}", clazz.getName(), method.getName());
                    return null;
                }
            }
        }
        return instance;
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
        if(files != null){
            for (File f : files) {
                String fileName = f.getName();
                if (f.isFile()) {
                    // .class 文件的情况
                    String clazzName = getClassName(pkgName, fileName);
                    if(!Strings.isNullOrEmpty(clazzName)) {
                        Class clazz = null;
                        try {
                            clazz = Class.forName(clazzName);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(clazz != null) {
                            clazzList.add(clazz);
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
