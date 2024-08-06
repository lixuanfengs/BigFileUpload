package cn.cactusli.upload.domain.fileupload.repository;

import cn.cactusli.upload.domain.fileupload.model.entity.FileChunk;

import java.util.List;

/**
 * Package: cn.cactusli.upload.domain.fileupload.repository
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/6 11:07
 * @Github https://github.com/lixuanfengs
 */
public interface FileChunkMapper {


    /**
     * 通过 md5 查询记录
     *
     * @param md5 md5
     * @return
     */
    List<FileChunk> listByMd5(String md5);

    /**
     * 批量新增记录
     *
     * @param fileChunkList fileChunkList
     * @return
     */
    int batchInsert(List<FileChunk> fileChunkList);

    /**
     * 删除记录
     *
     * @param fileChunk fileChunk
     * @return
     */

    int delete(FileChunk fileChunk);

    FileChunk getById(Long id);

    int insert(FileChunk fileChunk);

    int update(FileChunk fileChunk);
}
