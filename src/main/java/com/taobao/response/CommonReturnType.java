package com.taobao.response;

public class CommonReturnType {
    //表明对应请求返回结果success/fail
    private String status;
    //若status=success,则data内返回前段需要的json数据
    //若status=fail，则data内使用通用的错误码格式
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    //定义一个通用的创建方法
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    private static CommonReturnType create(Object result, String status) {
        CommonReturnType type=new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
