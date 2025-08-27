package cc.anqin.processor.base;

import java.util.Map;

/**
 * 映射转换接口
 * <p>
 * 该接口定义了实体对象与Map之间的双向转换方法，用于实现对象序列化和反序列化功能。
 * 实现类需要提供具体的转换逻辑，通常由注解处理器自动生成。
 * </p>
 *
 * @param <T> 需要转换的实体类型
 * @author Mr.An
 * @since 2024/11/13
 */
public interface MappingConvert<T> {

    /**
     * 将实体对象转换为Map
     * <p>
     * 此方法将给定的实体对象的属性转换为键值对形式的Map。
     * 键通常对应于实体类的属性名，值对应于属性值。
     * 转换过程中会考虑{@link cc.anqin.processor.annotation.AutoKeyMapping}和
     * {@link cc.anqin.processor.annotation.IgnoreToMap}注解的配置。
     * </p>
     *
     * @param entity 需要转换的实体对象，不能为null
     * @return 包含实体对象属性的Map，键为属性名(String)，值为属性值(Object)
     */
    Map<String, Object> toMap(T entity);

    /**
     * 将Map转换为实体对象
     * <p>
     * 此方法将给定的Map中的键值对转换回实体对象。
     * Map中的键应对应于实体类的属性名，值应对应于属性值。
     * 转换过程中会考虑{@link cc.anqin.processor.annotation.AutoKeyMapping}和
     * {@link cc.anqin.processor.annotation.IgnoreToBean}注解的配置。
     * </p>
     *
     * @param dataMap 包含实体属性的Map，键为属性名(String)，值为属性值(Object)
     * @return 转换后的实体对象实例
     * @throws ClassCastException 如果Map中的值类型与实体属性类型不匹配
     */
    T toBean(Map<String, Object> dataMap);
}
