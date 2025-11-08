package com.community.health.user.web;

import com.community.health.common.api.ApiResponse;
import com.community.health.common.exception.BusinessException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  public ApiResponse<?> biz(BusinessException e){ return new ApiResponse<>(e.getCode(), e.getMessage(), null); }
  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
  public ApiResponse<?> badRequest(Exception e){ return ApiResponse.error("请求不合法: "+ e.getMessage()); }
  @ExceptionHandler(DataAccessException.class)
  public ApiResponse<?> db(DataAccessException e){ return ApiResponse.error("数据库错误"); }
  @ExceptionHandler(Exception.class)
  public ApiResponse<?> other(Exception e){ return ApiResponse.error("服务器异常"); }
}
