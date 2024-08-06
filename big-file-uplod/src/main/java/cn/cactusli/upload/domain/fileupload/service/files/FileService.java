package cn.cactusli.upload.domain.fileupload.service.files;

import cn.cactusli.upload.domain.fileupload.model.entity.FileUpload;
import cn.cactusli.upload.domain.fileupload.model.entity.FileUploadRequest;

import java.io.IOException;

/**
 * Package: cn.cactusli.upload.domain.fileupload.service.files
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:55
 * @Github https://github.com/lixuanfengs
 */
public interface FileService {

    FileUpload upload(FileUploadRequest fileUploadRequestDTO)throws IOException;

    FileUpload sliceUpload(FileUploadRequest fileUploadRequestDTO);

    FileUpload checkFileMd5(FileUploadRequest fileUploadRequestDTO)throws IOException;
}
