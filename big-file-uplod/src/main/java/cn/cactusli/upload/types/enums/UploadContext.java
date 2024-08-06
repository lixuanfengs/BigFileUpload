package cn.cactusli.upload.types.enums;

import cn.cactusli.upload.domain.fileupload.service.annotation.UploadMode;
import cn.cactusli.upload.domain.fileupload.service.strategy.SliceUploadStrategy;
import cn.cactusli.upload.util.SpringContextHolder;
import org.reflections.Reflections;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
* Package: cn.cactusli.upload.types.enums
* Description:

* @Author 仙人球⁶ᴳ | 微信：Cactusesli
* @Date 2024/8/5 17:58
* @Github https://github.com/lixuanfengs 
*/
public enum UploadContext {

    INSTANCE;

    // private static final String PACKAGE_NAME = "com.github.lybgeek.upload.strategy.impl";

    private static final String PACKAGE_NAME = "cn.cactusli.upload.domain.fileupload.service.files.impl";

    private Map<UploadModeEnum,Class<SliceUploadStrategy>> uploadStrategyMap = new ConcurrentHashMap<>();


    UploadContext() {
        init();
    }

    public void init(){
        Reflections reflections = new Reflections(PACKAGE_NAME);
        Set<Class<?>> clzSet = reflections.getTypesAnnotatedWith(UploadMode.class);
        if(!CollectionUtils.isEmpty(clzSet)){
            for (Class<?> clz : clzSet) {
                UploadMode uploadMode = clz.getAnnotation(UploadMode.class);
                uploadStrategyMap.put(uploadMode.mode(), (Class<SliceUploadStrategy>) clz);
            }
        }
    }

    public SliceUploadStrategy getInstance(UploadModeEnum mode){
        return this.getStrategyByType(mode);

    }


    private SliceUploadStrategy getStrategyByType(UploadModeEnum mode){
        Class<SliceUploadStrategy> clz = uploadStrategyMap.get(mode);
        Assert.notNull(clz,"mode:"+mode+"can not found class,please checked");
        return SpringContextHolder.getBean(clz);
    }

}
