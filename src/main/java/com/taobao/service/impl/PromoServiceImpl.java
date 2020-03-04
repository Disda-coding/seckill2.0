package com.taobao.service.impl;

import com.taobao.dao.PromoDOMapper;
import com.taobao.dataobject.PromoDO;
import com.taobao.error.BusinessException;
import com.taobao.error.EmBusinessError;
import com.taobao.service.ItemService;
import com.taobao.service.PromoService;
import com.taobao.service.UserService;
import com.taobao.service.model.ItemModel;
import com.taobao.service.model.PromoModel;
import com.taobao.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDOMapper promoDOMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取队友商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        //dataobject->model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel==null) return null;

        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO.getItemId()==null||promoDO.getItemId().intValue()==0){
            return;
        }
        ItemModel itemModel=itemService.getItemById(promoDO.getItemId());

        //讲库存同步到Redis内 等秒杀上线后才将数据写入缓存，以防不一致
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(),itemModel.getStock());
        //将大闸的限制数字设置到redis内
        redisTemplate.opsForValue().set("promo_door_count_"+promoId, itemModel.getStock().intValue()*5);

    }

    @Override
    public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) throws BusinessException {


        //判断库存是否已售罄，若存在直接返回下单失败
        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);

        }
        //获取队友商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);

        //dataobject->model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel==null) return null;

        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        //判断活动是否正在进行
        if(promoModel.getStatus().intValue()!=2){
            return null;
        }
        //判断item信息是否存在
        ItemModel itemModel=itemService.getItemByIdInCache(itemId);
        if (itemModel == null)
            return null;
        //判断用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if(userModel == null)
            return null;
        //获取秒杀大闸的count数量
        long result = redisTemplate.opsForValue().increment("promo_door_count_"+promoId,-1);
        if(result<0) return null;

        //生成token冰泉存入redis给5分钟有效期
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId, token);
        redisTemplate.expire("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,5, TimeUnit.MINUTES);
        return token;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO==null) return null;
        PromoModel promoModel=new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
