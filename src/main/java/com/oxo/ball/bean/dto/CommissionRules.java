package com.oxo.ball.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionRules {
    private Integer level;
    private String commission;
    private String levelStr;
}
