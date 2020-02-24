package com.taobao.controller;

import com.taobao.controller.viewobject.ItemVO;
import com.taobao.error.BusinessException;
import com.taobao.response.CommonReturnType;
import com.taobao.service.ItemService;
import com.taobao.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.plugin.com.event.COMEventHandler;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController{

    @Autowired
    private ItemService itemService;

    //创建商品的Controller
    @RequestMapping(value = "/create",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam("title")String title,
                                       @RequestParam("description")String description,
                                       @RequestParam("price") BigDecimal price,
                                       @RequestParam("stock") Integer stock,
                                       @RequestParam("imgUrl") String imgUrl) throws BusinessException {
        //封装service请求用来创建商品
        ItemModel itemModel=new ItemModel();
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setTitle(title);
        itemModel.setPrice(price);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = this.convertItemVOFromModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }
    private ItemVO convertItemVOFromModel(ItemModel itemModel){
        if (itemModel==null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        return itemVO;
    }
    //商品详情页浏览
    @RequestMapping(value = "/get",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam("id")Integer id){
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO=convertItemVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    //商品列表浏览
    @RequestMapping(value = "/list",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList=itemService.listItem();

        //使用Stream API将list内的itemModel转换为itemVO
        List<ItemVO> itemVOList =itemModelList.stream().map(itemModel->{
                ItemVO itemVO=this.convertItemVOFromModel(itemModel);
                return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

}
