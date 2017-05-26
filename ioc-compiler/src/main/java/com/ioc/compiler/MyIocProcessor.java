package com.ioc.compiler;

import com.google.auto.service.AutoService;
import com.np.annotation.MyBindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class MyIocProcessor extends AbstractProcessor {

    /** 用来对程序元素进行操作的实用工具方法 如: 获取元素的包,成员变量等. */
    private Elements mElementUtils;
    /** 支持通过注解处理器创建新文件 */
    private Filer mFiler;
    /** 提供注解处理器用来报告错误消息、警告和其他通知的方式 */
    private Messager mMessager;
    /** key 为全类名, value 为 ProxyInfo */
    private Map<String, ProxyInfo> mProxyInfoMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(MyBindView.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mProxyInfoMap.clear();
        // 1、收集信息
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MyBindView.class);
        for (Element element : elements) {
            if (!checkElementValid(element, MyBindView.class)) {
                return false;
            }
            VariableElement variableElement = (VariableElement) element;
            // 获取元素的最里层元素,这里成员变量获取的为类元素.类元素获取的是包...
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            // 获得全类名.
            String qualifiedName = typeElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyInfoMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyInfoMap.put(qualifiedName, proxyInfo);
            }

            MyBindView annotation = variableElement.getAnnotation(MyBindView.class);
            int viewId = annotation.value();
            proxyInfo.variableElementMap.put(viewId, variableElement);
        }

        // 2、生成代理类源码
        for (String qualifiedName : mProxyInfoMap.keySet()) {
            ProxyInfo proxyInfo = mProxyInfoMap.get(qualifiedName);
            String proxyClassName = proxyInfo.getProxyClassName();
            try {
                // 创建类
                // 需要实现的接口
                ClassName interfaceName = ClassName.get("com.np.ioc", "Injector");
                // 实现接口的泛型
                ClassName interfaceGeneric = ClassName.bestGuess(qualifiedName);
                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(proxyClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ParameterizedTypeName.get(interfaceName, interfaceGeneric));

                // 创建方法
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("inject")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addParameter(interfaceGeneric, "host")
                        .addParameter(Object.class, "source");
                Map<Integer, VariableElement> variableElementMap = proxyInfo.variableElementMap;
                for (Integer viewId : variableElementMap.keySet()) {
                    VariableElement variableElement = variableElementMap.get(viewId);
                    String name = variableElement.getSimpleName().toString();
                    String type = variableElement.asType().toString();

                    methodBuilder.beginControlFlow("if (source instanceof android.app.Activity)")
                            .addStatement("host.$L = ($L) (((android.app.Activity) source).findViewById($L))",
                                    name, type, viewId)
                            .endControlFlow()
                            .beginControlFlow("else")
                            .addStatement("host.$L = ($L) (((android.view.View) source).findViewById($L))",
                                    name, type, viewId)
                            .endControlFlow();
                }
                // 创建类成功.
                TypeSpec injectorClass = typeBuilder.addMethod(methodBuilder.build()).build();
                // 创建 java 源文件.
                JavaFile javaFile = JavaFile.builder("com.np.ioc_example", injectorClass)
                        .build();
                // 通过 Filer 生成 java 源文件.
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /** 检查元素的有效性,不能是私有类型的 和 只能是成员变量. */
    private boolean checkElementValid(Element element, Class<MyBindView> annotationClass) {
        if (element.getKind() != ElementKind.FIELD) {
            error(element, "%s must be declared on Field.", annotationClass.getSimpleName());
            return false;
        } else if (isPrivate(element)) {
            error(element, "%s can not be private.", element.getSimpleName());
            return false;
        }
        return true;
    }

    /** 判断元素的修饰符是否为私有的. */
    private boolean isPrivate(Element element) {
        return element.getModifiers().contains(Modifier.PRIVATE);
    }

    /** 输出错误信息 */
    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
