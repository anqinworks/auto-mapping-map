package cc.anqin.processor.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略字段转换为Map注解
 * <p>
 * 该注解用于标记在实体类转换为Map过程中需要忽略的字段。被此注解标记的字段将不会被包含在转换结果的Map中。
 * 通常用于排除敏感信息、临时字段或不需要序列化的复杂对象。
 * </p>
 * 示例：
 * <blockquote>
 * <pre>
 *
 * &#064;AutoToMap
 * public class User {
 *     private String name;
 *     
 *     &#064;IgnoreToMap
 *     private String password; // 此字段在转换为Map时将被忽略
 *     
 *     // Getters and setters
 * }
 *
 * </pre>
 * </blockquote>
 *
 * @author Mr.An
 * @since 2025/03/06
 * @see AutoToMap
 * @see IgnoreToBean
 * @see AutoKeyMapping
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreToMap {
}
