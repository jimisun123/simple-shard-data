# simple-shard-data

简单的数据水平分库服务,支持将主键为数据库自增的大表根据业务水平拆分为n个表

* 支持配置初始消费id
* 支持配置每次任务消费多少数据
* 支持配置每次任务结束任务休眠多长时间
* 支持配置每次任务需要启动多少线程
* 支持线程锁保证线程消费数据成功

本服务经过某省核算数据水平分表的检验,默认配置每小时能分表数据1800万数据，请根据机器的配置配置
