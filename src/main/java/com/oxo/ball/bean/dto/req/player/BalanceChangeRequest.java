package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class BalanceChangeRequest {
    /**
     * TODO 增加账变
     * @see com.oxo.ball.bean.dao.BallBalanceChange#balanceChangeType
     */
    @Min(value = 1,message = "typeError")
    @Max(value = 28,message = "typeError")
    private Integer type;

    /**
     * 1收入2支出
     */
    @Min(value = 1,message = "typebError")
    @Max(value = 2,message = "typebError")
    private Integer typeb;

    /**
     * 1今日2昨日3近7日
     */
    @Min(value = 1,message = "timeError")
    @Max(value = 3,message = "timeError")
    private Integer time;
}
