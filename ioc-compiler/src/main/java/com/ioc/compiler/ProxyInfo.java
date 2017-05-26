package com.ioc.compiler;


import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

class ProxyInfo {

    private static final String PROXY = "_ViewInject";

    /** 包名 */
    private String packageName;
    /** 代理类名称 */
    private String proxyClassName;
    private TypeElement typeElement;

    /** key 为 viewId, value 为 VariableElement. */
    Map<Integer, VariableElement> variableElementMap = new HashMap<>();

    ProxyInfo(Elements elements, TypeElement typeElement) {
        this.typeElement = typeElement;
        PackageElement packageElement = elements.getPackageOf(typeElement);
        // 如 com.np.ioc
        String packageName = packageElement.getQualifiedName().toString();
        // MainActivity$A
        String className = getClassName(packageName, typeElement);
        // 代理类名: MainActivity$A_ViewInject
        this.proxyClassName = className + PROXY;
        this.packageName = packageName;
    }

    /** 获取类名 */
    private String getClassName(String packageName, TypeElement typeElement) {
        int length = packageName.length() + 1;
        // 如 com.np.ioc.MainActivity.A
        String classQualifiedName = typeElement.getQualifiedName().toString();
        // 如 MainActivity.A
        String name = classQualifiedName.substring(length);
        // 需要返回的是 MainActivity$A
        return name.replace(".", "$");
    }

    /** 获取代理类名 */
    String getProxyClassName() {
        return this.proxyClassName;
    }

    /** 获取包名 */
    String getPackageName() {
        return this.packageName;
    }

    /** 获取 TypeElement */
    TypeElement getTypeElement() {
        return this.typeElement;
    }

}
