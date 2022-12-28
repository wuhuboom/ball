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
public class ProxyStatis2Dto {
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
    //下注人数set
    private Set<Long> betPlayerSet;
    //充值人数set
    private Set<Long> rechargePlayerSet;
    //提现人数set
    private Set<Long> withdrawalPlayerSet;
    //累计充值
    private Long rechargeTotal;
    //累计提现
    private Long withdrawalTotal;
    //昨日下注人数set
    private Set<Long> betPlayerYestodaySet;
    public Integer getBetPlayer(){
        if(betPlayerSet==null){
            return 0;
        }
        return betPlayerSet.size();
    }
    public Integer getBetPlayerYestoday(){
        if(betPlayerYestodaySet==null){
            return 0;
        }
        return betPlayerYestodaySet.size();
    }
    public Integer getRechargeCount(){
        if(rechargePlayerSet==null){
            return 0;
        }
        return rechargePlayerSet.size();
    }
    public Integer getWithdrawalCount(){
        if(withdrawalPlayerSet==null){
            return 0;
        }
        return withdrawalPlayerSet.size();
    }
}
