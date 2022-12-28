package com.oxo.ball.bean.dto.req.report;

import lombok.Data;

@Data
public class ReportOperateRequest {
    private Integer pageNo;
    private Integer pageSize;
    private Integer time;
    private String begin;
    private String end;
    private Long playerId;
    private String playerName;
    private Integer type;
    private Long userId;
    private String username;
    private Integer treeType;

}
