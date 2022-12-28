package com.oxo.ball.bean.dto.req.report;

import lombok.Data;

@Data
public class ReportStandardRequest {
    private String playerName;
    private Long recharge;
    private Integer days;
    private Long bets;
    private String begin;
    private String end;
    private long begins;
    private long ends;
    private String regx;
    private Integer time;
    private Integer treeType;

    @Override
    public String toString() {
        return "ReportStandardRequest{" +
                "playerName='" + playerName + '\'' +
                ", recharge=" + recharge +
                ", bets=" + bets +
                ", time=" + time +
                ", treeType=" + treeType +
                '}';
    }
}
