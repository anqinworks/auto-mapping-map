package cc.anqin.processor.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略字段转换为Bean注解
 * <p>
 * 该注解用于标记在Map转换为实体类过程中需要忽略的字段。被此注解标记的字段将不会从Map中获取值并设置到实体对象中。
 * 通常用于排除不需要反序列化的字段、计算属性或由其他逻辑控制的字段。
 * </p>
 * 示例：
 *
 * <pre>{@code
 * @AutoToMap
 * public class User {
 *     private String name;
 *
 *     @IgnoreToBean
 *     private String calculatedField; // 此字段在Map转换为实体时将被忽略
 *
 *     // Getters and setters
 * }
 *
 * }</pre>
 *
 *
 * @author Mr.An
 * @since 2025/03/06
 * @see AutoToMap
 * @see IgnoreToMap
 * @see AutoKeyMapping
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreToBean {
}
