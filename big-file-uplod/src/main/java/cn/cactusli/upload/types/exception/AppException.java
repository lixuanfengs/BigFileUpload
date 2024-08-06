package cn.cactusli.upload.types.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Package: cn.cactusli.upload.types.exception
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:50
 * @Github https://github.com/lixuanfengs
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppException extends RuntimeException{

    private static final long serialVersionUID = 5317680961212299217L;

    /** 异常码 */
    private String code;

    /** 异常信息 */
    private String info;

    public AppException(String code) {
        this.code = code;
    }

    public AppException(String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public AppException(String code, String message) {
        this.code = code;
        this.info = message;
    }

    public AppException(String code, String message, Throwable cause) {
        this.code = code;
        this.info = message;
        super.initCause(cause);
    }

    @Override
    public String toString() {
        return "AppException{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
