package cc.anqin.processor.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * 生成
 *
 * @author Mr.An
 * @since 2024/11/24
 */
@Documented
@Retention(SOURCE)
@Target({PACKAGE, TYPE, ANNOTATION_TYPE, METHOD, CONSTRUCTOR, FIELD,
        LOCAL_VARIABLE, PARAMETER})
public @interface Generated {
    /**
     * 值元素必须包含代码生成器的名称。建议使用代码生成器的完全限定名称。
     *
     * @return 例如：cc.anqin.processor.MapConverterProcessor
     */
    String[] value();

    /**
     * 源数据生成日期
     *
     * @return 日期
     */
    String date() default "";

    /**
     * 代码生成器可能希望在生成的代码中包含的任何注释的占位符
     *
     * @return 任何注释的占位符
     */
    String comments() default "";

    /**
     * 源
     *
     * @return {@link String }
     */
    String source() default "";

    /**
     * 存储 库
     *
     * @return {@link String }
     */
    String repository() default "";
}
