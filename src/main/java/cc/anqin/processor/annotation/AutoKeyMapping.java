package cc.anqin.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标识字段在转换为 Map
 *
 * @author Mr.An
 * @date 2024/11/27
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoKeyMapping {

    /**
     * 是否忽略该字段
     *
     * @return boolean
     */
    boolean ignore() default false;


    /**
     * 转为目标的 key 名称
     *
     * @return {@link String }
     */
    String target() default "";
}
