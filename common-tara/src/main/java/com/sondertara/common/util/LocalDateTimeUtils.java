package com.sondertara.common.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * date util
 *
 * @author huangxiaohu
 * @since 2019-03-19 16:25
 */
public class LocalDateTimeUtils {

    private static final Logger log = LoggerFactory.getLogger(LocalDateTimeUtils.class);


    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";


    private static final LoadingCache<String, DateTimeFormatter> LOAD_CACHE = CacheBuilder.newBuilder().maximumSize(16).build(new CacheLoader<String, DateTimeFormatter>() {
        @Override
        public DateTimeFormatter load(@Nonnull String pattern) {
            return DateTimeFormatter.ofPattern(pattern);
        }
    });
    private static final String DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMATTER = "yyyy-MM-dd";

    public static String getNow() {
        return now(DATE_TIME_FORMATTER);
    }

    public static String now() {
        return getNow();
    }

    public static String now(String formatPattern) {

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        return dateTime.format(formatter);
    }

    public static LocalDateTime convertToLocalDateTime(String dateTimeStr, String formatPattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            log.error("parser date error,params=[{}],pattern=[{}]", dateTimeStr, formatPattern, e);
            return null;
        }
    }

    public static LocalDateTime convertToLocalDateTime(String dateTimeStr) {
        return convertToLocalDateTime(dateTimeStr, DATE_TIME_FORMATTER);
    }


    private static LocalDate convertToLocalDate(String date) {
        DateTimeFormatter formatter;
        LocalDate parse = null;
        try {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            parse = LocalDate.parse(date, formatter);
        } catch (Exception e) {
        }
        if (parse == null) {
            try {
                formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                parse = LocalDate.parse(date, formatter);
            } catch (Exception e) {
            }

        }
        if (parse == null) {
            try {
                formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                parse = LocalDate.parse(date, formatter);
            } catch (Exception e) {
            }
        }

        return parse;
    }

    /**
     * @param date jdk8之前的date
     */
    public static String format(Date date, String pattern) {
        if (null == date) {
            return null;
        }
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }

    public static String format(Date date) {
        if (null == date) {
            return null;
        }
        return format(date, DATE_TIME_FORMATTER);
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime, String pattern) {
        if (null == localDateTime) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return localDateTime.format(formatter);
        } catch (Exception e) {
            log.error("parser date str error,pattern=[{}]", pattern, e);
            return null;
        }
    }

    public static String formatLocalDate(LocalDate localDate, String pattern) {
        if (null == localDate) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDate.format(formatter);
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return formatLocalDateTime(localDateTime, DATE_TIME_FORMATTER);
    }

    public static Date parseToDate(String dateTime) {
        return parseToDate(dateTime, DATE_TIME_FORMATTER);
    }

    public static Date parseToDate(String dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        Date date = null;
        try {
            LocalDateTime localDateTime = convertToLocalDateTime(dateTime, pattern);
            assert localDateTime != null;
            date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
        }
        if (date == null) {
            try {
                final LocalDate parse = convertToLocalDate(dateTime);
                date = Date.from(parse.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            } catch (Exception e) {
            }

        }
        return date;
    }

    /**
     * 获取两个日期间隔天数，只要不是同一天，间隔就为1
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return the two date delay
     */
    public static Integer getDelay(String startDate, String endDate) {

        final LocalDate start = convertToLocalDate(startDate);
        final LocalDate end = convertToLocalDate(endDate);
        if (start != null && end != null) {
            final long l = end.toEpochDay() - start.toEpochDay();
            return (int) l;
        }
        return null;
    }

    /**
     * 获取某一天零点
     *
     * @param dateStr 20120920
     * @return 20120920
     */
    public static String getDayStart(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMATTER));

        LocalDateTime dateTime = LocalDateTime.of(localDate, LocalTime.MIN);
        return formatLocalDateTime(dateTime);
    }

    /**
     * 获取某一天零点
     *
     * @param localDate the local date
     * @return the
     */
    public static String getDayStart(LocalDate localDate) {

        LocalDateTime dateTime = LocalDateTime.of(localDate, LocalTime.MIN);
        return formatLocalDateTime(dateTime);
    }

    /**
     * 获取某一天结束
     *
     * @param dateStr 20120920
     * @return the end str 2012-09-20 23:59:59
     */
    public static String getDayEnd(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMATTER));

        LocalDateTime dateTime = LocalDateTime.of(localDate, LocalTime.MAX);
        return formatLocalDateTime(dateTime);
    }

    /**
     * get end of a day
     *
     * @return the end str 2012-09-20 23:59:59
     */
    public static String getDayEnd(LocalDate localDate) {

        LocalDateTime dateTime = LocalDateTime.of(localDate, LocalTime.MAX);
        return formatLocalDateTime(dateTime);
    }

    public static void main(String[] args) {
        String end = getDayEnd("2012-09-20");

        System.out.println(end);
    }

}