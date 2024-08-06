package cn.cactusli.upload.domain.fileupload.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Package: cn.cactusli.upload.model.entity
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 11:17
 * @Github https://github.com/lixuanfengs
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUpload {

    private String path;

    private Integer mtime;

    private boolean uploadComplete;

    private int code;

    private Map<Integer,String> chunkMd5Info;

    private List<Integer> missChunks;

    private long size;

    private String fileExt;

    private String fileId;

}
