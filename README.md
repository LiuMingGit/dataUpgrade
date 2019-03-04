# dataUpgrade
利用kettle进行数据抽取及转换的工具
* 先执行sql脚本生成三张关联表
    1. 数据抽取表：表示要对那些表进行数据抽取
       1. ![DATA_EXTRACTION](http://wx3.sinaimg.cn/mw690/0060lm7Tly1g0qpfeqhj7j30eg06c0sr.jpg "数据抽取表")    
    2. 表-字段-字典 对照表： 标识出表中哪个字段是字典要做转换，并标识字典名
        1. ![table_field](http://wx4.sinaimg.cn/mw690/0060lm7Tly1g0qpfu024aj30f808sgls.jpg "表-字段-字典 对照表")
    3. 新老字典对照表：标识字典名、老字典值、新字典值的对应关系
        1. ![dic_dic](http://wx2.sinaimg.cn/mw690/0060lm7Tly1g0qpga94ehj30es08s3yo.jpg "新老字典对照表")
* 修改oracle.yaml配置文件已应对自身环境
     1. ![oracle.yaml](http://wx4.sinaimg.cn/mw690/0060lm7Tly1g0qpsgb4v0j30kg0n80we.jpg "工具配置文件")
* 运行
```
    //实例化脚本执行类
    DataUpgrade dataUpgrade = new DataUpgrade();
    //执行脚本
    dataUpgrade.doWork();
   ```
* 如图示：是根据dataUpdate.xml文件导入资源库生成的kettle脚本文件
    1. ![dataUpdate](http://wx4.sinaimg.cn/mw690/0060lm7Tly1g0qpz5uqezj30og0gswgp.jpg "kettle脚本文件")
* 注：dataUpdate.xml文件是kettle脚本文件，用来导入资源库。请不要进行修改。