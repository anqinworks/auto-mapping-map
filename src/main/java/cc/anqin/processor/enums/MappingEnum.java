package cc.anqin.processor.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 映射方向枚举
 * <p>
 * 该枚举定义了字段映射的方向选项，用于控制实体类字段在转换过程中的行为。
 * 主要与{@link cc.anqin.processor.annotation.AutoKeyMapping}注解配合使用，
 * 指定字段是参与所有转换、仅参与对象到Map转换，还是仅参与Map到对象转换。
 * </p>
 *
 * @author Mr.An
 * @since 2025/03/06
 * @see cc.anqin.processor.annotation.AutoKeyMapping
 */
@Getter
@AllArgsConstructor
public enum MappingEnum {

    /**
     * 双向映射
     * <p>
     * 字段同时参与对象到Map和Map到对象的转换过程。
     * 这是默认的映射方向。
     * </p>
     */
    ALL,

    /**
     * 仅映射到Map
     * <p>
     * 字段仅参与对象到Map的转换过程，不参与Map到对象的转换。
     * 适用于只需要序列化但不需要反序列化的字段。
     * </p>
     */
    TO_MAP,

    /**
     * 仅映射到Bean
     * <p>
     * 字段仅参与Map到对象的转换过程，不参与对象到Map的转换。
     * 适用于只需要反序列化但不需要序列化的字段。
     * </p>
     */
    TO_BEAN,
}
