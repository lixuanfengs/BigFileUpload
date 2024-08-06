package cn.cactusli.upload.util;

import cn.cactusli.upload.domain.fileupload.model.entity.FileUploadRequest;
import cn.cactusli.upload.types.common.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Package: cn.cactusli.upload.util
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 15:07
 * @Github https://github.com/lixuanfengs
 */
@Slf4j
@Component
public class FilePathUtil implements ApplicationRunner {

    @Value("${upload.root.dir}")
    private String uploadRootDir;

    @Value("${upload.window.root}")
    private String uploadWindowRoot;



    @Override
    public void run(ApplicationArguments args) throws Exception {
        createUploadRootDir();
    }


    private void createUploadRootDir(){
        String path = getBasePath();
        File file = new File(path);
        if(!file.mkdirs()){
            file.mkdirs();
        }
    }



    public String getPath(){
        return uploadRootDir;
    }

    public String getBasePath(){
        String path = uploadRootDir;
        if(isWinOs()){
            path = uploadWindowRoot + uploadRootDir;
        }

        return path;
    }

    public String getPath(FileUploadRequest param){
        String path = this.getBasePath() + FileConstant.FILE_SEPARATORCHAR + param.getPath() + FileConstant.FILE_SEPARATORCHAR + param.getMd5();
        return path;
    }


    /**
     * 判断是否为window系统
     * @return
     */
    public static boolean isWinOs(){
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")){
            return true;
        }

        return false;

    }

    /**
     * 获取用户当前工作目录
     * @return
     */
    public static String getUserCurrentDir(){
        return System.getProperty("user.dir");
    }

}
