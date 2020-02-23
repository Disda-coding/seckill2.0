# seckill
## 开发日志

## 数据库
用户数据库分为用户信息库和用户密码库  
所有字段都为非null。 id字段需要递增，年龄默认为0，性别默认为0，其余字段使用默认值""。  

## 类和数据库的映射
通过Mybatis的自动生成工具产生Mapper和DO类，实现数据库和类之间的映射
## 异常
定义了CommonError的接口用于设置和返回异常信息和代码  

定义了


## Controller  

包含viewobject
里面将UserDO对象和UserPassword对象合并成为UserVO，包含所有用户属性  
目的是为了提供开发者操作用户属性。
----------------------------------
在BaseController中  
1. 定义了 CONTENT_TYPE_FORMED="application/x-www-form-urlencoded"
2. 定义了 ExceptionHandler用于处理抛出的异常  
如果不使用自定义异常处理机制的话，业务的异常也会被上层接收从而抛出500开头的服务器异常。
因此使用了@ExceptionHandler(Exception.class)和@ResponseStatus(HttpStatus.OK)分别用于处理异常和设置HTTP状态码不让客户端返回500页面
Map用来封装错误代码和错误信息，通过@ResponseBody用来封装成json格式返回给客户端处理。如果不是自定义异常那么就是网络异常，抛出未知异常。
