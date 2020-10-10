package com.example.miaosha1.exception;

import com.example.miaosha1.result.CodeMsg;

public class GlobalException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg){
        super();
        this.codeMsg = codeMsg;//在GlobalException中加入CodeMsg属性，就可以在异常中获取CodeMsg了
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
