package cc.anqin.processor.base;

import cn.hutool.core.util.ClassUtil;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 转换工厂
 *
 * @author Mr.An
 * @since 2024/11/18
 */
public class ConvertMap {


    /**
     * 转换地图
     */
    @Getter
    private static final Map<String, MappingConvert<?>> convertMap;

    /**
     * 类后缀
     */
    public static final String CLASS_SUFFIX = "_MapConverter";

    public static final String PACKAGE_NAME = "cc.anqin";


    static {

        // 1.获取 加了指定注解的所有类,扫描所有包
        Set<Class<?>> classes = ClassUtil.scanPackageBySuper(PACKAGE_NAME, MappingConvert.class);
        Map<String, MappingConvert<?>> dataMap = classes.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Class::getSimpleName, v -> {
                    try {
                        return (MappingConvert<?>) v.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, (nk, ok) -> nk));
        convertMap = Collections.unmodifiableMap(dataMap);
    }

    /**
     * 转换
     *
     * @param source 源
     * @param clazz  clazz
     * @return {@link MappingConvert }
     */
    @SuppressWarnings("all")
    public static Map<String, Object> toMap(Object source, Class<?> clazz) {
        if (source == null || clazz == null) return null;
        MappingConvert convert = convertMap.get(clazz.getSimpleName() + CLASS_SUFFIX);
        if (convert == null) return null;
        return convert.toMap(source);
    }
}
