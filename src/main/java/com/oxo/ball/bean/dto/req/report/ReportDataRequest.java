package com.oxo.ball.bean.dto.req.report;

import lombok.Data;

@Data
public class ReportDataRequest {
    private Integer time;
    private String begin;
    private String end;

    private Long userId;
    private String username;
}
