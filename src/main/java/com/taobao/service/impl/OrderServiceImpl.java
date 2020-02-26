package com.taobao.service.impl;

import com.taobao.dao.OrderDOMapper;
import com.taobao.dao.SequenceDOMapper;
import com.taobao.dataobject.OrderDO;
import com.taobao.dataobject.SequenceDO;
import com.taobao.error.BusinessException;
import com.taobao.error.EmBusinessError;
import com.taobao.service.ItemService;
import com.taobao.service.OrderService;
import com.taobao.service.UserService;
import com.taobao.service.model.ItemModel;
import com.taobao.service.model.OrderModel;
import com.taobao.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException {
        //1.校验下单状态，下单商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");

        UserModel userModel = userService.getUserById(userId);
        if(userModel == null)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        if(amount<=0||amount>99)
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量信息不一致");
        //校验活动信息
        if(promoId !=null){
            //1 校验对应活动是否存在这个适用商品
            if(promoId.intValue()!=itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
                //2 校验活动是否正在进行中
            }else if(itemModel.getPromoModel().getStatus()!=2){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动还未开始");

            }
        }


        //2. 落单减库存，（支付减库存）
        boolean result =itemService.decreaseStock(itemId,amount);
        if (!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入户
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setPromoId(promoId);
        orderModel.setAmount(amount);
        if(promoId!=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号，订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO=this.convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        //加上商品的销量
        itemService.increaseSales(itemId,amount);
        //返回前端
        return orderModel;
    }
    //为了全局唯一性 在分布式场景即使生成序号失败了也不应该回滚，但generateOrderNo被Transactional注释了。
    //因此需要用Transactional propagation
    //REQUIRED: 必须要开启一个事务，并且在该事务当中，如果已经在一个事务中就可以不开启
    //REQUIRES_NEW:无论在不在事务中都会开启一个新的事务，执行完成后把新的事务提交
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo(){
        //订单号有16位
        StringBuilder sb = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now=LocalDateTime.now();
        String nowDate=now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(nowDate);

        //中间6位为自增序列  如果超出6位就存在问题！先不解决
        //获取当前sequence
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        int seq=sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String seqStr=String.valueOf(seq);
        for (int i=0;i<6-seqStr.length();i++){
            sb.append(0);
        }
        sb.append(seqStr);


        //最后2位为分库分表位,暂时写死
        sb.append("00");

        return sb.toString();
    }
    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if (orderModel==null) return null;
        OrderDO orderDO=new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        return orderDO;
    }



}
