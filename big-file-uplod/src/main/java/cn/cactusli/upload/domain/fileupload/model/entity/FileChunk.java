package cn.cactusli.upload.domain.fileupload.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * Package: cn.cactusli.upload.domain.fileupload.model.entity
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/6 11:08
 * @Github https://github.com/lixuanfengs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class FileChunk {
    //上传文件到指定目录
    private String path;
    //上传文件的文件名称
    private String name;
    //任务ID
    private String id;
    //总分片数量
    private Integer chunks;
    //当前为第几块分片
    private Integer chunk;
    //按多大的文件粒度进行分片
    private Long chunkSize;
    //分片对象
    private MultipartFile file;
    // MD5
    private String md5;

    //当前分片大小
    private Long size = 0L;
}
