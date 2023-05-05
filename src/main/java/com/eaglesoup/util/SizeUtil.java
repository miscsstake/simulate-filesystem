package com.eaglesoup.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SizeUtil {
    private static final Logger logger = LoggerFactory.getLogger(SizeUtil.class);

    private SizeUtil() {
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