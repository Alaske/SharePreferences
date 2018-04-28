package com.jsweb.pen.compiler;

import com.abcpen.zc.convert.ISPConvert;
import com.abcpen.zc.sp.annotaion.SPData;
import com.abcpen.zc.sp.annotaion.SPItem;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by zhaocheng on 2018/4/28.
 */

public class SPProcessor {

    public static final String PACKAGE_NAME = "com.zc.util";
    public static final String CLASS_NAME = "SharePreferencesUtil";
    public static final String CLASS_INNER_NAME = "SharePreferencesHolder";
    public static final String GET_SHAREDPREFERENCES = "getSharedPreferences";

    private Filer mFileUtils;
    private Elements mElementUtils;
    private Messager mMessager;
    private Types mTypeUtils = null;
    private TypeSpec.Builder classSpec;


    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(SPData.class.getCanonicalName());
        annotationTypes.add(SPItem.class.getCanonicalName());
        return annotationTypes;
    }

    public void init(Filer filer, Elements elements, Messager messager, Types types) {
        this.mFileUtils = filer;
        this.mElementUtils = elements;
        this.mMessager = messager;
        this.mTypeUtils = types;
    }


    public void process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        processRegisterSPData(roundEnvironment);
        processRegisterSPItem(roundEnvironment);

    }

    private void processRegisterSPItem(RoundEnvironment element) {
        if (classSpec != null) {
            for (Element item : element.getElementsAnnotatedWith(SPItem.class)) {
                List<MethodSpec> methodSpecs = progressItem(item);
                for (MethodSpec methodSpec : methodSpecs) {
                    classSpec.addMethod(methodSpec);
                }
            }

        }

    }

    private void processRegisterSPData(RoundEnvironment element) {
        for (Element item : element.getElementsAnnotatedWith(SPData.class)) {
            SPData annotation = item.getAnnotation(SPData.class);
            classSpec = createSPData(annotation);
            break;
        }

    }


    public String upperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private List<MethodSpec> progressItem(Element item) {

        List<MethodSpec> methodSpecs = new ArrayList<>();

        SPItem items = item.getAnnotation(SPItem.class);
        addStatement(item, methodSpecs, items);


        return methodSpecs;


    }

    private void addStatement(Element item, List<MethodSpec> methodSpecs, SPItem items) {

        TypeMirror typeMirror = item.asType();
        TypeName typeName = TypeName.get(typeMirror);
        if (item instanceof TypeElement) {
            TypeMirror myValue2 = getMyValue2((TypeElement) item);
            if (myValue2 != null) {
                typeName = TypeName.get(myValue2);
            }
        }

        String key = items.key();
        if (key.length() == 0) {
            key = item.getSimpleName().toString();
        }
        MethodSpec.Builder setBuilder =
                MethodSpec.methodBuilder("set" + upperCase(key)).addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder getBuilder =
                MethodSpec.methodBuilder("get" + upperCase(key)).addModifiers(Modifier.PUBLIC)
                        .returns(typeName);

        //基本类型

        String setStatement = "";
        String getStatement = "";
        if (typeName.equals(TypeName.BOOLEAN)) {
            //boolean
            setStatement = "$L().edit().putBoolean($S,$L).apply()";
            getStatement = "return $L().getBoolean($S,false)";
        } else if (typeName == TypeName.CHAR) {
            //char
            setStatement = "$L().edit().putString($S,$L).apply()";
            getStatement = "return $L.getString($S,\"\")";
        } else if (typeName == TypeName.DOUBLE) {
            //double
            setStatement = "$L().edit().putString($S,$L).apply()";
            getStatement = "return (Double)($L.getString($S,\"0.00\"))";
        } else if (typeName == TypeName.INT) {
            setStatement = "$L().edit().putInt($S,$L).apply()";
            getStatement = "return $L().getInt($S,0)";
        } else if (typeName == TypeName.LONG) {
            setStatement = "$L().edit().putLong($S,$L).apply()";
            getStatement = "return $L().getLong($S,0)";
        } else if (typeName == TypeName.FLOAT) {
            setStatement = "$L().edit().putFloat($S,$L).apply()";
            getStatement = "return $L().getFloat($S,0f)";
        } else if (typeName.equals(ClassName.get("java.lang", "String"))) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "==============================");
            setStatement = "$L().edit().putString($S,$L).apply()";
            getStatement = "return $L().getString($S,\"\")";
        }
        if (setStatement.length() > 0 && getStatement.length() > 0) {
            setBuilder.addParameter(typeName, key);
            setBuilder.addStatement(setStatement, GET_SHAREDPREFERENCES, key, key);
            getBuilder.addStatement(getStatement, GET_SHAREDPREFERENCES, key);

        } else {
            TypeMirror value = null;
            if (item.getKind() == ElementKind.CLASS) {
                try {
                    Class<? extends ISPConvert> convert = items.convert();
                } catch (MirroredTypeException me) {
                    value = me.getTypeMirror();
                }

                setBuilder.addParameter(typeName, "param");
                setStatement = "$L().edit().putString($S,new $T().convertToObject(param))";
                setBuilder.addStatement(setStatement, GET_SHAREDPREFERENCES, key, value);


                getStatement = "String data= $L().getString($S,\"\")";
                String getStatement2 = "return new $T<$T>().unConvertData(data,$T.class)";

                getBuilder.addStatement(getStatement, GET_SHAREDPREFERENCES, key);
                getBuilder.addStatement(getStatement2, value, typeName, typeName);

            }
        }


        methodSpecs.add(setBuilder.build());
        methodSpecs.add(getBuilder.build());
    }


    private TypeSpec.Builder createSPData(SPData annotation) {
        String name = annotation.name();
        int mode = annotation.mode();

        classSpec = TypeSpec.classBuilder(ClassName.get(PACKAGE_NAME, CLASS_NAME))
                .addModifiers(Modifier.PUBLIC)
                .addField(TypeUtils.CONTEXT, "mContext", Modifier.STATIC, Modifier.PRIVATE)
                .addMethod(getSPDataConstruction())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("java.lang", "SuppressWarnings")).addMember("value", "$S", "unchecked").build())
                .addMethod(getSharePreferences(name, mode))
                .addMethod(getInitSPData())
                .addMethod(getInstanceMethod())
                .addType(TypeSpec.classBuilder(CLASS_INNER_NAME)
                        .addModifiers(Modifier.STATIC)
                        .addField(FieldSpec.builder(ClassName.get(PACKAGE_NAME, CLASS_NAME), "holder",
                                Modifier.FINAL, Modifier.STATIC).initializer("new $T()", ClassName.get(PACKAGE_NAME, CLASS_NAME)).build())
                        .build());

        return classSpec;


    }

    private MethodSpec getSharePreferences(String name, int mode) {
        MethodSpec.Builder getSharedPreferences = MethodSpec.methodBuilder(GET_SHAREDPREFERENCES)
                .returns(TypeUtils.SHAREPREFERENCES)
                .addStatement("return mContext.getSharedPreferences($S,$L)", name, mode);
        return getSharedPreferences.build();
    }


    public MethodSpec getSPDataConstruction() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    public MethodSpec getInitSPData() {
        return MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TypeUtils.CONTEXT, "context")
                .addStatement("mContext = context.getApplicationContext()")
                .build();
    }

    public MethodSpec getInstanceMethod() {
        return MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(PACKAGE_NAME, CLASS_NAME))
                .addStatement("return SharePreferencesHolder.holder")
                .build();
    }


    private AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, "*************item " + entry.getKey().getSimpleName() + "****" + entry);
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }


    public TypeMirror getMyValue2(TypeElement foo) {
        AnnotationMirror am = getAnnotationMirror(foo, SPItem.class);
        if (am == null) {
            return null;
        }
        AnnotationValue av = getAnnotationValue(am, "valueType");
        if (av == null) {
            return null;
        } else {
            return (TypeMirror) av.getValue();
        }
    }

    public boolean writeFile() {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "*******writeFile******");

        if (classSpec != null) {
            try {
                JavaFile.builder(PACKAGE_NAME, classSpec.build()).build().writeTo(mFileUtils);
            } catch (Exception e) {
                e.printStackTrace();
                mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
            classSpec = null;
            return true;
        }
        return false;
    }
}
