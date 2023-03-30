package com.eaglesoup.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SizeUtil {
    private static final Logger logger = LoggerFactory.getLogger(SizeUtil.class);

    private SizeUtil() {
    }

    public static String toBinary(Byte obj) {
        StringBuilder stringBuilder = new StringBuilder(Integer.toBinaryString(obj));
        //左侧补齐0
        int binaryStrLen = stringBuilder.length();
        while (binaryStrLen < Byte.SIZE) {
            stringBuilder.insert(0, "0");
            binaryStrLen++;
        }
        //左侧删除多余的位数
        if (stringBuilder.length() > Byte.SIZE) {
            return stringBuilder.substring(stringBuilder.length() - Byte.SIZE);
        }
        return stringBuilder.toString();
    }

    /**
     * byte[] 转二进制
     * case1:
     * >> byte[] i = new byte[3];
     * >> i = "a".getBytes();
     *
     * @param obj:  数据
     * @param size: 多少个字节
     */
    public static String toBinary(byte[] obj, int size) {
        if (obj.length > size) {
            logger.error("{} 超过了指定的字节数:{}", obj, size);
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (; i < obj.length; i++) {
            result.append(toBinary(obj[i]));
        }
        for (; i < size; i++) {
            result.append(toBinary((byte) 0));
        }
        return result.toString();
    }

    public static byte[] longToByteArray(long obj, int byteSize) {
        byte[] result = new byte[byteSize];
        // 由高位到低位
        for (int i = 0; i < byteSize; i++) {
            int bitIndex = (byteSize - i - 1) * 8;
            result[i] = (byte) ((obj >> bitIndex) & 0xFF);
        }
        return result;
    }

    /**
     * byte[] 和 int类型转换
     *
     * @param input
     * @param byteSize: 字节数
     * @return
     */
    public static long byteArrayToLong(byte[] input, int byteSize) {
        return byteArrayToLong(input, byteSize, false);
    }

    private static long byteArrayToLong(byte[] input, int byteSize, boolean littleEndian) {
        long value = 0;
        // 循环读取每个字节通过移位运算完成long的8个字节拼装
        for (int count = 0; count < byteSize; ++count) {
            int shift = (littleEndian ? count : (byteSize - 1 - count)) << 3;
            value |= ((long) 0xff << shift) & ((long) input[count] << shift);
        }
        return value;
    }
}