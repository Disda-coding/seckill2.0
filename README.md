# Seckill2.0
## 船新版本2.1

在seckill的基础上极大的提高了性能
#  Feature

##  nginx
使用nginx作为反向代理，减少了tomcat的负荷和数据库的负载，能够有效提高吞吐量。  

使用nginx实现了动静分离  

使用token代替了cookie 可以满足移动端，微信等等的服务兼容性,但是也带来了一些问题。

##  Redis缓存机制
由于我们默认使用序列化的方式存储数据,因此会使得数据不可读。这时候需要在RedisConfig.java中
定义序列化的方式，同时去除implements Serializable。key使用string的序列化方式，value采用jason的
序列化方式。  

同时还需要新建对时间的序列化方式。因此新建了两个类用于序列化和去序列化 

虽然使用redis作为缓存保存商品数据使得商品数据下次访问就可以直接读取内存而不需要访问数据库
但是存在redis内存中数据不更新等问题。  

##  本地热点缓存：
特性：
* 热点数据
* 脏读非常不敏感
* 内存可控 
  
可以使用hashmap来做热点缓存，但是如果使用concurrenthashmap的话写数据的时候会对
读取的数据造成影响，而且我们还需要拥有淘汰机制的热点缓存如FIFO和timeout以及lRU等等

因此我们可以使用谷歌的Guava cache：  
*  可控制的大小和超时时间
*  可配置LRU策略
*  线程安全

多级缓存读取逻辑：
nginx proxy cache缓存-->JVM本地缓存-->Redis缓存-->数据库缓存

开启nginx proxy cahce后发现比开启本地缓存还慢，原因是因为nginx没有使用基于内存的缓存，而是硬盘缓存

因此我们抛弃了这种方法，然后开启nginx的内存缓存:nginx lua
