### 串口接收程序 ###

本程序接收串口发送过来的特定内容,并将其保存为XML文件

1. 项目开发依赖32位的jdk,eclipse的32位版,jdk1.6以上
2. 【dependency】目录里是项目依赖的javacomm库
3. 项目基于maven构建,但是javacomm库又不在任何maven仓库中
4. 上述问题的解决方案是本地安装javacomm到maven仓库

安装comm.jar到本地maven仓库的方法参照:
[https://blog.csdn.net/u011713016/article/details/15813665](https://blog.csdn.net/u011713016/article/details/15813665)

```
mvn install:install-file -DgroupId=javax.comm -DartifactId=comm -Dversion=2.0.3 -Dpackaging=jar -Dfile=/path/to/file
```

