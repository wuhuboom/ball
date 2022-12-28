package com.oxo.ball.bean.dto.req.report;

import lombok.Data;

@Data
public class ReportBalanceChangeRequest {
    private Integer time;
    private Integer accountType;
    private String begin;
    private String end;
    private Long userId;
    private String username;
    private Integer treeType;
    private Integer type;
    private String orderNo;
    private Long pageNo;
    private Long pageSize;

}
