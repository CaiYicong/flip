# Flip

Flip定义：一个Flip操作是将一个合法的英语单词调换一个字母从而获得一个新的合法的英语单词，其调换字母的位置不发生变化。 例如：dig --> dog 就是一个Flip操作。单词的合法性可以通过/usr/share/dict/linux.words是否包含特定单词来确认该单词是否合法。

## linux.words分析

linux.words提供了合法单词的字典，数量是可控并且有限的；每一个Flip操作都需要检查单词的合法性，如果每一次操作都进行单词检查，将浪费大量的时间。

由于words包含的单词数量固定，因此可以对words中的所有单词进行预处理，把每个单词的合法Flip全部处理并记录，从而为后续每次Flip路径检索提供更快的支持。

Words单词及其相关的Flip操作，是天然的图模型，所有合法单词构成了图顶点，而合法的Flip操作即为顶点之间的边。

words相关统计数据：

- 纯字母单词数量：431727
- 合法Flip操作数量：921972

核心数据约为150万字符串，内存占用约为100MB数量级；从资源的预估量看，内存完全可以放下所有合法单词的图模型数据。

## linux.words预处理

Flip操作只能在具有相同数量字符的单词之间展开，所以可以根据单词的字符数量划分为不同的图，而最优Flip路径的查找只会发生在某个字符数量的图里面。因此内存中保存的图结构如下：

字符数---->单词---->[该单词合法Flip操作后的目标单词列表]

### 预处理流程

1. 依次读入linux.words中的单词，筛选出来纯字母的单词。
2. 对该单词每一位进行a~z、A~Z的替换，并检查替换后的单词是否已经存在内存图结构中；如果存在，则在该单词及替换后的单词图中分别保存这条边。

### 图数据保存

当单词字典集不变时，相应的Flip图结构也不会改变，所以可以保存与处理后的图数据，当需要使用时直接读取预处理后的数据即可，从而节省预处理所有单词所消耗的时间。从而做到了一次处理，多次使用。

## Flip最优路径搜索

获取两个单词之间的Flip最优路径，其实是对单词所在图的节点之间最短路径的查找。图的最短路径查找有广度遍历及深度遍历的方式，对于我们数据保存结构，使用广度遍历更合适。由于我们记录了以每个节点起始的Flip操作，所以可以对起始单词和中止单词同时进行广度遍历，直接寻找每个节点Flip交集找到共同节点即可结束搜索。

## 资源使用情况

![image-20200729031444538](image\MemUsage)

内存真实使用情况和预期基本一致160MB，还有可优化空间。

## 项目编译

根目录下，使用maven编译：

```shell
mvn clean package
```

编译完成后，在target目录下生成flip-1.0.tar.gz包。包含了运行脚本、所有依赖jar包及linux.words文件。

## 命令说明

使用命令前需设置JAVA_HOME环境变量。

解析linux.words命令：

```shell
bin/flip.sh -p parse -s data/linux.words -g data/linux.words.graph
```

输出示例：

SUCCESS parse data/linux.words to data/linux.words.graph !

查找Flip最短路径命令：

```shell
bin/flip.sh -p flip -s data/linux.words.graph -f cat -t pig
```

输出示例：

The shortest flip path from cat to pig:
[cat, bat, bag, big, pig]