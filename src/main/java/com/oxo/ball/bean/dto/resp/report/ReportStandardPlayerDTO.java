package com.oxo.ball.bean.dto.resp.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStandardPlayerDTO {
    private Long playerId;
    private String playerName;
    private Long recharge;
    private Set<String> daySet;
    private Long bets;
    private String superTree;
}
