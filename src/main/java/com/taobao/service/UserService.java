package com.taobao.service;

import com.taobao.error.BusinessException;
import com.taobao.service.model.UserModel;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     *
     * @param telephone 用户注册手机
     * @param encryptedPassword 用户加密后的密码
     * @throws BusinessException
     */
    UserModel validateLogin(String telephone,String encryptedPassword) throws BusinessException;
}
