package com.oxo.ball.bean.dto.resp.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportFormTeamResponse {
    private String date;
    private Long bet;
    private Long bingo;
    private Long activity;
    private Long recharge;
    private Long withdrawal;
    private String superPlayer;
    @JsonIgnore
    private Set<Long> betCountSet;
    public Integer getBetCount(){
        return betCountSet.size();
    }
    public Long getWinLose(){
        return bingo - bet;
    }
}
