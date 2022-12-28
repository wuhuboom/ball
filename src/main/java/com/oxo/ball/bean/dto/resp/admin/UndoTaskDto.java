package com.oxo.ball.bean.dto.resp.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UndoTaskDto {
    private String name;
    private Integer count;
    /**
     * 1.充值优惠待办
     */
    private Integer type;
}
