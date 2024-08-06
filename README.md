# **SpringBoot超大文件分片上传**

## 简介

文文件上传是一个常见的话题。对于小文件，可以直接将其转化为字节流上传到服务器。但是对于大文件，普通的上传方式可能会导致上传中断后需要重新开始，这是一种不好的用户体验。为了解决这个问题，本文介绍了几种更好的上传方式：分片上传、断点续传、秒传。这些方法可以提供更好的上传体验，适用于大文件上传和网络环境不稳定的情况。本文将详细介绍这些方法的原理和实现方式。

## 分片上传

### 1.什么是分片上传

分片上传是一种文件上传方法，它将大文件分割成多个较小的数据块（称为Part），分别上传这些数据块，然后由服务器将所有上传的部分重新组合成完整的原始文件。这种方法可以有效处理大文件上传的问题。

### 2.分片上传的应用场景

* **大文件上传：**当需要上传体积较大的文件时，分片上传可以提高上传效率和成功率。

* **网络环境不稳定：**在网络连接可能中断的情况下，分片上传可以降低上传失败的风险。

### 3.实现分片上传的核心逻辑

* **文件分割：**在客户端将文件划分为固定大小的数据块。

* **初始化上传：**向服务器发送上传请求，获取唯一的上传标识。

* **分片上传：**按照一定的策略（串行或并行）上传各个数据块。

* **上传确认：**服务器检查接收到的所有分片是否完整。

* **文件合并：**服务器将接收到的所有分片重新组合成完整的文件。

## 断点续传

### 1.什么是断点续传

断点续传是在下载或上传过程中，将任务划分为多个部分，每部分采用单独的线程进行传输。如果传输中断，可以从已完成的部分开始继续传输，而不必从头开始。本文的断点续传主要是针对断点上传场景。

### 2.断点续传应用场景

断点续传可以看成是分片上传的一个衍生，因此可以使用分片上传的场景，都可以使用断点续传。

### 3.实现断点续传的核心逻辑

在分片上传的过程中，如果因为系统崩溃或者网络中断等异常因素导致上传中断，这时候客户端需要记录上传的进度。在之后支持再次上传时，可以继续从上次上传中断的地方进行继续上传。
为了避免客户端在上传之后的进度数据被删除而导致重新开始从头上传的问题，服务端也可以提供相应的接口便于客户端对已经上传的分片数据进行查询，从而使客户端知道已经上传的分片数据，从而从下一个分片数据开始继续上传。

### 4.实现断点续传的核心步骤

* **方案一，常规步骤：**
  * 将需要上传的文件按照一定的分割规则，分割成相同大小的数据块；
  * 初始化一个分片上传任务，返回本次分片上传唯一标识；
  * 按照一定的策略（串行或并行）发送各个分片数据块；
  * 发送完成后，服务端根据判断数据上传是否完整，如果完整，则进行数据块合成得到原始文件。

* **方案二、本文实现的步骤：**
  * 前端（客户端）需要根据固定大小对文件进行分片，请求后端（服务端）时要带上分片序号和大小
  * 服务端创建`conf`文件用来记录分块位置，`conf`文件长度为总分片数，每上传一个分块即向`conf`文件中写入一个127，那么没上传的位置就是默认的0,已上传的就是`Byte.MAX_VALUE 127`（这步是实现断点续传和秒传的核心步骤）
  * 服务器按照请求数据中给的分片序号和每片分块大小（分片大小是固定且一样的）算出开始位置，与读取到的文件片段数据，写入文件。

## 秒传

### 1.什么是秒传

秒传是一种文件上传优化技术。当用户尝试上传一个文件时，服务器首先进行MD5校验。如果服务器上已经存在一个相同MD5值的文件，则直接返回上传成功的信息，而无需实际上传文件内容。这种方法可以大大节省上传时间和服务器存储空间。想要不秒传，其实只要让MD5改变，就是对文件本身做一下修改（改名字不行），例如一个文本文件，你多加几个字，MD5就变了，就不会秒传了。

### 2.本文实现的秒传核心逻辑

* 利用`redis`的`set`方法存放文件上传状态，其中`key`为文件上传的`md5`，`value`为是否上传完成的标志位。
* 当标志位`true`为上传已经完成，此时如果有相同文件上传，则进入秒传逻辑。如果标志位为`false`，则说明还没上传完成，此时需要在调用set的方法，保存块号文件记录的路径，其中`key`为上传文件`md5`加一个固定前缀，`value`为块号文件记录路径。

## 分片上传/断点上传代码实现

### 1.前端

推荐使用`vue-simple-uploader` 实现文件分片上传、断点续传及秒传。

> 相关连接: https://blog.csdn.net/fuhanghang/article/details/134525820
>
> https://blog.csdn.net/Guoxuxinwen/article/details/129945356

### 2.后端

**后端用两种方式实现文件写入：**RandomAccessFile、MappedByteBuffer。

#### 2.1 RandomAccessFile

RandomAccessFile 是 Java 提供的一个用于文件随机访问的类，它允许在文件的任意位置进行读写操作。使用 RandomAccessFile，可以通过文件指针来定位到文件的特定位置，然后进行读取或写入操作。这个类特别适合处理大文件和需要频繁跳转文件位置的场景，如分片上传中的文件写入。使用时，可以通过构造函数指定文件和访问模式（如 "r" 只读, "rw" 读写），然后使用 seek() 方法定位文件指针，再通过 read() 或 write() 方法进行数据操作。RandomAccessFile 的这些特性使其成为实现文件分片上传和断点续传的理想工具。

