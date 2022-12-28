package com.oxo.ball.bean.dto.resp.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProxyStatis3Dto {
    private String ymd;
    private String username;
    private Integer subCount;
    private Integer subCountAll;
    //首充人数
    private Integer rechargeFirst;
    //充值金额
    private Long rechargeMoney;
    //提现金额
    private Long withdrawalMoney;
    //下注次数
    private Integer betCount;
    private Integer betPlayer;
    private Integer rechargeCount;
    private Integer withdrawalCount;
    //累计充值
    private Long rechargeTotal;
    //累计充值
    private Long withdrawalTotal;
    //
    private Integer betPlayerYestoday;
}
