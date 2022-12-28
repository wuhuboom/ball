package com.oxo.ball.bean.dto.api.web;

import lombok.Data;

import java.util.Map;

@Data
public class WebPayResponse {
    private Integer code;//	状态码	integer(int32)
    private Map<String,String> data;//	返回数据
//  payUrl	支付地址	string
    private String desc;//	状态描述	string
    private String msg;//	状态信息	string
    private Boolean success;//	请求状态	boolean
}
