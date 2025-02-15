package com.github.herdeny.healthaiv2.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code; //状态码
    private String msg;//提示信息
    private T data;//响应数据

    public static <E> Result<E> success(E data) {
        return new Result<>(0, "success", data);
    }

    public static <E> Result<E> success() {
        return new Result<>(0, "success", null);
    }

    public static <E> Result<E> error(String msg) {
        return new Result<>(420, msg, null);
    }

    public static <E> Result<E> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <E> Result<E> error(int code) {
        return new Result<>(code, null, null);
    }

    public static <E> Result<E> error(int code, String msg, E data) {
        return new Result<>(code, msg, data);
    }
}