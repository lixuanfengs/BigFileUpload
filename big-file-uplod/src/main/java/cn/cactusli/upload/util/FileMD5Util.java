package cn.cactusli.upload.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Package: cn.cactusli.upload.util
 * Description:
 *  计算文件MD5工具类
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 15:12
 * @Github https://github.com/lixuanfengs
 */
public class FileMD5Util {

    private final static Logger logger = LoggerFactory.getLogger(FileMD5Util.class);

    public static String getFileMD5(File file) throws FileNotFoundException {

        String value = null;
        FileInputStream in = new FileInputStream(file);
        MappedByteBuffer byteBuffer = null;
        try {
            byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
            if (value.length() < 32) {
                value = "0" + value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(in, byteBuffer);
        }
        return value;
    }

    public static String getFileMD5(MultipartFile file) {

        try {
            byte[] uploadBytes = file.getBytes();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(uploadBytes);
            String hashString = new BigInteger(1, digest).toString(16);
            return hashString;
        } catch (IOException e) {
            logger.error("get file md5 error!!!", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("get file md5 error!!!", e);
        }
        return null;
    }
}
