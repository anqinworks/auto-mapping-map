package cc.anqin.processor.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动映射注解
 * <p>
 * 该注解用于标记需要自动生成Map转换器的实体类。被标记的类将在编译时自动生成对应的转换器实现类，
 * 用于实现实体对象与Map之间的双向转换。转换器类将实现{@link cc.anqin.processor.base.MappingConvert}接口。
 * </p>
 * <p>
 * 使用此注解的类必须具有无参构造函数，且需要为所有需要映射的字段提供getter和setter方法。
 * 可以通过{@link AutoKeyMapping}、{@link IgnoreToMap}和{@link IgnoreToBean}注解进一步控制字段的映射行为。
 * </p>
 * 示例：
 *
 * <pre>{@code
 *
 * @AutoToMap
 * public class User {
 *     private String name;
 *     private int age;
 *     
 *     // Getters and setters
 * }
 * }</pre>
 *
 * @author Mr.An
 * @since 2024/11/13
 * @see cc.anqin.processor.base.MappingConvert
 * @see cc.anqin.processor.base.ConvertMap
 * @see AutoKeyMapping
 * @see IgnoreToMap
 * @see IgnoreToBean
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface AutoToMap {
}
