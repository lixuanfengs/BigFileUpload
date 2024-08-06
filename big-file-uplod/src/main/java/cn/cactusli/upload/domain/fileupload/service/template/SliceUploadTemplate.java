package cn.cactusli.upload.domain.fileupload.service.template;

import cn.cactusli.upload.domain.fileupload.model.entity.FileUpload;
import cn.cactusli.upload.domain.fileupload.model.entity.FileUploadRequest;
import cn.cactusli.upload.domain.fileupload.service.strategy.SliceUploadStrategy;
import cn.cactusli.upload.types.common.FileConstant;
import cn.cactusli.upload.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Package: cn.cactusli.upload.domain.fileupload.service.template
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 14:51
 * @Github https://github.com/lixuanfengs
 */
@Slf4j
public abstract class SliceUploadTemplate implements SliceUploadStrategy {


    public abstract boolean upload(FileUploadRequest param);

    protected File createTmpFile(FileUploadRequest param) {
        FilePathUtil filePathUtil = SpringContextHolder.getBean(FilePathUtil.class);
        param.setPath(FileUtil.withoutHeadAndTailDiagonal(param.getPath()));
        String fileName = param.getFile().getOriginalFilename();
        String uploadDirPath = filePathUtil.getPath(param);
        String tempFileName = fileName + "_tmp";
        File tmpDir = new File(uploadDirPath);
        File tmpFile = new File(uploadDirPath, tempFileName);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        return tmpFile;
    }

    @Override
    public FileUpload sliceUpload(FileUploadRequest param) {
        boolean isOk = this.upload(param);
        if (isOk) {
            File tmpFile = this.createTmpFile(param);
            FileUpload fileUploadDTO = this.saveAndFileUploadDTO(param.getFile().getOriginalFilename(), tmpFile);
            return fileUploadDTO;
        }
        String md5 = FileMD5Util.getFileMD5(param.getFile());

        Map<Integer, String> map = new HashMap<>();
        map.put(param.getChunk(), md5);
        return FileUpload.builder().chunkMd5Info(map).build();
    }

    /**
     * 检查并修改文件上传进度
     */
    public boolean checkAndSetUploadProgress(FileUploadRequest param, String uploadDirPath) {
        String fileName = param.getFile().getOriginalFilename();
        File confFile = new File(uploadDirPath, fileName + ".conf");
        byte isComplete = 0;

        try (RandomAccessFile accessConfFile = new RandomAccessFile(confFile, "rw")) {
            markChunkComplete(accessConfFile, param.getChunks(), param.getChunk());
            isComplete = checkAllChunksComplete(confFile, param.getChunks());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return setUploadProgress2Redis(param, uploadDirPath, fileName, confFile, isComplete);
    }

    private void markChunkComplete(RandomAccessFile accessConfFile, int totalChunks, int chunkIndex) throws IOException {
        log.info("set part " + chunkIndex + " complete");
        accessConfFile.setLength(totalChunks);
        accessConfFile.seek(chunkIndex);
        accessConfFile.write(Byte.MAX_VALUE);
    }

    private byte checkAllChunksComplete(File confFile, int totalChunks) throws IOException {
        byte[] completeList = FileUtils.readFileToByteArray(confFile);
        byte isComplete = Byte.MAX_VALUE;
        for (int i = 0; i < completeList.length && isComplete == Byte.MAX_VALUE; i++) {
            isComplete = (byte) (isComplete & completeList[i]);
            log.info("check part " + i + " complete?:" + completeList[i]);
        }
        return isComplete;
    }

    /**
     * 把上传进度信息存进redis
     */
    private boolean setUploadProgress2Redis(FileUploadRequest param, String uploadDirPath, String fileName, File confFile, byte isComplete) {

        RedisUtil redisUtil = SpringContextHolder.getBean(RedisUtil.class);
        if (isComplete == Byte.MAX_VALUE) {
            redisUtil.hset(FileConstant.FILE_UPLOAD_STATUS, param.getMd5(), "true");
            redisUtil.del(FileConstant.FILE_MD5_KEY + param.getMd5());
            confFile.delete();
            return true;
        } else {
            if (!redisUtil.hHasKey(FileConstant.FILE_UPLOAD_STATUS, param.getMd5())) {
                redisUtil.hset(FileConstant.FILE_UPLOAD_STATUS, param.getMd5(), "false");
                redisUtil.set(FileConstant.FILE_MD5_KEY + param.getMd5(), uploadDirPath + FileConstant.FILE_SEPARATORCHAR + fileName + ".conf");
            }

            return false;
        }
    }

    /**
     * 保存文件操作
     */
    public FileUpload saveAndFileUploadDTO(String fileName, File tmpFile) {
        FileUpload fileUploadDTO = null;
        try {
            fileUploadDTO = renameFile(tmpFile, fileName);
            if (fileUploadDTO.isUploadComplete()) {
                log.info("upload complete !! " + fileUploadDTO.isUploadComplete() + " name=" + fileName);
                // TODO 保存文件信息到数据库
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return fileUploadDTO;
    }

    /**
     * 文件重命名
     *
     * @param toBeRenamed   将要修改名字的文件
     * @param toFileNewName 新的名字
     */
    private FileUpload renameFile(File toBeRenamed, String toFileNewName) {
        //检查要重命名的文件是否存在，是否是文件
        FileUpload fileUploadDTO = new FileUpload();
        if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {
            log.info("File does not exist: {}", toBeRenamed.getName());
            fileUploadDTO.setUploadComplete(false);
            return fileUploadDTO;
        }
        String ext = FileUtil.getExtension(toFileNewName);
        String p = toBeRenamed.getParent();
        String filePath = p + FileConstant.FILE_SEPARATORCHAR + toFileNewName;
        File newFile = new File(filePath);
        //修改文件名
        boolean uploadFlag = toBeRenamed.renameTo(newFile);

        fileUploadDTO.setMtime(DateUtil.getCurrentTimeStamp());
        fileUploadDTO.setUploadComplete(uploadFlag);
        fileUploadDTO.setPath(filePath);
        fileUploadDTO.setSize(newFile.length());
        fileUploadDTO.setFileExt(ext);
        fileUploadDTO.setFileId(toFileNewName);

        return fileUploadDTO;
    }


}
