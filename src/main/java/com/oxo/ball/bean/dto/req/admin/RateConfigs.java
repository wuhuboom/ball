package com.oxo.ball.bean.dto.req.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RateConfigs implements Serializable {
    private String name;//货币名称
    private String areaCode;//手机区号
    private String rate;//汇率
    private String symbol;//货币符号
}
