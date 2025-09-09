package cc.anqin.processor;

import cc.anqin.processor.util.CollectFields;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.*;

import static cc.anqin.processor.constant.Constant.GENERATE_NAME_SUFFIX;
import static cc.anqin.processor.constant.Constant.PACKAGE_PREFIX;


@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MapConverterProcessor extends AbstractProcessor {

    private final Map<String, String> converterRegistry = new HashMap<>();

    Log log = LogFactory.get(MapConverterProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 查找所有被 @AutoToMap 标记的注解类型

        for (TypeElement annotationType : annotations) {
            // 检查当前注解类型是否被 @AutoToMap 标记
            if (isAutoToMap(annotationType)) {
                log.info("Found @AutoToMap annotation: " + annotationType.getQualifiedName());
                processAutoToMap(annotationType, roundEnv);
            }
        }

        // 在所有注解处理完成后生成注册表
        if (roundEnv.processingOver()) {
            generateJsonRegistryFile();
        }

        return true;
    }

    private void processAutoToMap(TypeElement annotationType, RoundEnvironment roundEnv) {
        log.info("Processing annotation: " + annotationType.getQualifiedName());

        // 找到了一个衍生注解（@AutoToMap），获取所有被它标记的类
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationType);
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            // 获取用户注解上的参数
            String[] excludeFields = getExcludeFieldsFromAnnotation(classElement, annotationType);

            Pair<TypeElement, TypeElement> mappingPair = getMappingFromAnnotation(classElement, annotationType);

            String qualifiedName = generateClass(mappingPair, excludeFields);

            // 注册转换器信息
            converterRegistry.put(classElement.getQualifiedName().toString(), qualifiedName);
        }
    }


    /**
     * 检查给定的注解元素是否是 @AutoToMap
     */
    private boolean isAutoToMap(TypeElement annotationElement) {
        String autoToMapCompilerName = "cc.anqin.processor.annotation.AutoToMap";
        return annotationElement.getQualifiedName().toString().equals(autoToMapCompilerName);
    }

    /**
     * 从注解中获取排除字段列表
     */
    private String[] getExcludeFieldsFromAnnotation(Element element, TypeElement annotationType) {
        // 优先从用户注解获取排除字段
        @SuppressWarnings("unchecked")
        List<AnnotationValue> excludeValues =
                (List<AnnotationValue>)
                        getFromAnnotationValue(element, annotationType, "exclude");

        if (excludeValues == null || excludeValues.isEmpty()) {
            return new String[0];
        }
        String[] excludes = new String[excludeValues.size()];
        for (int i = 0; i < excludeValues.size(); i++) {
            excludes[i] = excludeValues.get(i).getValue().toString();
        }
        String name = annotationType.getQualifiedName().toString();
        log.info(StrUtil.format("{} Excluding fields: {}", name, Arrays.toString(excludes)));
        return excludes;
    }


    private Pair<TypeElement, TypeElement> getMappingFromAnnotation(TypeElement element, TypeElement annotationType) {
        TypeMirror value = (TypeMirror) getFromAnnotationValue(element, annotationType, "mapping");
        TypeElement mapping;
        Pair<TypeElement, TypeElement> pair;
        if (value == null || value.toString().equals(Object.class.getName())) {
            mapping = processingEnv.getElementUtils().getTypeElement(element.getQualifiedName().toString());
            pair = Pair.of(null, mapping);
        } else {
            mapping = (TypeElement) processingEnv.getTypeUtils().asElement(value);
            pair = Pair.of(element, mapping);
        }
        log.info(StrUtil.format("{} Mapping to: {}", element.getSimpleName(), mapping.getSimpleName()));
        return pair;
    }


    private Object getFromAnnotationValue(Element element, TypeElement annotationType, String fieldName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (processingEnv.getTypeUtils().isSameType(mirror.getAnnotationType().asElement().asType(), annotationType.asType())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                    if (fieldName.equals(entry.getKey().getSimpleName().toString())) {
                        return entry.getValue().getValue();
                    }
                }
            }
        }
        return null;
    }


    /**
     * 为给定的类型元素生成转换器实现类
     */
    private String generateClass(Pair<TypeElement, TypeElement> mappingPair, String[] excludeFields) {

        TypeElement typeElement = mappingPair.getValue();

        TypeElement mappingElement = mappingPair.getKey();

        String className = Opt.ofNullable(mappingElement)
                .map(t -> GENERATE_NAME_SUFFIX.apply(t.getSimpleName().toString() + "_mapping_" + typeElement.getSimpleName().toString()))
                .orElse(GENERATE_NAME_SUFFIX.apply(typeElement.getSimpleName().toString()));

        String source = Opt.ofNullable(mappingElement)
                .map(t -> t.getQualifiedName().toString())
                .orElse(typeElement.getQualifiedName().toString());

        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).toString();


        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "Generating converter for: " + typeElement.getQualifiedName());

        // 创建 @Generated 注解
        AnnotationSpec componentAnnotation = AnnotationSpec
                .builder(ClassName.get("cc.anqin.processor.annotation", "Generated"))
                .addMember("value", "$S", "cc.anqin.processor.MapConverterProcessor")
                .addMember("date", "$S", LocalDateTime.now())
                .addMember("comments", "$S", "Generated by MapConverterProcessor; Introduction: https://anqin.cc/")
                .addMember("repository", "$S", "https://github.com/anqinworks/auto-mapping-map")
                .addMember("source", "$S", source)
                .build();

        // 创建实现 MappingConvert 接口的类
        TypeSpec mapConverterClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(componentAnnotation)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get("cc.anqin.processor.base", "MappingConvert"),
                        TypeName.get(typeElement.asType())
                ))
                .addMethod(toMap(typeElement, excludeFields))
                .addMethod(toBean(typeElement, excludeFields))
                .build();

        log.info(StrUtil.format("Generating class: {}", className));

        write(PACKAGE_PREFIX + packageName, mapConverterClass);

        return PACKAGE_PREFIX + packageName + "." + className;
    }

    /**
     * 生成toMap方法的实现
     */
    private MethodSpec toMap(TypeElement typeElement, String[] excludeFields) {
        MethodSpec.Builder toMapBuilder = MethodSpec.methodBuilder("toMap")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(Map.class, String.class, Object.class))
                .addParameter(TypeName.get(typeElement.asType()), "entity")
                .beginControlFlow("if (entity == null)")
                .addStatement("    return $T.emptyMap()", ClassName.get("java.util", "Collections"))
                .endControlFlow()
                .addStatement("$T<String, Object> map = new $T<>()", Map.class, HashMap.class);

        // 传递排除字段给收集器
        CollectFields.toMapCollectFields(typeElement, toMapBuilder, processingEnv, excludeFields);

        toMapBuilder.addStatement("return map");
        return toMapBuilder.build();
    }

    /**
     * 生成toBean方法的实现
     */
    private MethodSpec toBean(TypeElement typeElement, String[] excludeFields) {
        TypeMirror targetType = typeElement.asType();
        MethodSpec.Builder toBeanMethodBuilder = MethodSpec.methodBuilder("toBean")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ParameterizedTypeName.get(Map.class, String.class, Object.class), "dataMap")
                .returns(TypeVariableName.get(targetType))
                .beginControlFlow("if ($T.isEmpty(dataMap))", ClassName.get("cn.hutool.core.collection", "CollUtil"))
                .addStatement("    return new $T()", targetType)
                .endControlFlow()
                .addStatement("$T bean = new $T()", targetType, targetType);

        // 传递排除字段给收集器
        CollectFields.toBeanCollectFields(typeElement, toBeanMethodBuilder, processingEnv, excludeFields);

        toBeanMethodBuilder.addStatement("return bean");
        return toBeanMethodBuilder.build();
    }

    /**
     * 将生成的类写入文件
     */
    private void write(String packageName, TypeSpec mapConverterClass) {
        JavaFile javaFile = JavaFile.builder(packageName, mapConverterClass).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate converter class: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成转换器注册表的JSON文件
     */
    private void generateJsonRegistryFile() {
        if (converterRegistry.isEmpty()) {
            return;
        }

        try {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\n");

            boolean first = true;
            for (Map.Entry<String, String> entry : converterRegistry.entrySet()) {
                if (!first) {
                    jsonBuilder.append(",\n");
                }
                jsonBuilder.append("  \"")
                        .append(escapeJsonString(entry.getKey()))
                        .append("\": \"")
                        .append(escapeJsonString(entry.getValue()))
                        .append("\"");
                first = false;
            }

            jsonBuilder.append("\n}");

            FileObject fileObject = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/map-converter-registry.json"
            );

            try (Writer writer = fileObject.openWriter()) {
                writer.write(jsonBuilder.toString());
            }

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    "Generated JSON registry with " + converterRegistry.size() + " entries");

        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate JSON registry: " + e.getMessage());
        }
    }

    /**
     * 对JSON字符串进行转义处理
     */
    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}