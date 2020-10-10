package com.example.miaosha1.util;


import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
    private static final String salt = "1a2b3c4d";

    public static String md5(String src){

        return DigestUtils.md5Hex(src);//MD5工具方法
    }

    public static String inputPassToFormPass(String inputPass){//输入密码转换到form密码
        String str=//在MD5之前，先加盐，就算别人得到了MD5反推，也只能得到密码为 12123456c3
                ""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);

        return md5(str);//第一次MD5
    }

    public static String formPassToDBPass(String formPass, String salt){//form密码转换到数据库密码
        String str=//在MD5之前，先加盐
                ""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);

        return md5(str);//第二次MD5
    }

    public static String inputPassToDBPass(String inputPass, String saltDB){//inputPass密码转换到数据库密码
        String formPass = inputPassToFormPass(inputPass);//第一次MD5
        String dbPass = formPassToDBPass(formPass,saltDB);//第二次MD5

        return dbPass;
    }

    public static void main(String[] args) {
        //System.out.println(inputPassToFormPass("123456"));
        //System.out.println(formPassToDBPass(inputPassToFormPass("123456"),"1a2b3c4d"));
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }
}
