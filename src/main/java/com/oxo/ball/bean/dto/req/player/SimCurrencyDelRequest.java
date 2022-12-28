package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SimCurrencyDelRequest {
    /**
     * id
     */
    @NotEmpty(message = "simCurrencyNotEmpty")
    private Long id;

}
