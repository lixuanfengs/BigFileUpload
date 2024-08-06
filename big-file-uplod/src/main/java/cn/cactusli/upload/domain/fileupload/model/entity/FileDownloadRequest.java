package cn.cactusli.upload.domain.fileupload.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package: cn.cactusli.upload.model.entity
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:42
 * @Github https://github.com/lixuanfengs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadRequest {

    private String path;

    private String name;

}
