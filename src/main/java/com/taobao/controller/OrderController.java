package com.taobao.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.taobao.error.BusinessException;
import com.taobao.error.EmBusinessError;
import com.taobao.mq.MqProducer;
import com.taobao.response.CommonReturnType;
import com.taobao.service.ItemService;
import com.taobao.service.OrderService;
import com.taobao.service.PromoService;
import com.taobao.service.UserService;
import com.taobao.service.impl.PromoServiceImpl;
import com.taobao.service.model.OrderModel;
import com.taobao.service.model.UserModel;
import com.taobao.util.CodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    private ExecutorService executorService;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init(){
        executorService= Executors.newFixedThreadPool(20);
        orderCreateRateLimiter=RateLimiter.create(100);

    }
    //生成验证码
    @RequestMapping(value = "/generateverifycode",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public void generateverifycode(HttpServletResponse response) throws BusinessException, IOException {
        //根据token获取用户信息
        String token =  httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能生成验证码");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null)
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能生成验证码");
        //获取秒杀访问令牌

        Map<String,Object> map = CodeUtil.generateCodeAndPic();
        redisTemplate.opsForValue().set("veryfy_code_"+userModel.getId(), map.get("code"));
        redisTemplate.expire("veryfy_code_"+userModel.getId(),10,TimeUnit.MINUTES);
        ImageIO.write((RenderedImage)map.get("codePic"),"jpeg", response.getOutputStream());


    }


    @RequestMapping(value = "/generatetoken",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generatetoken(@RequestParam("itemId")Integer itemId,
                                        @RequestParam(name="promoId")Integer promoId,
                                          @RequestParam(name="verifyCode")String verifyCode) throws BusinessException {
        //根据token获取用户信息
        String token =  httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆");
        }


        //获取用户的登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null)
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户登录过期");

        //通过verifycode验证验证码有效性
        String redisVerifyCode = (String) redisTemplate.opsForValue().get("veryfy_code_"+userModel.getId());
        if(StringUtils.isEmpty(redisVerifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请求非法");
        }
        if (!redisVerifyCode.equalsIgnoreCase(verifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "验证码错误,请求非法");
        }


        //获取秒杀访问令牌
        String promoToken =promoService.generateSecondKillToken(promoId, itemId, userModel.getId());

        if(promoId==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");

        }
        return CommonReturnType.create(promoToken);
    }


    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId")Integer itemId,
                                        @RequestParam("amount")Integer amount,
                                        @RequestParam(name="promoId" ,required=false)Integer promoId,
                                        @RequestParam(name="promoToken" ,required=false)String promoToken) throws BusinessException {
//        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

//        if(isLogin==null||!isLogin.booleanValue()){
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆");
//        }


        if(orderCreateRateLimiter.acquire()<=0){
            throw new BusinessException(EmBusinessError.RATELIMIT);
        }
        String token =  httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆");
        }


        //获取用户的登录信息
//        UserModel userModel=(UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null)
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户登录过期");

        //校验秒杀令牌是否正确
        if(promoId!=null){
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userModel.getId()+"_itemid_"+itemId);

            if (inRedisPromoToken==null||!StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }
        //OrderModel orderModel=orderService.createOrder(userModel.getId(),itemId,promoId,amount);

        //同步调用线程池的submit的方法
        //拥塞窗口为20的等待队列，用于泄洪
        Future<Object> future=executorService.submit(new Callable<Object>(){
           @Override
            public Object call() throws Exception{
               //加入库存流水init状态
               String stockLogId=itemService.initStockLog(itemId,amount);

               //再去完成对应的下单事务型消息

               if(mqProducer.transactionAsyncReduceStock(userModel.getId(),itemId,promoId,amount,stockLogId)==false)
                   throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
               return null;
           }
        });
        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }

        return CommonReturnType.create(null);

    }
}
