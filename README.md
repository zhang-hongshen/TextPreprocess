### 项目介绍

​	该项目是搜索引擎中的前期文本预处理阶段，可以将爬取到的html文件提取纯文本并返回词组。项目正在开发中且本人能力有限，如有bug请谅解。

### 第三方工具

| 工具             | 名称                        |
| :--------------- | --------------------------- |
| 中文分词工具     | 阿里云NLP中文分词（基础版） |
| 英文词干提取算法 | Porter Stemming             |
| jar包            | fastjson-1.2.76.jar         |

### 环境搭建

| 工具          | 版本号 |
| ------------- | ------ |
| IntelliJ IDEA | 2021.1 |
| JDK           | 11.0   |

### 个人参数填写

```java
package main.java.com.zhanghongshen.textpreprocess;

public class AliyunNlp {
    /**
     * 个人参数
     */
    private String ResourceOwnerAccount = "";
    private String AccessKeyId = "";
    private String AccessKeySecret = "";
 }
```


### 该版本已知问题

1. 中文文档处理时，在分词阶段由于阿里云NLP的中文分词功能调用频率限制，如果在极小一段时间内请求过多会有很大概率不成功；
2. 英文文档处理时，Porter Stemming提取词干不准确会出现单词还原错误的情况。