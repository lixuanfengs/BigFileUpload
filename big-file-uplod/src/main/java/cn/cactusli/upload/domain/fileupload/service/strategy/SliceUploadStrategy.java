package cn.cactusli.upload.domain.fileupload.service.strategy;

import cn.cactusli.upload.domain.fileupload.model.entity.FileUpload;
import cn.cactusli.upload.domain.fileupload.model.entity.FileUploadRequest;

/**
 * Package: cn.cactusli.upload
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:20
 * @Github https://github.com/lixuanfengs
 */
public interface SliceUploadStrategy {

    FileUpload sliceUpload(FileUploadRequest param);

}
