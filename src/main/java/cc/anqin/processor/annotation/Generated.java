package cc.anqin.processor.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * 生成标记注解
 * <p>
 * 该注解用于标记由代码生成器自动生成的代码元素。它提供了关于代码生成的元数据信息，
 * 包括生成器名称、生成日期、注释、源代码信息和代码库信息等。
 * </p>
 * <p>
 * 此注解主要用于自动生成的转换器类，以便于区分手写代码和自动生成的代码，
 * 并提供生成代码的溯源信息。注解保留在源代码级别，不会被编译到字节码中。
 * </p>
 *
 * @author Mr.An
 * @since 2024/11/24
 * @see cc.anqin.processor.MapConverterProcessor
 */
@Documented
@Retention(SOURCE)
@Target({PACKAGE, TYPE, ANNOTATION_TYPE, METHOD, CONSTRUCTOR, FIELD,
        LOCAL_VARIABLE, PARAMETER})
public @interface Generated {
    /**
     * 代码生成器名称
     * <p>
     * 值元素必须包含代码生成器的名称。建议使用代码生成器的完全限定名称，
     * 以便明确标识生成代码的工具。
     * </p>
     *
     * @return 代码生成器的名称数组，例如："cc.anqin.processor.MapConverterProcessor"
     */
    String[] value();

    /**
     * 代码生成日期
     * <p>
     * 记录代码生成的时间戳，通常使用ISO-8601格式（如："2025-03-06T17:42:12.411"）。
     * 此信息有助于追踪代码的生成时间，便于版本管理和问题排查。
     * </p>
     *
     * @return 代码生成的日期时间字符串
     */
    String date() default "";

    /**
     * 生成代码的注释
     * <p>
     * 代码生成器可能希望在生成的代码中包含的任何注释或说明信息。
     * 可用于提供关于生成代码的额外上下文、使用说明或其他重要信息。
     * </p>
     *
     * @return 关于生成代码的注释信息
     */
    String comments() default "";

    /**
     * 源代码信息
     * <p>
     * 指定生成代码所基于的源代码类或资源的完全限定名称。
     * 此信息有助于追踪自动生成代码与原始源代码之间的关系。
     * </p>
     *
     * @return 源代码的完全限定名称
     */
    String source() default "";

    /**
     * 代码库信息
     * <p>
     * 指定代码生成器或生成代码所属项目的代码库URL。
     * 此信息有助于查找代码生成器的源代码、文档或相关资源。
     * </p>
     *
     * @return 代码库的URL或标识符
     */
    String repository() default "";
}
