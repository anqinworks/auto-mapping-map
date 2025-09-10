package cc.anqin.processor.annotation;

import cc.anqin.processor.enums.MappingEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动键映射注解
 * <p>
 * 该注解用于控制实体类字段与Map之间的映射关系。通过此注解，可以自定义字段在Map中的键名、
 * 控制映射方向（仅转为Map、仅转为Bean或双向），以及是否忽略该字段。
 * </p>
 * 示例：
 *
 *
 *  <pre>{@code
 * @AutoToMap
 * public class User {
 *     // 使用自定义键名
 *     @AutoKeyMapping(target  = "userName")
 *     private String name;
 *     
 *     // 仅在转为Map时使用
 *     @AutoKeyMapping(method  = MappingEnum.TO_MAP)
 *     private String sensitiveData;
 *     
 *     // 忽略此字段
 *     @AutoKeyMapping(ignore  = true)
 *     private String temporaryField;
 *     
 *     // Getters and setters
 * }
 * }</pre>
 *
 *
 * @author Mr.An
 * @since 2024/11/27
 * @see AutoToMap
 * @see IgnoreToMap
 * @see IgnoreToBean
 * @see MappingEnum
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoKeyMapping {

    /**
     * 是否忽略该字段
     * <p>
     * 当设置为true时，该字段将在所有转换过程中被忽略，相当于同时使用了{@link IgnoreToMap}和{@link IgnoreToBean}。
     * </p>
     *
     * @return 如果为true，则忽略该字段；否则根据其他配置进行处理
     */
    boolean ignore() default false;

    /**
     * 映射方向
     * 控制字段参与的转换方向：
     * <ul>
     *   <li>{@link MappingEnum#ALL} - 字段同时参与对象到Map和Map到对象的转换</li>
     *   <li>{@link MappingEnum#TO_MAP} - 字段仅参与对象到Map的转换</li>
     *   <li>{@link MappingEnum#TO_BEAN} - 字段仅参与Map到对象的转换</li>
     * </ul>
     *
     * @return 映射方向枚举值，默认为{@link MappingEnum#ALL}
     */
    MappingEnum method() default MappingEnum.ALL;

    /**
     * 目标键名
     * <p>
     * 指定字段在转换为Map时使用的键名。如果未设置，则使用字段的原始名称作为键名。
     * 此属性仅在对象转换为Map时有效，不影响Map转换为对象的过程。
     * </p>
     *
     * @return 自定义的Map键名，默认为空字符串（使用字段原名）
     */
    String target() default "";
}
