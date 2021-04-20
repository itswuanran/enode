# SQL脚本
如果修改表结构的话，EventStore需要自定义一套实现
一般直接使用默认的表结构，和默认的EventStore即可

# EventStore
选型不同的EventStore，需要指定唯一索引，这种约束是为了实现幂等处理