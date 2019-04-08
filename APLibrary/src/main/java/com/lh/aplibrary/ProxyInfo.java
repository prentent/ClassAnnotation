package com.lh.aplibrary;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class ProxyInfo {
    private String packageName;
    private String proxyClassName;
    private TypeElement typeElement;

    public Map<Integer, VariableElement> injectVariables = new HashMap<>();
    public Map<Integer, ExecutableElement> injectExecutable = new HashMap<>();

    public static final String PROXY = "ViewInjector";

    public ProxyInfo(Elements elementUtils, TypeElement classElement) {
        this.typeElement = classElement;
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        //获取类名
        String className = getClassName(classElement, packageName);
        this.packageName = packageName;
        this.proxyClassName = className + "$$" + PROXY;
    }


    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!\n");
        builder.append("package ")
                .append(packageName)
                .append(";\n\n");
        builder.append("import com.lh.aplibrary.*;\n\n");
        builder.append("public class ")
                .append(proxyClassName)
                .append(" implements " + ProxyInfo.PROXY + "<")
                .append(typeElement.getQualifiedName())
                .append(">{\n");
        generateMethods(builder);
        builder.append("}");
        return builder.toString();

    }


    private void generateMethods(StringBuilder builder) {

        builder.append("    @Override\n ");
        builder.append("    public void inject(").append(typeElement.getQualifiedName()).append(" activity, Object obj ) {\n");
        for (int id : injectVariables.keySet()) {
            VariableElement element = injectVariables.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("    if(source instanceof android.app.Activity){\n");
            builder.append("        activity.").append(name)
                    .append(" = ");
            builder.append("(").append(type)
                    .append(")(((android.app.Activity)obj).findViewById( ")
                    .append(id).append("));\n");
            builder.append("    }else{\n");
            builder.append("        activity.")
                    .append(name).append(" = ");
            builder.append("(")
                    .append(type)
                    .append(")(((android.view.View)obj).findViewById( ")
                    .append(id)
                    .append("));\n");
            builder.append("    };\n\n");
        }
        for (int id : injectExecutable.keySet()) {
            //ExecutableElement element = injectExecutable.get(id);
            builder.append("    if(source instanceof android.app.Activity){\n");
            builder.append("        ((android.app.Activity)obj).findViewById( ")
                    .append(id)
                    .append(").setOnClickListener(view -> {((")
                    .append(typeElement.getQualifiedName())
                    .append(")source).OnClick(view);});\n");
            builder.append("   }else{\n");
            builder.append("        ((android.view.View)obj).findViewById( ")
                    .append(id)
                    .append(").setOnClickListener(view -> {((")
                    .append(typeElement.getQualifiedName())
                    .append(")source).OnClick(view);});\n");
            builder.append("    };\n\n");
        }
        builder.append("  }\n\n");


    }

    public String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen)
                .replace('.', '$');
    }
}
