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
 * 收集字段
 *
 * @author Mr.An
 * @since 2025/03/06
 */
public class CollectFields {


    /**
     * 递归收集类及其父类的字段
     *
     * @param typeElement   类型元素
     * @param toMapBuilder  致地图构建者
     * @param processingEnv 处理环境
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
     * 去收豆田
     *
     * @param typeElement         类型元素
     * @param toBeanMethodBuilder to bean方法生成器
     */
    public static void toBeanCollectFields(TypeElement typeElement, MethodSpec.Builder toBeanMethodBuilder) {

        // 动态生成 set 方法调用
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) { // 只处理字段
                VariableElement field = (VariableElement) element;

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
    }
}
