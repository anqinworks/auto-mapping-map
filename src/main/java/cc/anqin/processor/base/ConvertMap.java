package cc.anqin.processor.base;

import cc.anqin.processor.util.ConfigLoader;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 映射转换工厂类
 *
 * <p>提供对象与Map之间的双向转换功能，基于预注册的转换器实现类型转换。该工具类是整个映射框架的核心入口点，
 * 负责管理和调用所有自动生成的转换器实现。</p>
 *
 * 转换器的注册过程：
 * <ol>
 *   <li>使用{@link cc.anqin.processor.annotation.AutoToMap}注解标记需要转换的实体类</li>
 *   <li>编译时，注解处理器{@link cc.anqin.processor.MapConverterProcessor}自动生成转换器实现类</li>
 *   <li>生成的转换器在类加载时自动注册到{@code CONVERT_MAP}中</li>
 * </ol>
 *
 * 使用示例：
 * <pre>{@code
 * // 对象转Map
 * Map<String, Object> map = ConvertMap.toMap(user);
 *
 * // Map转对象
 * User user = ConvertMap.toBean(map, User.class);
 *
 * // 检查转换器是否存在
 * boolean exists = ConvertMap.exists(User.class);
 *
 * // 批量转换
 * List<Map<String, Object>> maps = ConvertMap.toMapList(users);
 * List<User> users = ConvertMap.toBeanList(maps, User.class);
 * }</pre>
 *
 *
 * @author Mr.An
 * @since 2024/11/18
 * @see cc.anqin.processor.annotation.AutoToMap
 * @see cc.anqin.processor.base.MappingConvert
 */
public class ConvertMap {

    /** 日志 */
    private static final Log log = Log.get(ConvertMap.class);

    /**
     * 转换器类名后缀
     * <p>
     * 用于构建转换器类的完整类名。例如，对于实体类{@code User}，
     * 其对应的转换器类名为{@code User_MapConverter}。
     * </p>
     */
    public static final String CLASS_SUFFIX = "_MapConverter";

    /**
     * 转换器映射表
     * <p>
     * 存储类名与对应转换器实例的映射关系。该映射表在类加载时初始化，
     * 通过文件配置或包扫描方式加载所有实现了{@link MappingConvert}接口的类来填充。
     * 映射表是线程安全的，且初始化后不可修改。
     * </p>
     * <p>
     * 加载策略：优先使用文件配置方式（高性能），失败时降级到包扫描方式（兼容性），
     * 最终失败时使用空映射保障系统可用性。
     * </p>
     */
    private static final Map<String, MappingConvert<?>> CONVERT_MAP;

    static {
        long startTime = System.currentTimeMillis();

        CONVERT_MAP = ConfigLoader.scanLoadAllConfigsAsSingleMap();

        log.info("转换器初始化总耗时: {}ms", System.currentTimeMillis() - startTime);
    }



    /**
     * 私有构造函数防止实例化
     */
    private ConvertMap() {
        throw new UnsupportedOperationException("ConvertMap是一个工具类，不能被实例化");
    }


