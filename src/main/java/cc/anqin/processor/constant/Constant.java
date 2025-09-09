package cc.anqin.processor.constant;

import cn.hutool.core.util.StrUtil;

import java.util.function.Function;

/**
 *
 * @author Mr.An
 * @since 2025/9/9
 */
public class Constant {


    public static final String REGISTRY_PATH = "META-INF/map-converter-registry.json";

    public static final Function<String, String> GENERATE_NAME_SUFFIX = t -> StrUtil.format("{}_MapConverter", t);


    public static final String PACKAGE_PREFIX = "auto.mappings.";


}
