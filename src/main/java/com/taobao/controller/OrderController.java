package com.taobao.controller;

import com.taobao.error.BusinessException;
import com.taobao.error.EmBusinessError;
import com.taobao.mq.MqProducer;
import com.taobao.response.CommonReturnType;
import com.taobao.service.OrderService;
import com.taobao.service.UserService;
import com.taobao.service.model.OrderModel;
import com.taobao.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId")Integer itemId,
                                        @RequestParam("amount")Integer amount,
                                        @RequestParam(name="promoId" ,required=false)Integer promoId) throws BusinessException {
//        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

//        if(isLogin==null||!isLogin.booleanValue()){
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆");
//        }

        String token =  httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆");
        }


        //获取用户的登录信息
//        UserModel userModel=(UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null)
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户登录过期");

        //OrderModel orderModel=orderService.createOrder(userModel.getId(),itemId,promoId,amount);
        //加入库存流水init状态


        //再去完成对应的下单事务型消息

        if(mqProducer.transactionAsyncReduceStock(userModel.getId(),itemId,promoId,amount)==false)
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        return CommonReturnType.create(null);
    }
}
