package com.lamzier.io.encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Md5加密类
 */
public class Md5 {

    /**
     * 加密
     */
    public static String doMd5(String username , String password){
        String text = username + password;
        return doMd5(text);
    }

    /**
     * 加密方法
     */
    private static String doMd5(String password){
        StringBuilder stringBuffer;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            //加密对象，指定加密方式
            byte[] b = password.getBytes();
            //准备要加密的数据
            byte[] digest = md5.digest(b);
            //16进制的字符
            char[] chars = new char[]{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
            stringBuffer = new StringBuilder();
            //处理十六进制字符串
            for (byte bb : digest){
                stringBuffer.append(chars[(bb >> 4) & 15]);
                stringBuffer.append(chars[bb & 15]);
            }
            //完成加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("MD5加密失败！");
            return null;
        }
        return stringBuffer.toString();
    }


}
