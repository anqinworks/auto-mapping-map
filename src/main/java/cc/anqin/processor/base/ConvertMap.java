package cc.anqin.processor.base;

import cn.hutool.core.util.ClassUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 映射转换工厂类
 *
 * <p>提供对象与Map之间的双向转换功能，基于预注册的转换器实现类型转换。</p>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 在目标类添加 @cc.anqin.processor.annotation.AutoToMap 注解，重新编译后自动注册
 *
 * // 对象转Map
 * Map<String, Object> map = ConvertMap.toMap(user);
 *
 * // Map转对象
 * User user = ConvertMap.toBean(map, User.class);
 *
 * // 检查转换器是否存在
 * boolean exists = ConvertMap.exists(User.class);
 * }
 * </pre>
 * </p>
 *
 * @author Mr.An
 * @since 2024/11/18
 */
public class ConvertMap {

    /**
     * 类名后缀
     */
    public static final String CLASS_SUFFIX = "_MapConverter";

    /**
     * 转换器映射表
     *
     * <p>存储类名与对应转换器的映射关系，线程安全且不可修改</p>
     */
    private static final Map<String, MappingConvert<?>> CONVERT_MAP;

    // 静态初始化块，加载所有转换器
    static {
        // 1. 扫描指定包下所有实现了MappingConvert接口的类
        Set<Class<?>> classes = ClassUtil.scanPackageBySuper("auto.mappings", MappingConvert.class);

        // 2. 创建转换器实例并构建不可修改的映射表
        Map<String, MappingConvert<?>> dataMap = classes.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Class::getSimpleName,
                        ConvertMap::createConverterInstance,
                        (existing, replacement) -> existing // 重复键处理策略：保留现有值
                ));

        CONVERT_MAP = Collections.unmodifiableMap(dataMap);
    }

    /**
     * 私有构造函数防止实例化
     */
    private ConvertMap() {
        throw new UnsupportedOperationException("ConvertMap是一个工具类，不能被实例化");
    }

    /**
     * 创建转换器实例
     *
     * @param clazz 转换器类
     * @return 转换器实例
     * @throws RuntimeException 当实例创建失败时抛出
     */
    private static MappingConvert<?> createConverterInstance(Class<?> clazz) {
        try {
            return (MappingConvert<?>) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建转换器实例失败: " + clazz.getName(), e);
        }
    }

    /**
     * 将对象转换为Map
     *
     * @param source 源对象，不能为null
     * @return 包含对象属性的Map，如果源对象为null则返回null
     * @throws IllegalArgumentException 如果找不到对应的转换器
     */
    public static Map<String, Object> toMap(Object source) {
        if (source == null) {
            return null;
        }
        return toMap(source, source.getClass());
    }

    /**
     * 将对象转换为Map
     *
     * @param source 源对象，不能为null
     * @param clazz 对象类型，不能为null
     * @return 包含对象属性的Map
     * @throws IllegalArgumentException 如果参数为null或找不到对应的转换器
     */
    @SuppressWarnings("all")
    public static Map<String, Object> toMap(Object source, Class<?> clazz) {
        validateParameters(source, clazz);

        MappingConvert convert = getMappingConvert(clazz);
        if (convert == null) {
            throw new IllegalArgumentException("找不到类型 " + clazz.getName() + " 的转换器");
        }

        return convert.toMap(source);
    }

    /**
     * 将Map转换为指定类型的对象
     *
     * @param <T> 目标类型
     * @param dataMap 包含数据的Map，不能为null
     * @param clazz 目标类型，不能为null
     * @return 转换后的对象
     * @throws IllegalArgumentException 如果参数为null或找不到对应的转换器
     */
    public static <T> T toBean(Map<String, Object> dataMap, Class<T> clazz) {
        validateParameters(dataMap, clazz);

        MappingConvert<T> convert = getMappingConvert(clazz);
        if (convert == null) {
            throw new IllegalArgumentException("找不到类型 " + clazz.getName() + " 的转换器");
        }

        return convert.toBean(dataMap);
    }

    /**
     * 检查指定类型是否存在对应的转换器
     *
     * @param clazz 要检查的类型
     * @return 如果存在对应的转换器返回true，否则返回false
     */
    public static boolean exists(Class<?> clazz) {
        return clazz != null && CONVERT_MAP.containsKey(getConvertName(clazz));
    }

    /**
     * 批量转换对象列表到Map列表
     *
     * @param sources 源对象列表
     * @param <T> 对象类型
     * @return Map列表
     */
    public static <T> List<Map<String, Object>> toMapList(List<T> sources) {
        if (sources == null) {
            return null;
        }

        return sources.stream()
                .map(ConvertMap::toMap)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换Map列表到对象列表
     *
     * @param dataMaps Map列表
     * @param clazz 目标类型
     * @param <T> 目标类型
     * @return 对象列表
     */
    public static <T> List<T> toBeanList(List<Map<String, Object>> dataMaps, Class<T> clazz) {
        if (dataMaps == null || clazz == null) {
            return null;
        }

        return dataMaps.stream()
                .map(map -> toBean(map, clazz))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定类型的转换器
     *
     * @param <T> 目标类型
     * @param clazz 目标类型
     * @return 对应的转换器，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> MappingConvert<T> getMappingConvert(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }

        String convertName = getConvertName(clazz);
        MappingConvert<?> converter = CONVERT_MAP.get(convertName);

        if (converter == null) {
            throw new IllegalArgumentException("找不到类型 " + clazz.getName() + " 的转换器");
        }

        return (MappingConvert<T>) converter;
    }

    /**
     * 获取所有已注册的转换器名称
     *
     * @return 转换器名称集合
     */
    public static Set<String> getRegisteredConverterNames() {
        return Collections.unmodifiableSet(CONVERT_MAP.keySet());
    }

    /**
     * 根据类获取转换器名称
     *
     * @param clazz 类
     * @return 转换器名称
     */
    public static String getConvertName(Class<?> clazz) {
        return clazz.getSimpleName() + CLASS_SUFFIX;
    }

    /**
     * 根据类名获取转换器名称
     *
     * @param className 类名
     * @return 转换器名称
     */
    public static String getConvertName(String className) {
        return className + CLASS_SUFFIX;
    }

    /**
     * 验证参数是否有效
     *
     * @param obj 对象参数
     * @param clazz 类参数
     * @throws IllegalArgumentException 如果任一参数为null
     */
    private static void validateParameters(Object obj, Class<?> clazz) {
        if (obj == null) {
            throw new IllegalArgumentException("源对象不能为null");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("目标类型不能为null");
        }
    }
}