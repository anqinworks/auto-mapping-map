package cc.anqin.processor.util;

import cc.anqin.processor.annotation.AutoKeyMapping;
import cc.anqin.processor.annotation.IgnoreToBean;
import cc.anqin.processor.annotation.IgnoreToMap;
import cc.anqin.processor.enums.MappingEnum;
import cn.hutool.core.util.StrUtil;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

/**
 * 字段收集工具类
 * <p>
 * 该工具类负责在代码生成过程中收集实体类的字段信息，并生成相应的转换代码。
 * 主要用于支持{@link cc.anqin.processor.MapConverterProcessor}在生成转换器实现类时，
 * 处理实体类字段与Map之间的映射关系。
 * </p>
 * <p>
 * 该类提供了两个主要方法：
 * <ul>
 *   <li>{@link #toMapCollectFields} - 收集字段并生成对象到Map的转换代码</li>
 *   <li>{@link #toBeanCollectFields} - 收集字段并生成Map到对象的转换代码</li>
 * </ul>
 * </p>
 * <p>
 * 在处理字段时，会考虑以下注解的影响：
 * <ul>
 *   <li>{@link AutoKeyMapping} - 控制字段映射方向和自定义键名</li>
 *   <li>{@link IgnoreToMap} - 忽略字段在对象到Map的转换中</li>
 *   <li>{@link IgnoreToBean} - 忽略字段在Map到对象的转换中</li>
 * </ul>
 * </p>
 *
 * @author Mr.An
 * @since 2025/03/06
 * @see cc.anqin.processor.MapConverterProcessor
 * @see AutoKeyMapping
 * @see IgnoreToMap
 * @see IgnoreToBean
 */
public class CollectFields {


    /**
     * 递归收集类及其父类的字段，并生成对象到Map的转换代码
     * <p>
     * 该方法遍历给定类型及其所有父类的字段，为每个符合条件的字段生成从对象到Map的转换代码。
     * 在处理过程中，会考虑字段上的{@link AutoKeyMapping}和{@link IgnoreToMap}注解，
     * 以决定字段是否参与转换以及在Map中使用的键名。
     * </p>
     * <p>
     * 生成的代码示例：
     * <pre>
     * {@code
     * map.put("fieldName", entity.getFieldName());
     * }
     * </pre>
     * </p>
     *
     * @param typeElement   要处理的类型元素
     * @param toMapBuilder  用于构建toMap方法的JavaPoet方法构建器
     * @param processingEnv 提供处理工具的环境
     */
    public static void toMapCollectFields(TypeElement typeElement, MethodSpec.Builder toMapBuilder, ProcessingEnvironment processingEnv) {
        // 获取当前类的字段
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        for (Element enclosed : enclosedElements) {
            if (enclosed instanceof VariableElement) {
                VariableElement field = (VariableElement) enclosed;

                // 跳过 final 字段
                if (field.getModifiers().contains(Modifier.FINAL)) {
                    continue;
                }

                if (field.getAnnotation(IgnoreToMap.class) != null) {
                    continue;
                }

                // 检查字段是否被 @AutoKeyMapping 注解标记并获取目标名称
                AutoKeyMapping annotation = field.getAnnotation(AutoKeyMapping.class);

                // 跳过被标记为忽略的字段
                if (annotation != null
                        && annotation.ignore()
                        && (MappingEnum.ALL.equals(annotation.method())
                        || MappingEnum.TO_MAP.equals(annotation.method()))) {
                    continue;
                }


                String fieldName = field.getSimpleName().toString();

                // 确定目标字段名：优先取 annotation.target，否则默认使用 fieldName
                String targetFieldName = Optional.ofNullable(annotation)
                        .map(AutoKeyMapping::target)
                        .filter(StrUtil::isNotBlank)
                        .orElse(fieldName);

                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

                // 使用 getter 方法获取字段值并放入 Map
                toMapBuilder.addStatement("map.put($S, entity.$L())", targetFieldName, getterName);
            }
        }

        // 递归获取父类字段
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals("java.lang.Object")) {
            Element superElement = processingEnv.getTypeUtils().asElement(superclass);
            if (superElement instanceof TypeElement) {
                toMapCollectFields((TypeElement) superElement, toMapBuilder, processingEnv);
            }
        }
    }

    /**
     * 递归收集类及其父类的字段，并生成Map到对象的转换代码
     * <p>
     * 该方法遍历给定类型及其所有父类的字段，为每个符合条件的字段生成从Map到对象的转换代码。
     * 在处理过程中，会考虑字段上的{@link AutoKeyMapping}和{@link IgnoreToBean}注解，
     * 以决定字段是否参与转换以及从Map中获取值的键名。
     * </p>
     * <p>
     * 生成的代码示例：
     * <pre>
     * {@code
     * bean.setFieldName((FieldType) dataMap.get("fieldName"));
     * }
     * </pre>
     * </p>
     *
     * @param typeElement         要处理的类型元素
     * @param toBeanMethodBuilder 用于构建toBean方法的JavaPoet方法构建器
     * @param processingEnv       提供处理工具的环境
     */
    public static void toBeanCollectFields(TypeElement typeElement, MethodSpec.Builder toBeanMethodBuilder, ProcessingEnvironment processingEnv) {

        // 动态生成 set 方法调用
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) { // 只处理字段
                VariableElement field = (VariableElement) element;

                // 跳过 final 字段
                if (field.getModifiers().contains(Modifier.FINAL)) {
                    continue;
                }

                if (field.getAnnotation(IgnoreToBean.class) != null) {
                    continue;
                }


                // 检查字段是否被 @AutoKeyMapping 注解标记并获取目标名称
                AutoKeyMapping annotation = field.getAnnotation(AutoKeyMapping.class);

                // 跳过被标记为忽略的字段
                if (annotation != null
                        && annotation.ignore()
                        && (MappingEnum.ALL.equals(annotation.method())
                        || MappingEnum.TO_BEAN.equals(annotation.method()))) {
                    continue;
                }

                String fieldName = field.getSimpleName().toString();
                TypeName fieldType = TypeName.get(field.asType());

                // 生成 set 方法调用
                String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                toBeanMethodBuilder.addStatement("bean.$L(($T) dataMap.get(\"$L\"))", setMethodName, fieldType, fieldName);
            }
        }

        // 递归获取父类字段
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals("java.lang.Object")) {
            Element superElement = processingEnv.getTypeUtils().asElement(superclass);
            if (superElement instanceof TypeElement) {
                toBeanCollectFields((TypeElement) superElement, toBeanMethodBuilder, processingEnv);
            }
        }
    }
}
