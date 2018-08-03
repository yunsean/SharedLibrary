package com.dylan.common.utils;

import java.io.UnsupportedEncodingException;

public class Charset {
    public static final String US_ASCII = "US-ASCII";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String UTF_8 = "UTF-8";
    public static final String UTF_16BE = "UTF-16BE";
    public static final String UTF_16LE = "UTF-16LE";
    public static final String UTF_16 = "UTF-16";
    public static final String GBK = "GBK";
    public static final String GB2312 = "GB2312";

    public static String toASCII(String str) throws UnsupportedEncodingException {
        return toCharset(str, US_ASCII);
    }
    public static String toISO_8859_1(String str) throws UnsupportedEncodingException {
        return toCharset(str, ISO_8859_1);
    }
    public static String toUTF_8(String str) throws UnsupportedEncodingException {
        return toCharset(str, UTF_8);
    }
    public static String toUTF_16BE(String str) throws UnsupportedEncodingException {
        return toCharset(str, UTF_16BE);
    }
    public static String toUTF_16LE(String str) throws UnsupportedEncodingException {
        return toCharset(str, UTF_16LE);
    }
    public static String toUTF_16(String str) throws UnsupportedEncodingException {
        return toCharset(str, UTF_16);
    }
    public static String toGBK(String str) throws UnsupportedEncodingException {
        return toCharset(str, GBK);
    }
    public static String toGB2312(String str) throws UnsupportedEncodingException {
        return toCharset(str, GB2312);
    }

    public static String fromASCII(String str) throws UnsupportedEncodingException {
        return fromCharset(str, US_ASCII);
    }
    public static String fromISO_8859_1(String str) throws UnsupportedEncodingException {
        return fromCharset(str, ISO_8859_1);
    }
    public static String fromUTF_8(String str) throws UnsupportedEncodingException {
        return fromCharset(str, UTF_8);
    }
    public static String fromUTF_16BE(String str) throws UnsupportedEncodingException {
        return fromCharset(str, UTF_16BE);
    }
    public static String fromUTF_16LE(String str) throws UnsupportedEncodingException {
        return fromCharset(str, UTF_16LE);
    }
    public static String fromUTF_16(String str) throws UnsupportedEncodingException {
        return fromCharset(str, UTF_16);
    }
    public static String fromGBK(String str) throws UnsupportedEncodingException {
        return fromCharset(str, GBK);
    }
    public static String fromGB2312(String str) throws UnsupportedEncodingException {
        return fromCharset(str, GB2312);
    }

    public static String toCharset(String str, String newCharset) throws UnsupportedEncodingException {
        if (str != null) {
            byte[] bs = str.getBytes();
            return new String(bs, newCharset);
        }
        return null;
    }
    public static String changeCharset(String str, String oldCharset, String newCharset) throws UnsupportedEncodingException {
        if (str != null) {
            byte[] bs = str.getBytes(oldCharset);
            return new String(bs, newCharset);
        }
        return null;
    }
    public static String fromCharset(String str, String oldCharset) throws UnsupportedEncodingException {
        if (str != null) {
            byte[] bs = str.getBytes(oldCharset);
            return new String(bs);
        }
        return null;
    }
}