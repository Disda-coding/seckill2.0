package com.taobao.service.impl;

import com.taobao.dao.UserDOMapper;
import com.taobao.dao.UserPasswordDOMapper;
import com.taobao.dataobject.UserDO;
import com.taobao.dataobject.UserPasswordDO;
import com.taobao.service.UserService;
import com.taobao.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Override
    public UserModel getUserById(Integer id){
        UserDO userDO=userDOMapper.selectByPrimaryKey(id);
        if (userDO==null) return null;
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO,userPasswordDO);
    }
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if (userDO==null) return null;
        UserModel userModel=new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO!=null)
            userModel.setEncryptPassword(userPasswordDO.getEncryptPassword());
        return userModel;
    }
}
