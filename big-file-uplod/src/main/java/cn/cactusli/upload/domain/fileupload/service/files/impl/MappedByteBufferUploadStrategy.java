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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
@UploadMode(mode = UploadModeEnum.MAPPED_BYTEBUFFER)
@Service
public class MappedByteBufferUploadStrategy extends SliceUploadTemplate {

    @Autowired
    private FilePathUtil filePathUtil;

    @Value("${upload.chunkSize}")
    private long defaultChunkSize;

    @Override
    public boolean upload(FileUploadRequest param) {

        RandomAccessFile tempRaf = null;
        FileChannel fileChannel = null;
        MappedByteBuffer mappedByteBuffer = null;
        try {
            String uploadDirPath = filePathUtil.getPath(param);
            File tmpFile = super.createTmpFile(param);
            tempRaf = new RandomAccessFile(tmpFile, "rw");
            fileChannel = tempRaf.getChannel();

            long chunkSize = Objects.isNull(param.getChunkSize()) ? defaultChunkSize * 1024 * 1024 : param.getChunkSize();
            //写入该分片数据
            long offset = chunkSize * param.getChunk();
            byte[] fileData = param.getFile().getBytes();
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset, fileData.length);
            mappedByteBuffer.put(fileData);
            boolean isOk = super.checkAndSetUploadProgress(param, uploadDirPath);
            return isOk;

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {

            FileUtil.freedMappedByteBuffer(mappedByteBuffer);
            FileUtil.close(fileChannel);
            FileUtil.close(tempRaf);

        }

        return false;
    }

}
