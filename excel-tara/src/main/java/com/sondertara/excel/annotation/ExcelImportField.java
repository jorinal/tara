
package com.sondertara.excel.annotation;

import com.sondertara.excel.enums.FieldRangeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;

/**
 * @author huangxiaohu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelImportField {

    /**
     * 对应excel中的列
     */
    int index() default -1;

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 日期格式 默认 yyyy-MM-dd HH:mm:ss
     */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 正则表达式校验
     */
    String regex() default "";

    /**
     * 正则表达式校验失败返回的错误信息,regex配置后生效
     */
    String regexMessage() default "正则表达式验证失败";

    /**
     * BigDecimal精度 默认:2
     */
    int scale() default 2;

    /**
     * BigDecimal 舍入规则 默认:BigDecimal.ROUND_HALF_EVEN
     */
    RoundingMode roundingMode() default RoundingMode.HALF_EVEN;

    /**
     * range
     *
     * @return the field value rang
     */
    String[] range() default {};

    /**
     * the range type
     *
     * @return range type enum
     */
    FieldRangeType rangeType() default FieldRangeType.RANGE_CLOSE;
}
