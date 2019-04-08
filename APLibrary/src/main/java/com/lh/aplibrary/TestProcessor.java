package com.lh.aplibrary;

import com.google.auto.service.AutoService;
import com.lh.annomations.BindView;
import com.lh.annomations.OnClick;

import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class TestProcessor extends AbstractProcessor {

    private Filer mFiler; //跟文件相关的辅助类，生成JavaSourceCode.
    private Messager mMessager; //跟日志相关的辅助类。
    private Elements mMlementUtils; //跟元素相关的辅助类，帮助我们去获取一些元素相关的信息。
    private Map<String, ProxyInfo> proxyInfoMap = new HashMap<String, ProxyInfo>();//一个类信息的存放集合

    //获取一些信息
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mMlementUtils = processingEnvironment.getElementUtils();
    }

    //返回注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        hashSet.add(BindView.class.getCanonicalName());
        hashSet.add(OnClick.class.getCanonicalName());
        return hashSet;
    }

    //返回支持的源码版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //主要的方法
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        proxyInfoMap.clear();

        if (getFieldData(roundEnvironment)) return true;
        if (getMethodData(roundEnvironment)) return true;
        putFile();
        return true;
    }

    private void putFile() {
        //生成文件
        for (String key : proxyInfoMap.keySet()) {
            ProxyInfo proxyInfo = proxyInfoMap.get(key);
            JavaFileObject sourceFile = null;
            try {
                sourceFile = mFiler.createSourceFile(
                        proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement());
                Writer writer = sourceFile.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "注入失败 %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }

        }
    }

    private boolean getFieldData(RoundEnvironment roundEnvironment) {
        //获取捕获到的BindView注解集合
        Set<? extends Element> elementsBindView = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //一、收集BindView注解信息
        for (Element element : elementsBindView) {
            //检查element类型,如果不为成员变量。跳过本次循环
            if (element.getKind() != ElementKind.FIELD) {
                return true;
            }
            //强转为成员变量类型，上面已经判断为成员变量类型了
            VariableElement variableElement = (VariableElement) element;
            //获取类类型
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            //判断该类是否已经在HashMap集合存在
            ProxyInfo proxyInfo = proxyInfoMap.get(qualifiedName);
            //不存在，则添加进去
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mMlementUtils, typeElement);
                proxyInfoMap.put(qualifiedName, proxyInfo);
            }
            //获取绑定的BindView的值
            BindView annotation = variableElement.getAnnotation(BindView.class);
            int id = annotation.value();
            //根据id吧所有注册了BindView的成员变量存放进ProxyInfo类里的Map集合里
            proxyInfo.injectVariables.put(id, variableElement);
        }
        return false;
    }

    private boolean getMethodData(RoundEnvironment roundEnvironment) {
        //获取到捕获到的OnClick注解集合
        Set<? extends Element> elementsOnClick = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        //一、收集BindView注解信息
        for (Element element : elementsOnClick) {
            //检查element类型,如果不为成员变量。跳过本次循环
            if (element.getKind() != ElementKind.METHOD) {
                return true;
            }
            //强转为方法类型，上面已经判断为方法类型了
            ExecutableElement executableElement = (ExecutableElement) element;
            //获取类类型
            TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            //判断该类是否已经在HashMap集合存在
            ProxyInfo proxyInfo = proxyInfoMap.get(qualifiedName);
            //不存在，则添加进去
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mMlementUtils, typeElement);
                proxyInfoMap.put(qualifiedName, proxyInfo);
            }
            //获取绑定的OnClick的值
            OnClick annotation = executableElement.getAnnotation(OnClick.class);
            int id = annotation.value();
            //根据id吧所有注册了OnClick的成员变量存放进ProxyInfo类里的Map集合里
            proxyInfo.injectExecutable.put(id, executableElement);
        }
        return false;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
