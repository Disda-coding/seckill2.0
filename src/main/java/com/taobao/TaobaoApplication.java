package com.taobao;

import com.taobao.dao.UserDOMapper;
import com.taobao.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.taobao"})
@RestController
@MapperScan("com.taobao.dao")

public class TaobaoApplication {
    @Autowired
    private UserDOMapper userDOMapper;

    public static void main( String[] args){

        SpringApplication.run(TaobaoApplication.class,args);
    }
    @RequestMapping("/a")
    public String test(){
        UserDO u = new UserDO();
        u.setAge(1);
        u.setGender((byte) 1);
        u.setId(1);
        u.setRegisterMode("aa");
        u.setName("hehe");
        u.setTelephone("123321123");
        u.setThirdPartId("adf");
        try{
            userDOMapper.insertSelective(u);
        }catch (Exception e){
            e.printStackTrace();
        }

        return "ok";
    }
    @RequestMapping("/")
    public String home(){
        UserDO userDO=userDOMapper.selectByPrimaryKey(1);
        if(userDO == null){
            return "用户对象不存在";
        }else{
            return userDO.getName();
        }
    }
}