```java
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileExample {
    public static void main(String[] args) {
        File file = new File("example.txt");
        String content1 = "Hello, ";
        String content2 = "World!";
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            // 写入第一部分内容
            raf.write(content1.getBytes());
            
            // 移动文件指针到文件末尾
            raf.seek(raf.length());
            
            // 写入第二部分内容
            raf.write(content2.getBytes());
            
            // 移动文件指针到开头
            raf.seek(0);
            
            // 读取整个文件内容
            byte[] buffer = new byte[(int) raf.length()];
            raf.readFully(buffer);
            
            System.out.println("File content: " + new String(buffer));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

#### 2.2 MappedByteBuffer

MappedByteBuffer 是 Java NIO 包中的一个类，用于实现高效的文件读写操作。它通过将文件的一部分或全部直接映射到内存中，允许像操作内存一样来操作文件，从而大大提高了文件的读写性能。在文章中，MappedByteBuffer 被用于实现分片上传的文件写入操作。使用时，首先通过 RandomAccessFile 获取 FileChannel，然后使用 FileChannel 的 map() 方法创建 MappedByteBuffer，指定要映射的文件区域。之后，可以直接使用 MappedByteBuffer 的 put() 方法将数据写入文件。这种方法特别适合处理大文件，因为它避免了频繁的系统调用，提高了文件操作的效率。

```java
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteBufferExample {
    public static void main(String[] args) {
        try {
            // 打开文件，创建 RandomAccessFile 对象
            RandomAccessFile file = new RandomAccessFile("example.txt", "rw");
            
            // 获取文件通道
            FileChannel channel = file.getChannel();
            
            // 将文件的特定部分映射到内存中
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, channel.size());
            
            // 读取映射缓冲区中的数据
            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get()); // 读取一个字节
            }
            
            // 修改文件中的一个字符
            buffer.put(0, (byte) 'M'); // 将位置 0 的字符修改为 'M'
            
            // 关闭通道和文件
            channel.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### 2.3 后端进行写入操作的核心代码

* **RandomAccessFile 实现方式**

  ```java
  @Slf4j
  @UploadMode(mode = UploadModeEnum.RANDOM_ACCESS)
  @Service
  public class RandomAccessUploadStrategy extends SliceUploadTemplate {
  
      @Autowired
      private FilePathUtil filePathUtil;
  
      @Value("${upload.chunkSize}")
      private long defaultChunkSize;
  
      @Override
      public boolean upload(FileUploadRequest param) {
          RandomAccessFile accessTmpFile = null;
          try {
              String uploadDirPath = filePathUtil.getPath(param);
              File tmpFile = super.createTmpFile(param);
              accessTmpFile = new RandomAccessFile(tmpFile, "rw");
              //这个必须与前端设定的值一致
              long chunkSize = Objects.isNull(param.getChunkSize()) ? defaultChunkSize * 1024 * 1024 : param.getChunkSize();
              long offset = chunkSize * param.getChunk();
              //定位到该分片的偏移量
              accessTmpFile.seek(offset);
              //写入该分片数据
              accessTmpFile.write(param.getFile().getBytes());
              boolean isOk = super.checkAndSetUploadProgress(param, uploadDirPath);
              return isOk;
          } catch (IOException e) {
              log.error(e.getMessage(), e);
          } finally {
              FileUtil.close(accessTmpFile);
          }
          return false;
      }
  
  }
  
  ```

* **MappedByteBuffer实现方式**

  ```java
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
  ```

* 文件操作核心模板类代码:

  ```java
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
          RandomAccessFile accessConfFile = null;
          try {
              accessConfFile = new RandomAccessFile(confFile, "rw");
              //把该分段标记为 true 表示完成
              System.out.println("set part " + param.getChunk() + " complete");
  
              //创建conf文件文件长度为总分片数，每上传一个分块即向conf文件中写入一个127，那么没上传的位置就是默认0,已上传的就是Byte.MAX_VALUE 127
              accessConfFile.setLength(param.getChunks());
              accessConfFile.seek(param.getChunk());
              accessConfFile.write(Byte.MAX_VALUE);
  
              //completeList 检查是否全部完成,如果数组里是否全部都是127(全部分片都成功上传)
              byte[] completeList = FileUtils.readFileToByteArray(confFile);
              isComplete = Byte.MAX_VALUE;
              for (int i = 0; i < completeList.length && isComplete == Byte.MAX_VALUE; i++) {
                  //与运算, 如果有部分没有完成则 isComplete 不是 Byte.MAX_VALUE
                  isComplete = (byte) (isComplete & completeList[i]);
                  System.out.println("check part " + i + " complete?:" + completeList[i]);
              }
  
          } catch (IOException e) {
              log.error(e.getMessage(), e);
          } finally {
              FileUtil.close(accessConfFile);
          }
          boolean isOk = setUploadProgress2Redis(param, uploadDirPath, fileName, confFile, isComplete);
          return isOk;
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
                  System.out.println("upload complete !!" + fileUploadDTO.isUploadComplete() + " name=" + fileName);
                  //TODO 保存文件信息到数据库
  
              }
  
          } catch (Exception e) {
              log.error(e.getMessage(), e);
          } finally {
  
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
  ```



## 总结

在实现分片上传的过程，需要前端和后端配合，比如前后端的上传块号的文件大小，前后端必须得要一致，否则上传就会有问题。其次文件相关操作正常都是要搭建一个文件服务器的，比如使用fastdfs、hdfs等。
本示例代码在电脑配置为8核内存32G情况下，上传5G大小的文件，上传时间需要8多分钟，主要时间耗费在前端的md5值计算，后端写入的速度还是比较快。如果项目组觉得自建文件服务器太花费时间，且项目的需求仅仅只是上传下载，那么推荐使用阿里的oss服务器。