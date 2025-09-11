package cc.anqin.processor.util;

import cc.anqin.processor.base.MappingConvert;
import cn.hutool.core.util.ClassUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 配置加载器
 *
 * @author <a href="https://blog.anqin.cc/">Mr.An</a>
 * @since 2025/09/10
 */
public class ConfigLoader {

    /** 配置文件 */
    public static final String CONFIG_FILE_PATH = "META-INF/map-converter-registry.json";

    /**
     * 生成的转换器类的包前缀
     * <p>
     * 所有自动生成的转换器类都将被放置在此前缀指定的包下，以避免与用户代码冲突。
     * 例如，对于类{@code com.example.User}，其转换器类的全限定名为
     * {@code auto.mappings.com.example.User_MapConverter}。
     * </p>
     */
    public static final String PACKAGE_PREFIX = "auto.mappings.";


    /**
     * 扫描将所有配置加载为单个映射
     *
     * @return {@link Map }<{@link String }, {@link MappingConvert }<{@link ? }>>
     */
    public static Map<String, MappingConvert<?>> scanLoadAllConfigsAsSingleMap() {
        // 1. 扫描指定包下所有实现了MappingConvert接口的类
        Set<Class<?>> classes = ClassUtil.scanPackageBySuper("auto.mappings", MappingConvert.class);

        // 2. 创建转换器实例并构建不可修改的映射表
        Map<String, MappingConvert<?>> dataMap = classes.stream()
                .filter(Objects::nonNull)
                .map(ConfigLoader::createConverterInstance)
                .collect(Collectors.toMap(
                        converter -> getGenericType(converter.getClass()).getName(),
                        converter -> converter,
                        (existing, replacement) -> existing // 重复键处理策略：保留现有值
                ));

        return Collections.unmodifiableMap(dataMap);
    }


    /**
     * 辅助方法：获取MappingConvert的泛型类型
     *
     * @param converterClass 转换器类
     * @return {@link Class }<{@link ? }>
     */
    private static Class<?> getGenericType(Class<?> converterClass) {
        Type[] genericInterfaces = converterClass.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericInterface;
                if (paramType.getRawType().equals(MappingConvert.class)) {
                    Type actualType = paramType.getActualTypeArguments()[0];
                    if (actualType instanceof Class) {
                        return (Class<?>) actualType;
                    } else if (actualType instanceof ParameterizedType) {
                        return (Class<?>) ((ParameterizedType) actualType).getRawType();
                    }
                }
            }
        }
        throw new IllegalStateException("无法获取MappingConvert的泛型类型: " + converterClass);
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
}