package com.np.ioc;

import android.app.Activity;
import android.view.View;

public class ViewInject {

    private static final String PROXY = "_ViewInject";

    public static void injectView(Activity activity) {
        Injector injector = findProxyClass(activity);
        injector.inject(activity, activity);
    }

    public static void injectView(Object host, View view) {
        Injector injector = findProxyClass(host);
        injector.inject(host, view);
    }

    /** 找到注解处理器动态生成的代理类. */
    private static Injector findProxyClass(Object activity) {
        try {
            Class<?> aClass = activity.getClass();
            Class<?> proxyClass = Class.forName(aClass.getName() + PROXY);
            return (Injector) proxyClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
