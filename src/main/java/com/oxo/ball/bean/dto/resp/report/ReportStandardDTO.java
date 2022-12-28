package com.oxo.ball.bean.dto.resp.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStandardDTO {
    private Long playerId;
    private String playerName;
    //达标人数
    private int aimCount;
    //团队人数
    private int groupCount;
    //充值达标
    private int aimRecharge;
    //下注达标
    private int aimBet;
    //顶级
    private String topUser;
    // 上级
    private String parentUser;

}
