package cn.cactusli.upload.domain.fileupload.service.files.impl;

import cn.cactusli.upload.domain.fileupload.model.entity.FileUploadRequest;
import cn.cactusli.upload.domain.fileupload.service.annotation.UploadMode;
import cn.cactusli.upload.domain.fileupload.service.template.SliceUploadTemplate;
import cn.cactusli.upload.types.enums.UploadModeEnum;
import cn.cactusli.upload.util.FilePathUtil;
import cn.cactusli.upload.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * Package: cn.cactusli.upload.domain.fileupload.service.files.impl
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:57
 * @Github https://github.com/lixuanfengs
 */
@Slf4j
@UploadMode(mode = UploadModeEnum.RANDOM_ACCESS)
@Service
public class RandomAccessUploadStrategy extends SliceUploadTemplate {

    @Autowired
    private FilePathUtil filePathUtil;

    @Value("${upload.chunkSize}")
    private long defaultChunkSize;

    @Override
    public boolean upload(FileUploadRequest param) {
        RandomAccessFile accessTmpFile = null;
        try {
            String uploadDirPath = filePathUtil.getPath(param);
            File tmpFile = super.createTmpFile(param);
            accessTmpFile = new RandomAccessFile(tmpFile, "rw");
            //这个必须与前端设定的值一致
            long chunkSize = Objects.isNull(param.getChunkSize()) ? defaultChunkSize * 1024 * 1024 : param.getChunkSize();
            long offset = chunkSize * param.getChunk();
            //定位到该分片的偏移量
            accessTmpFile.seek(offset);
            //写入该分片数据
            accessTmpFile.write(param.getFile().getBytes());
            boolean isOk = super.checkAndSetUploadProgress(param, uploadDirPath);
            return isOk;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            FileUtil.close(accessTmpFile);
        }
        return false;
    }

}
