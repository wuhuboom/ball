package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author flooming
 */
@Data
public class PlayerAuthLoginNewDevicesRequest implements Serializable {
    private static final long serialVersionUID = -4842392920233760571L;

    @NotEmpty(message = "usernameIsEmpty")
    private String username;
    @NotEmpty(message = "passwordIsEmpty")
    private String password;
    @NotEmpty(message = "codeIsEmpty")
    private String code;
    @NotEmpty(message = "verifyKeyIsEmpty")
    private String verifyKey;
    @NotEmpty(message = "smsCodeIsEmpty")
    private String phoneCode;
}
