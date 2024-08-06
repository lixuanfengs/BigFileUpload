package cn.cactusli.upload.domain.fileupload.service.annotation;

import cn.cactusli.upload.types.enums.UploadModeEnum;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Component
@Inherited
public @interface UploadMode {

  UploadModeEnum mode();
}
