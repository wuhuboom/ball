package com.oxo.ball.bean.dto.req.report;

import lombok.Data;

@Data
public class ReportGameRequest {
    private Integer pageNo;
    private Integer pageSize;
    private Integer time;
    private String begin;
    private String end;
    private Integer gameId;
}
