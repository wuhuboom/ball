package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class PlayerChangePwdRequest {
    @NotBlank(message = "originIsEmpty")
    private String origin;
    @Size(min = 6, max = 16, message = "passwordSizeError")
    @NotBlank(message = "newpwdIsEmpty")
    private String newPwd;
    @NotBlank(message = "confirmedIsEmpty")
    @Size(min = 6, max = 16, message = "passwordSizeError")
    private String twicePwd;
}
