package cc.anqin.processor.base;

import java.util.Map;

/**
 * 映射转换
 *
 * @author Mr.An
 * @date 2024/11/13
 */

public interface MappingConvert<T> {

    /**
     * 转为Map
     *
     * @param entity 实体
     * @return {@link Map }<{@link String }, {@link Object }>
     */
    Map<String, Object> toMap(T entity);
}
