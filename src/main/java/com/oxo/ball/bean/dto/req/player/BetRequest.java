package com.oxo.ball.bean.dto.req.player;

import io.swagger.models.auth.In;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.*;

@Data
public class BetRequest {
    @NotNull(message = "gameIdIsNull")
    private Long gameId;
    @NotNull(message = "oddsIdIsNull")
    private Long oddsId;
    @NotNull(message = "moneyIsNull")
    @Min(value = 1, message = "moneyMustThanZero")
    @Pattern(regexp = "\\d+(\\.\\d+)?",message = "moneyNotInteger")
    private String money;
    /**
     * 1正波2反波
     */
    @NotNull(message = "typeIsNull")
    @Range(min = 1,max = 2,message = "typeError")
    private Integer type;


}
