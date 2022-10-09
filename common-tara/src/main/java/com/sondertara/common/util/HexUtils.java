package com.sondertara.common.util;

import com.sondertara.common.exception.TaraException;

import java.awt.Color;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 十六进制（简写为hex或下标16）在数学中是一种逢16进1的进位制，一般用数字0到9和字母A到F表示（其中:A~F即10~15）。<br>
 * 例如十进制数57，在二进制写作111001，在16进制写作39。<br>
 * 像java,c这样的语言为了区分十六进制和十进制数值,会在十六进制数的前面加上 0x,比如0x20是十进制的32,而不是十进制的20<br>
 * <p>
 * 参考：https://my.oschina.net/xinxingegeya/blog/287476
 *
 * @author huangxiaohu
 */
public class HexUtils {

    public static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F' };

    private final static char[] LOWER_CHARS = "0123456789abcdef".toCharArray();
    private final static char[] UPPER_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * 判断给定字符串是否为16进制数<br>
     * 如果是，需要使用对应数字类型对象的{@code decode}方法解码<br>
     * 例如：{@code Integer.decode}方法解码int类型的16进制数字
     *
     * @param value 值
     * @return 是否为16进制
     */
    public static boolean isHexNumber(String value) {
        final int index = (value.startsWith("-") ? 1 : 0);
        if (value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index)) {
            try {
                // noinspection ResultOfMethodCallIgnored
                Long.decode(value);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        return false;
    }

    // ----------------------------------------------------------------------------------------------------
    // encode

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data byte[]
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param str     字符串
     * @param charset 编码
     * @return 十六进制char[]
     */
    public static char[] encodeHex(String str, Charset charset) {
        return encodeHex(StringUtils.bytes(str, charset), true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data        byte[]
     * @param toLowerCase {@code true} 传换成小写格式 ， {@code false} 传换成大写格式
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        char[] alphabets = toLowerCase ? LOWER_CHARS : UPPER_CHARS;

        final int len = data.length;
        // len*2
        final char[] out = new char[len << 1];
        // two characters from the hex value.
        for (int i = 0, j = 0; i < len; i++) {
            // 高位
            out[j++] = alphabets[(0xF0 & data[i]) >>> 4];
            // 低位
            out[j++] = alphabets[0x0F & data[i]];
        }
        return out;
    }

    private static char[] encode(byte[] data, boolean toLowerCase) {
        char[] alphabets = toLowerCase ? LOWER_CHARS : UPPER_CHARS;

        final int len = data.length;
        // len*2
        final char[] out = new char[len << 1];
        // two characters from the hex value.
        for (int i = 0, j = 0; i < len; i++) {
            // 高位
            out[j++] = alphabets[(0xF0 & data[i]) >>> 4];
            // 低位
            out[j++] = alphabets[0x0F & data[i]];
        }
        return out;

    }

    public static byte[] decode(CharSequence encoded) {
        if (StringUtils.isEmpty(encoded)) {
            return null;
        }

        encoded = StringUtils.cleanBlank(encoded);
        int len = encoded.length();

        if ((len & 0x01) != 0) {
            // 如果提供的数据是奇数长度，则前面补0凑偶数
            encoded = "0" + encoded;
            len = encoded.length();
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(encoded.charAt(j), j) << 4;
            j++;
            f = f | toDigit(encoded.charAt(j), j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将十六进制字符转换成一个整数
     *
     * @param ch    十六进制char
     * @param index 十六进制字符在字符数组中的位置
     * @return 一个整数
     */
    public static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit < 0) {
            throw new TaraException("Illegal hexadecimal character {} at index {}", ch, index);
        }
        return digit;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data byte[]
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    /**
     * 将字符串转换为十六进制字符串，结果为小写
     *
     * @param data    需要被编码的字符串
     * @param charset 编码
     * @return 十六进制String
     */
    public static String encodeHexStr(String data, Charset charset) {
        return encodeHexStr(StringUtils.bytes(data, charset), true);
    }

    /**
     * 将字符串转换为十六进制字符串，结果为小写，默认编码是UTF-8
     *
     * @param data 被编码的字符串
     * @return 十六进制String
     */
    public static String encodeHexStr(String data) {
        return encodeHexStr(data, StandardCharsets.UTF_8);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data        byte[]
     * @param toLowerCase {@code true} 传换成小写格式 ， {@code false} 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return new String(encodeHex(data, toLowerCase));
    }

    // ----------------------------------------------------------------------------------------------------
    // decode

    /**
     * 将十六进制字符数组转换为字符串，默认编码UTF-8
     *
     * @param hexStr 十六进制String
     * @return 字符串
     */
    public static String decodeHexStr(String hexStr) {
        return decodeHexStr(hexStr, StandardCharsets.UTF_8);
    }

    /**
     * 将十六进制字符数组转换为字符串
     *
     * @param hexStr  十六进制String
     * @param charset 编码
     * @return 字符串
     */
    public static String decodeHexStr(String hexStr, Charset charset) {
        if (StringUtils.isEmpty(hexStr)) {
            return hexStr;
        }
        return StringUtils.str(decodeHex(hexStr), charset);
    }

    /**
     * 将十六进制字符数组转换为字符串
     *
     * @param hexData 十六进制char[]
     * @param charset 编码
     * @return 字符串
     */
    public static String decodeHexStr(char[] hexData, Charset charset) {
        return StringUtils.str(decodeHex(hexData), charset);
    }

    /**
     * 将十六进制字符串解码为byte[]
     *
     * @param hexStr 十六进制String
     * @return byte[]
     */
    public static byte[] decodeHex(String hexStr) {
        return decodeHex((CharSequence) hexStr);
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param hexData 十六进制char[]
     * @return byte[]
     * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] decodeHex(char[] hexData) {
        return decodeHex(String.valueOf(hexData));
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param hexData 十六进制字符串
     * @return byte[]
     * @since 5.6.6
     */
    public static byte[] decodeHex(CharSequence hexData) {
        return decode(hexData);
    }

    // ----------------------------------------------------------------------------------------
    // Color

    /**
     * 将{@link Color}编码为Hex形式
     *
     * @param color {@link Color}
     * @return Hex字符串
     * @since 3.0.8
     */
    public static String encodeColor(Color color) {
        return encodeColor(color, "#");
    }

    /**
     * 将{@link Color}编码为Hex形式
     *
     * @param color  {@link Color}
     * @param prefix 前缀字符串，可以是#、0x等
     * @return Hex字符串
     * @since 3.0.8
     */
    public static String encodeColor(Color color, String prefix) {
        final StringBuilder builder = new StringBuilder(prefix);
        String colorHex;
        colorHex = Integer.toHexString(color.getRed());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getGreen());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getBlue());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        return builder.toString();
    }

    /**
     * 将Hex颜色值转为
     *
     * @param hexColor 16进制颜色值，可以以#开头，也可以用0x开头
     * @return {@link Color}
     * @since 3.0.8
     */
    public static Color decodeColor(String hexColor) {
        return Color.decode(hexColor);
    }

    /**
     * 将指定int值转换为Unicode字符串形式，常用于特殊字符（例如汉字）转Unicode形式<br>
     * 转换的字符串如果u后不足4位，则前面用0填充，例如：
     *
     * <pre>
     * '你' =》\u4f60
     * </pre>
     *
     * @param value int值，也可以是char
     * @return Unicode表现形式
     */
    public static String toUnicodeHex(int value) {
        final StringBuilder builder = new StringBuilder(6);

        builder.append("\\u");
        String hex = toHex(value);
        int len = hex.length();
        if (len < 4) {
            // 不足4位补0
            builder.append("0000", 0, 4 - len);
        }
        builder.append(hex);

        return builder.toString();
    }

    /**
     * 转为16进制字符串
     *
     * @param value int值
     * @return 16进制字符串
     * @since 4.4.1
     */
    public static String toHex(int value) {
        return Integer.toHexString(value);
    }

    /**
     * 16进制字符串转为int
     *
     * @param value 16进制字符串
     * @return 16进制字符串int值
     * @since 5.7.4
     */
    public static int hexToInt(String value) {
        return Integer.parseInt(value, 16);
    }

    /**
     * 转为16进制字符串
     *
     * @param value int值
     * @return 16进制字符串
     * @since 4.4.1
     */
    public static String toHex(long value) {
        return Long.toHexString(value);
    }

    /**
     * 16进制字符串转为long
     *
     * @param value 16进制字符串
     * @return long值
     * @since 5.7.4
     */
    public static long hexToLong(String value) {
        return Long.parseLong(value, 16);
    }

    /**
     * Hex（16进制）字符串转为BigInteger
     *
     * @param hexStr Hex(16进制字符串)
     * @return {@link BigInteger}
     * @since 5.2.0
     */
    public static BigInteger toBigInteger(String hexStr) {
        if (null == hexStr) {
            return null;
        }
        return new BigInteger(hexStr, 16);
    }

    /**
     * 格式化Hex字符串，结果为每2位加一个空格，类似于：
     * 
     * <pre>
     *     e8 8c 67 03 80 cb 22 00 95 26 8f
     * </pre>
     *
     * @param hexStr Hex字符串
     * @return 格式化后的字符串
     */
    public static String format(String hexStr) {
        final int length = hexStr.length();
        final StringBuilder builder = StringUtils.builder(length + length / 2);
        builder.append(hexStr.charAt(0)).append(hexStr.charAt(1));
        for (int i = 2; i < length - 1; i += 2) {
            builder.append(CharUtils.SPACE).append(hexStr.charAt(i)).append(hexStr.charAt(i + 1));
        }
        return builder.toString();
    }

    /**
     * 字符串转换unicode
     */
    public static String string2Unicode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u").append(Integer.toHexString(c));
        }
        return unicode.toString();
    }

    /**
     * unicode字符串转16进制字符串
     */
    public static String unicodeStrTo16(String s) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            str.append(Integer.toHexString(ch));
        }
        return str.toString();
    }

    public static byte[] toByteArray(int i) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (i >>> 24);
        byteArray[1] = (byte) ((i & 0xFFFFFF) >>> 16);
        byteArray[2] = (byte) ((i & 0xFFFF) >>> 8);
        byteArray[3] = (byte) (i & 0xFF);
        return byteArray;
    }

    public static byte[] long2bytes(long l) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (l >>> ((7 - i) * 8));
        }
        return bytes;
    }

}