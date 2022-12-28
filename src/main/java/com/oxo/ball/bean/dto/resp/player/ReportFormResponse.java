package com.oxo.ball.bean.dto.resp.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportFormResponse {
    private String date;
    private Long recharge;
    private Long withdrawal;
    private Long bet;
    private Long bingo;
    private Long rebate;
    private Long activity;
}
