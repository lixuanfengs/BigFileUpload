package cn.cactusli.upload.types.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Package: cn.cactusli.upload.types.common
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:51
 * @Github https://github.com/lixuanfengs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
public class Result<T> {

    public static final int success = 0;
    public static final int fail = 1;
    private int status = success;
    private String message = "success";
    private T data;

    public Result setErrorMsgInfo(String msg){

        this.setStatus(fail);
        this.setMessage(msg);
        return this;

    }
}
