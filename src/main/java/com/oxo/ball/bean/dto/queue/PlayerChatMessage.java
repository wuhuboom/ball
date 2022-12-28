package com.oxo.ball.bean.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerChatMessage implements Serializable {
    private Long sendTime;
}
