package com.taobao.service;

import com.taobao.error.BusinessException;
import com.taobao.service.model.UserModel;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;
}
