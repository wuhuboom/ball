package com.oxo.ball.bean.dto.req.report;

import lombok.Data;

@Data
public class ReportRewiRequest {
    private Integer time;
    private String begin;
    private String end;
    private Long payId;
    private Long behalfId;
}
