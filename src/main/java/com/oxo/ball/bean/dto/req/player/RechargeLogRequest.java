package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class RechargeLogRequest {
    /**
     * 1今日 2昨日 3.7日 4.10日 5.30日"),
     */
    @Min(value = 1,message = "timeError")
    @Max(value = 5,message = "timeError")
    private Integer time;
    /**
     *
     * 1.线下,2.线上"),
     */
    @Min(value = 1,message = "typeError")
    @Max(value = 2,message = "typeError")
    private Integer type;

    /**
     *   1待付款/2已到账/3已上分/4支付超时
     */
    @Min(value = 1,message = "statusError")
    @Max(value = 4,message = "statusError")
    private Integer status;


}
