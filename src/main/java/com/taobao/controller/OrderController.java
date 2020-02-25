package com.taobao.controller;

import com.taobao.error.BusinessException;
import com.taobao.error.EmBusinessError;
import com.taobao.response.CommonReturnType;
import com.taobao.service.OrderService;
import com.taobao.service.UserService;
import com.taobao.service.model.OrderModel;
import com.taobao.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
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

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam("itemId")Integer itemId,
                                        @RequestParam("amount")Integer amount) throws BusinessException {
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin==null||!isLogin.booleanValue()){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆");
        }

        //获取用户的登录信息
        UserModel userModel=(UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");

        OrderModel orderModel=orderService.createOrder(userModel.getId(),itemId,amount);

        return CommonReturnType.create(null);
    }
}
