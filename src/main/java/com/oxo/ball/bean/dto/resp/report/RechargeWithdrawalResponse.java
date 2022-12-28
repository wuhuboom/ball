package com.oxo.ball.bean.dto.resp.report;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RechargeWithdrawalResponse {
    private String ymd;
    private String payName;

    private Long resuccMoney;
    private Integer resuccCount;
    @JsonIgnore
    private Set<Long> resuccPlayer;
//    private Long refailMoney;
//    private Integer refailCount;
//    @JsonIgnore
//    private Set<Long> refailPlayer;
//    private String resuccPer;

    private Long wisuccMoney;
    private Integer wisuccCount;
    @JsonIgnore
    private Set<Long> wisuccPlayer;
//    private Long wifailMoney;
//    private Integer wifailCount;
//    @JsonIgnore
//    private Set<Long> wifailPlayer;
//    private String wisuccPer;

    public Integer getResuccPlayers(){
        return resuccPlayer.size();
    }
//    public Integer getRefailPlayers(){
//        return refailPlayer.size();
//    }
    public Integer getWisuccPlayers(){
        return wisuccPlayer.size();
    }
//    public Integer getWifailPlayers(){
//        return wifailPlayer.size();
//    }


}
