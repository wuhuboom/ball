package com.oxo.ball.bean.dto.req.admin;

import lombok.Data;

@Data
public class PlayerRepairWithdrawal {
    private Long playerId;
    //提现方式
    private Long wiId;
    //如果是usdt,选择usdt
    private Long usdtId;
    //代付ID
    private Long behalfId;
    private String money;
    private String remark;
}
