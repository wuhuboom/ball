package com.oxo.ball.bean.dto;

import com.oxo.ball.bean.dao.BallDepositPolicy;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class RechargeHanderDTO {
    private List<RechargeRebateDto> discountQuota;
    private BallPlayer edit;
    private boolean first;
    private boolean second;
    private Long totalDiscount;
}
