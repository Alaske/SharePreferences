package com.jsweb.pen.compiler;

import com.google.auto.service.AutoService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by zhaocheng on 2018/4/28.
 */
@AutoService(Processor.class)
public class Processor extends AbstractProcessor {

    private Filer mFileUtils;
    private Elements mElementUtils;
    private Messager mMessager;
    private Types typeUtils = null;

    private SPProcessor spProcessor;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.addAll(spProcessor.getSupportedAnnotationTypes());
        return annotationTypes;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFileUtils = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();
        spProcessor = new SPProcessor();
        spProcessor.init(mFileUtils, mElementUtils, mMessager, typeUtils);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            spProcessor.process(set, roundEnvironment);
            return spProcessor.writeFile();

        } catch (IllegalArgumentException e) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "error" + "  " + e.getMessage());
        }

        return true;
    }
}
