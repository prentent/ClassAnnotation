package com.lh.classannotation;

import android.app.Activity;

public class Butterknife {
    public static void bind(Activity activity) {
        bind(activity, activity);
    }

    public static void bind(Object host, Object root) {
        Class<?> clazz = host.getClass();
        String proxyClassFullName = clazz.getName() + "$$ViewInjector";
        Class<?> proxyClazz = null;
        try {
            proxyClazz = Class.forName(proxyClassFullName);
            ViewInjector instance = (ViewInjector) proxyClazz.newInstance();
            instance.inject(host, root);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }


}
