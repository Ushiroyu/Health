package com.community.health.common.api;

public record ApiResponse<T>(int code, String message, T data) {
  public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(0, "ok", data); }
  public static <T> ApiResponse<T> ok() { return new ApiResponse<>(0, "ok", null); }
  public static <T> ApiResponse<T> error(String msg) { return new ApiResponse<>(-1, msg, null); }
}
