package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public class PlayerBindPhoneCodeRequest {
    @NotEmpty(message = "phoneIsEmpty")
    private String phone;
}
