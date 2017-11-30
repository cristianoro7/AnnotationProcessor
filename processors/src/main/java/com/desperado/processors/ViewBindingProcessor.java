package com.desperado.processors;

import com.desperado.annotations.BindView;
import com.google.auto.service.AutoService;

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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by desperado on 17-11-28.
 */
@AutoService(Processor.class)
public class ViewBindingProcessor extends AbstractProcessor {

    private Types types;

    private Filer filer;

    private Messager messager;

    private Elements elements;

    private Map<String, ViewClass> map = new HashMap<>();

    private static final String TYPE_ACTIVITY = "android.app.Activity";

    private static final String TYPE_VIEW = "android.view.View";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        types = processingEnvironment.getTypeUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        map.clear(); //该方法会被调用多次, 所以每次进入时, 首先清空之前的脏数据
        logNote("start process");
        for (Element e : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            if (!isValid(e)) {
                return true; //出现错误, 停止编译
            }
            logNote("start parse annotations");
            performParseAnnotations(e);
        }
        logNote("start generate code");
        generateCode();
        return true;
    }

    private boolean isValid(Element element) {
        if (!(element instanceof VariableElement)) {
            logError("BindView只能标注字段");
            return false;
        }

        VariableElement variableElement = (VariableElement) element;
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();

        if (typeElement.getKind() != ElementKind.CLASS) {
            logError("只能标注类中的字段");
            return false;
        }
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            logError("不能标注抽象类中的字段");
            return false;
        }

        for (Modifier modifier : element.getModifiers()) {
            if (modifier == Modifier.PRIVATE || modifier == Modifier.STATIC ||
                    modifier == Modifier.FINAL) {
                logError("BindView不能标注被static," +
                        "private或者final的字段");
                return false;
            }
        }

        if (!isSubtype(typeElement.asType(), TYPE_ACTIVITY)) {
            logError(typeElement.getSimpleName() + "必须是 Activity的子类");
            return false;
        }
        if (!isSubtype(variableElement.asType(), TYPE_VIEW)) {
            logError("BindView只能标注View的子类");
            return false;
        }
        return true;
    }

    private boolean isSubtype(TypeMirror tm, String type) {
        boolean isSubType = false;
        while (tm != null) {
            if (type.equals(tm.toString())) {
                isSubType = true;
                break;
            }
            TypeElement superTypeElem = (TypeElement) types.asElement(tm);
            if (superTypeElem != null) {
                tm = superTypeElem.getSuperclass();
            } else { //如果为空, 说明没了父类, 所以直接退出
                break;
            }
        }
        return isSubType;
    }

    private void performParseAnnotations(Element element) {

        VariableElement variableElement = (VariableElement) element;
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();


        String className = typeElement.getSimpleName().toString();

        ViewClass viewClass = map.get(className);
        ViewField field = new ViewField(variableElement);

        if (viewClass == null) {
            viewClass = new ViewClass(elements.getPackageOf(variableElement).getQualifiedName().toString(),
                    className, typeElement);
            map.put(className, viewClass);
        }
        viewClass.addField(field);
    }

    private void generateCode() {
        for (ViewClass vc : map.values()) {
            try {
                vc.generateCode().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                logError("error in parse");
            }
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        return annotations;
    }


    private void logError(String msg) {
        log(Diagnostic.Kind.ERROR, msg);
    }

    private void logNote(String msg) {
        log(Diagnostic.Kind.NOTE, msg);
    }

    private void log(Diagnostic.Kind kind, String msg) {
        messager.printMessage(kind, msg);
    }
}
