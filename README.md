# Seckill
## 基础版本

## Database  
用户数据库分为用户信息库和用户密码库  
所有字段都为非null。 id字段需要递增，年龄默认为0，性别默认为0，其余字段使用默认值""。  

## 类和数据库的映射  
通过Mybatis的自动生成工具产生Mapper和DO类，实现数据库和类之间的映射  
*  在mybatis-generator.xml配置文件中在对应生成表类名配置中加入 enableCountByExample="false"enableUpdateByExample="false" enableDeleteByExample="false" enableSelectByExample="false"selectByExampleQueryId="false" 避免生成不常用方法
* insertSelective 中设置 keyProperty="id" useGeneratedKeys="true" 使得插入完后的 DO 生成自增 id 。 insertSelective与insert区别： insertSelective对应的sql语句加入了NULL校验，即只会插入数据不为null的字段值（null的字段依赖于数据库字段默认值）insert则会插入所有字段，会插入null。


## Return Type
使用一个统一的类去处理返回的数据，CommonReturnType。  
该类有两个属性，一个是status状态属性，另外一个是data数据属性。
若status=success,则data内返回前段需要的json数据，若status=fail，则data内使用通用的错误码格式  
同时异常被拦截后也根据异常类型返回不同的结果给前端处理

## Exception  
定义了CommonError的接口用于设置和返回异常信息和代码  

定义了BusinessException，继承了Exception类和实现了CommonError接口

构造了一个枚举EmBusinessError实现了CommonError接口

## Model
包含viewobject  
里面将UserDO对象和UserPassword对象合并成为UserVO，包含所有用户属性  
目的是为了提供开发者操作用户属性。

## Controller  
在BaseController中  
1. 定义了 CONTENT_TYPE_FORMED="application/x-www-form-urlencoded"
2. 定义了 ExceptionHandler用于处理抛出的异常  
如果不使用自定义异常处理机制的话，业务的异常也会被上层接收从而抛出500开头的服务器异常。
因此使用了@ExceptionHandler(Exception.class)和@ResponseStatus(HttpStatus.OK)分别用于处理异常和设置HTTP状态码不让客户端返回500页面
Map用来封装错误代码和错误信息，通过@ResponseBody用来封装成json格式返回给客户端处理。如果不是自定义异常那么就是网络异常，抛出未知异常。

在UserController中主要实现对用户的生命周期进行管理  
由于我们使用的是前后分离的架构，因此所有的请求都是通过Ajax来完成的，因此我们后端必须支持跨域请求  
使用@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")来对跨域进行支持  
同时前端需要设置xhrFields:{withCredentials:true}使得ajax能够支持跨域，然后通过设置CONTENT_TYPE_FORMED="application/x-www-form-urlencoded"来接收表单  

## Service
UserService接口中定义了
* 通过用户id得到用户模型的方法
* 注册方法
* 登录方法

通过UserServiceImpl实现类去实现业务层和持久层之间的逻辑交互，组装用户模型

## Validation
由于业务层要对每个方法去检测输入的值是否合理，代码冗余繁杂，因此可以引入javax包中的validation来解决此问题  
首先定义一个类ValidationResult，用来保存校验的结果集，然后通过StringUtils的join方法将验证结果封装到字符串中，并以逗号形式隔开。  
实现一个InitializingBean的实现类ValidatorImpl，实现了afterPropertiesSet()方法。  
其作用在于在Bean所有的属性都被注入之后会去调用这个afterPropertiesSet()方法，其实在依赖注入完成的时候，spring会去检查这个类是否实现了InitializingBean接口，如果实现了InitializingBean接口，就会去调用这个类的afterPropertiesSet()方法。所以afterPropertiesSet()方法的执行时间点就很清楚了，发生在所有的properties被注入后。  
将hibernate validator通过工厂初始化方式使其实例化。然后通过validate方法将错误信息返回。  
我们只需要在用户对象中对声明的属性调用@NotNull等等注解就可以对所有属性进行规范了。****

   


