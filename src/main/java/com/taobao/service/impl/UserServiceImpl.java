package com.taobao.service.impl;

import com.taobao.dao.UserDOMapper;
import com.taobao.dao.UserPasswordDOMapper;
import com.taobao.dataobject.UserDO;
import com.taobao.dataobject.UserPasswordDO;
import com.taobao.error.BusinessException;
import com.taobao.error.EmBusinessError;
import com.taobao.service.UserService;
import com.taobao.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException{
        if (userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        if (StringUtils.isEmpty(userModel.getName())
                ||userModel.getGender()==null
                ||userModel.getAge()==null
                ||StringUtils.isEmpty(userModel.getTelephone()))
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        UserDO userDO = new UserDO();

        //实现model->userDO的方法
        userDO=convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已被使用!");
        }



        //insertSelective就是未定义的值不去覆盖 在update的时候极其有用
        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO=convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

    }
    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel==null) return null;
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncryptPassword(userModel.getEncryptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }
    private UserDO convertFromModel(UserModel userModel){
        if(userModel==null) return null;
        UserDO userDO=new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
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
