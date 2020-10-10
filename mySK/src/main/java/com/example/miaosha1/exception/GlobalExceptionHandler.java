package com.example.miaosha1.exception;

import com.example.miaosha1.redis.BasePrefix;
import com.example.miaosha1.result.CodeMsg;
import com.example.miaosha1.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        e.printStackTrace();

        if(e instanceof GlobalException){
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCodeMsg());
        }else if(e instanceof BindException){
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);

            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));//对绑定异常进行处理
        }else{
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}