    /**
     * 将对象转换为Map
     *
     * @param source 源对象，不能为null
     * @return 包含对象属性的Map，如果源对象为null则返回null
     * @throws IllegalArgumentException 如果找不到对应的转换器
     */
    public static Map<String, Object> toMap(Object source) {
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
    public static Map<String, Object> toMap(Object source, Class<?> clazz) {
        return toMap(source, clazz, null);
    }

    /**
     * 将对象转换为Map
     *
     * @param source 源对象，不能为null
     * @param clazz 对象类型，为null 的时候执行 defaultClazz
     * @param defaultClazz 默认 clazz
     * @return 包含对象属性的Map
     *
     */
    @SuppressWarnings("all")
    public static Map<String, Object> toMap(Object source, Class<?> clazz, Class<?> defaultClazz) {
        if (source == null) {
            throw new IllegalArgumentException("源对象不能为null");
        }
        if (exists(clazz)) {
            MappingConvert convert = getMappingConvert(clazz);
            return convert.toMap(source);
        }

        if (nonExists(defaultClazz)) {
            throw new IllegalArgumentException("目标 clazz 为空，且 defaultClazz 不存在: " + defaultClazz);
        }
        MappingConvert convert = getMappingConvert(defaultClazz);
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
        return toBean(dataMap, clazz, null);
    }


    /**
     * 将Map转换为指定类型的对象
     *
     * @param dataMap 包含数据的Map，不能为null
     * @param clazz 目标类型，不能为null
     * @param defaultClazz 默认 clazz
     * @return 转换后的对象
     *
     */
    public static <T> T toBean(Map<String, Object> dataMap, Class<T> clazz, Class<?> defaultClazz) {
        if (dataMap == null) {
            throw new IllegalArgumentException("源对象不能为null");
        }
        if (exists(clazz)) {
            return getMappingConvert(clazz).toBean(dataMap);
        }

        if (nonExists(defaultClazz)) {
            throw new IllegalArgumentException("目标 clazz 为空，且 defaultClazz 不存在: " + defaultClazz);
        }
        @SuppressWarnings("unchecked")
        T bean = (T) getMappingConvert(defaultClazz).toBean(dataMap);
        return bean;

    }

    /**
     * 检查指定类型是否存在对应的转换器
     *
     * @param clazz 要检查的类型
     * @return 如果存在对应的转换器返回true，否则返回false
     */
    public static boolean exists(Class<?> clazz) {
        return clazz != null && CONVERT_MAP.containsKey(clazz.getName());
    }

    /**
     * 检查指定类型是否存在对应的转换器
     *
     * @param clazz 要检查的类型
     * @return 如果存在对应的转换器返回true，否则返回false
     */
    public static boolean nonExists(Class<?> clazz) {
        return !exists(clazz);
    }


    /**
     * 批量转换对象列表到Map列表
     * <p>
     * 将给定的对象列表中的每个对象转换为对应的Map，并返回包含这些Map的列表。
     * 转换过程使用{@link #toMap(Object)}方法，保持与单个对象转换的一致性。
     * </p>
     *
     * @param sources 需要转换的源对象列表
     * @param <T> 对象类型
     * @return 转换后的Map列表，如果输入为null则返回null
     * @see #toMap(Object)
     */
    public static <T> List<Map<String, Object>> toMapList(List<T> sources) {
        if (sources == null) {
            throw new IllegalArgumentException("输入参数 sources 不能为 null");
        }

        return sources.stream()
                .map(ConvertMap::toMap)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换Map列表到对象列表
     * <p>
     * 将给定的Map列表中的每个Map转换为指定类型的对象，并返回包含这些对象的列表。
     * 转换过程使用{@link #toBean(Map, Class)}方法，保持与单个Map转换的一致性。
     * </p>
     *
     * @param dataMaps 需要转换的Map列表
     * @param clazz 目标对象类型的Class对象
     * @param <T> 目标对象类型
     * @return 转换后的对象列表，如果输入参数任一为null则返回null
     * @throws IllegalArgumentException 如果找不到对应的转换器
     * @see #toBean(Map, Class)
     */
    public static <T> List<T> toBeanList(List<Map<String, Object>> dataMaps, Class<T> clazz) {
        if (dataMaps == null) {
            throw new IllegalArgumentException("输入参数 dataMaps 不能为 null");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("输入参数 clazz 不能为 null");
        }

        return dataMaps.stream()
                .map(map -> toBean(map, clazz))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定类型的转换器
     *
     * @param <T> 目标类型
     * @param clazz 目标类型的Class对象
     * @return 对应类型的转换器实例
     * @throws IllegalArgumentException 如果clazz为null或找不到对应的转换器
     */
    @SuppressWarnings("unchecked")
    public static <T> MappingConvert<T> getMappingConvert(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz不能为null");
        }

        MappingConvert<?> converter = CONVERT_MAP.get(clazz.getName());

        if (converter == null) {
            throw new IllegalArgumentException("找不到类型 " + clazz.getName() + " 的转换器");
        }

        return (MappingConvert<T>) converter;
    }

    /**
     * 获取所有注册的转换器
     *
     * @return 包含所有已注册转换器名称的不可修改集合
     */
    public static Map<String, MappingConvert<?>> getRegisteredMap() {
        return CONVERT_MAP;
    }


    /**
     * 获取所有已注册的转换器名称
     * <p>
     * 返回当前已注册到系统中的所有转换器名称的不可修改集合。
     * 此方法主要用于调试和诊断目的，可以帮助确认哪些转换器已被成功注册。
     * </p>
     *
     * @return 包含所有已注册转换器名称的不可修改集合
     */
    public static Set<String> getRegisteredConverterNames() {
        return Collections.unmodifiableSet(CONVERT_MAP.keySet());
    }

    /**
     * 根据类获取转换器名称
     * <p>
     * 根据给定的类对象构建对应转换器的名称。转换器名称由类的简单名称加上{@link #CLASS_SUFFIX}组成。
     * 例如，对于类{@code com.example.User}，其转换器名称为{@code User_MapConverter}。
     * </p>
     *
     * @param clazz 需要获取转换器名称的类对象
     * @return 对应的转换器名称
     * @throws NullPointerException 如果clazz为null
     */
    public static String getConvertName(Class<?> clazz) {
        return clazz.getSimpleName() + CLASS_SUFFIX;
    }

    /**
     * 根据类名获取转换器名称
     * <p>
     * 根据给定的类名字符串构建对应转换器的名称。转换器名称由类名加上{@link #CLASS_SUFFIX}组成。
     * 例如，对于类名{@code User}，其转换器名称为{@code User_MapConverter}。
     * </p>
     *
     * @param className 类名字符串
     * @return 对应的转换器名称
     */
    public static String getConvertName(String className) {
        if (StrUtil.isBlank(className)) {
            return null;
        }
        return className + CLASS_SUFFIX;
    }
}