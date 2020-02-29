# Seckill2.0
## 船新版本2.1

在seckill的基础上极大的提高了性能
#  Feature
使用nginx作为反向代理，减少了tomcat的负荷和数据库的负载，能够有效提高吞吐量。  

使用nginx实现了动静分离  

使用token代替了cookie 可以满足移动端，微信等等的服务兼容性,但是也带来了一些问题。

由于我们默认使用序列化的方式存储数据,因此会使得数据不可读。这时候需要在RedisConfig.java中
定义序列化的方式，同时去除implements Serializable。key使用string的序列化方式，value采用jason的
序列化方式  

虽然使用redis作为缓存保存商品数据使得商品数据下次访问就可以直接读取内存而不需要访问数据库
但是存在redis内存中数据不更新等问题。
