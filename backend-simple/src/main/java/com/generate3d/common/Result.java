package com.generate3d.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;

    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public Result() {}
    public Result(int code, String message) { this.code = code; this.message = message; }
    public Result(int code, String message, T data) { this(code, message); this.data = data; }

    public static <T> Result<T> success() { return new Result<>(SUCCESS_CODE, "操作成功"); }
    public static <T> Result<T> success(T data) { return new Result<>(SUCCESS_CODE, "操作成功", data); }
    public static <T> Result<T> success(String message, T data) { return new Result<>(SUCCESS_CODE, message, data); }
    public static <T> Result<T> error() { return new Result<>(ERROR_CODE, "操作失败"); }
    public static <T> Result<T> error(String message) { return new Result<>(ERROR_CODE, message); }
}