package com.example.miaosha1.result;

public class Result<T> {
    private int code;
    private String msg;
    private T data;

    private Result(T data) {
        this.code=CodeMsg.SUCCESS.getCode();
        this.msg=CodeMsg.SUCCESS.getMsg();
        this.data=data;
    }

    private Result(CodeMsg codeMsg){
        if(codeMsg==null)
            return;
        this.code=codeMsg.getCode();
        this.msg=codeMsg.getMsg();
    }

    //正确就带上信息
    public static <T>Result<T> success(T data){
        return new Result<T>(data);
    }

    //错误就带上错误码
    public static <T>Result<T> error(CodeMsg codeMsg){
        return new Result<T>(codeMsg);
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }
}
