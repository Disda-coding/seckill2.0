package com.taobao.service.model;

import java.math.BigDecimal;

public class OrderModel {
    //下单流水号，用String原因是要记录更多信息如下单时间等等
    private String id;

    private Integer userId;

    private Integer itemId;

    //若非空，则表示以秒杀商品方式下单
    private Integer promoId;

    //购买商品的单价：当时的价格，若promoId非空则表示秒杀商品价格
    private BigDecimal itemPrice;

    private Integer amount;

    //购买金额 若promoId非空则表示秒杀商品价格
    private BigDecimal orderPrice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }
}
