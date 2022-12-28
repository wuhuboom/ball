package com.oxo.ball.bean.dto.resp.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProxyStatisDto {
    private Long userId;
    private String username;
    private Integer subCount;
    private Integer subCountAll;
    private Integer sameIp;
    //返利
    private Long subRebate;
    //充值次数
//    private Integer rechargeCount;
    //充值金额
    private Long rechargeMoney;
    //首充人数
    private Integer rechargeFirst;
    //提现次数
//    private Integer withdrawalCount;
    //提现金额
    private Long withdrawalMoney;
    //下注次数
    private Integer betCount;
//    //下注人数
//    private Long betPlayer;
    //总余额
    private Long totalMoney;
    //下注人数set
    private Set<Long> betPlayerSet;
    //充值人数set
    private Set<Long> rechargePlayerSet;
    //提现人数set
    private Set<Long> withdrawalPlayerSet;

    //下级特有字段
    private Integer level;
    //数据库的层级
    private Integer vipRank;

    public Integer getBetPlayer(){
        if(betPlayerSet==null){
            return 0;
        }
        return betPlayerSet.size();
    }

    private Set<String> subIp;

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
