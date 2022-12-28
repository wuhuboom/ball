package com.oxo.ball.bean.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author flooming
 */
@Data
public class AuthLoginRequest implements Serializable {
    private static final long serialVersionUID = -4842392920233760571L;

    @NotBlank
    @JsonProperty("user")
    private String username;
    @NotBlank
    @JsonProperty("pwd")
    private String password;
    @NotBlank
    private String googleCode;
    @NotBlank
    private String googleKey;

}
