package com.taobao.service;

import com.taobao.error.BusinessException;
import com.taobao.service.model.PromoModel;

public interface PromoService {
    //根据itemId获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);
    void publishPromo(Integer promoId);
    //生成秒杀用的令牌
    String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) throws BusinessException;
}
