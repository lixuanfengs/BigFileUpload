package cn.cactusli.upload.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Package: cn.cactusli.upload.util
 * Description:
 *
 * @Author 仙人球⁶ᴳ | 微信：Cactusesli
 * @Date 2024/8/5 15:11
 * @Github https://github.com/lixuanfengs
 */
public class SimpleAsyncExec {

    private final static SimpleAsyncExec inst = new SimpleAsyncExec();
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private static Logger logger = LoggerFactory.getLogger(SimpleAsyncExec.class);

    public static SimpleAsyncExec getInstance() {
        return inst;
    }

    public <T> T exec(Callable callable) {
        T t = null;
        //logger.info("SimpleAsyncExec exec start。。。");
        Future<T> future = executor.submit(callable);
        try {
            t = future.get();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(),e);
        }

        return t;
    }


    public void exec(Runnable run) {
        //logger.info("SimpleAsyncExec exec start。。。");
        executor.submit(run);
    }
}
