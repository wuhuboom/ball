package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class WithdrawalLogRequest {
    /**
     * 1今日 2昨日 3.7日 4.10日 5.30日"),
     */
    @Min(value = 1,message = "timeError")
    @Max(value = 5,message = "timeError")
    private Integer time;

    /**
     *  1待审核'
     * 2已审核'
     * 3失败'
     * 4提现成功'
     * 5代付中'
     */
    @Min(value = 1,message = "statusError")
    @Max(value = 5,message = "statusError")
    private Integer status;


}
