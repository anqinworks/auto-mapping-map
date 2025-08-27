package cc.anqin.processor.base;

import java.util.Map;

/**
 * 映射转换
 *
 * @param <T> entity
 * @author Mr.An
 * @since 2024/11/13
 */
public interface MappingConvert<T> {

    /**
     * 转为Map
     *
     * @param entity 实体
     * @return {@code Map<String, Object>} where the key is a {@link String} and the value is an {@link Object}.
     */
    Map<String, Object> toMap(T entity);

    /**
     * 转为 实体
     *
     * @param dataMap 资料图
     * @return {@link T }
     */
    T toBean(Map<String, Object> dataMap);
}
