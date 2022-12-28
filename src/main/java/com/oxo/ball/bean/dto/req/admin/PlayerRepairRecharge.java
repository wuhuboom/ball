package com.oxo.ball.bean.dto.req.admin;

import lombok.Data;

@Data
public class PlayerRepairRecharge {
    private Long playerId;
    private Long payId;
    private String money;
    private String remark;

}
