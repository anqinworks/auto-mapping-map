package cc.anqin.processor.base;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
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
                .collect(Collectors.toMap(Class::getSimpleName,
                        v -> {
                            try {
                                return (MappingConvert<?>) v.getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }, (nk, ok) -> nk));
        convertMap = Collections.unmodifiableMap(dataMap);
    }


    /**
     * 转换 Map
     *
     * @param source 源
     * @param clazz  clazz
     * @return {@link MappingConvert }
     */
    @SuppressWarnings("all")
    public static  Map<String, Object> toMap(Object source, Class<?> clazz) {
        if (source == null || clazz == null) return null;
        MappingConvert convert = getMappingConvert(clazz);
        if (convert == null) return null;
        return convert.toMap(source);
    }

    /**
     * 转换 为 Bean
     *
     * @param dataMap dataMap
     * @param clazz   clazz
     * @param <T>     entity
     * @return {@link MappingConvert }
     */
    public static <T> T toBean(Map<String, Object> dataMap, Class<T> clazz) {
        if (dataMap == null || clazz == null) return null;
        MappingConvert<T> convert = getMappingConvert(clazz);
        if (convert == null) return null;
        return convert.toBean(dataMap);
    }


    /**
     * 获取映射转换
     *
     * @param clazz clazz
     * @param <T>   entity
     * @return {@link MappingConvert }<{@link ? }>
     */
    @SuppressWarnings("unchecked")
    private static <T> MappingConvert<T> getMappingConvert(Class<T> clazz) {
        return (MappingConvert<T>) convertMap.get(getConvertName(clazz));
    }

    /**
     * 获取转换名称
     *
     * @param clazz clazz
     * @return {@link String }
     */
    public static String getConvertName(Class<?> clazz) {
        return getConvertName(clazz.getCanonicalName());
    }

    /**
     * 获取转换名称
     *
     * @param packageName 包名称
     * @return {@link String }
     */
    public static String getConvertName(String packageName) {
        return StrUtil.replace(packageName, ".", "_") + CLASS_SUFFIX;
    }
}
