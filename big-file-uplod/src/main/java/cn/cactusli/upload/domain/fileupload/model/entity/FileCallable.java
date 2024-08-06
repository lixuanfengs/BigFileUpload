package cn.cactusli.upload.domain.fileupload.model.entity;

import cn.cactusli.upload.types.enums.UploadContext;
import cn.cactusli.upload.types.enums.UploadModeEnum;

import java.util.concurrent.Callable;

/**
 * Package: cn.cactusli.upload.model.entity
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:17
 * @Github https://github.com/lixuanfengs
 */
public class FileCallable implements Callable<FileUpload> {
    private UploadModeEnum mode;

    private FileUploadRequest param;

    public FileCallable(UploadModeEnum mode,
                        FileUploadRequest param) {

        this.mode = mode;
        this.param = param;
    }

    @Override
    public FileUpload call() throws Exception {
        FileUpload fileUploadDTO = UploadContext.INSTANCE.getInstance(mode).sliceUpload(param);
        return fileUploadDTO;
    }
}
