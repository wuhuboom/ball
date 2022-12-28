package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 玩家-系统公告
 * * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_player_notice")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BallPlayerNotice{

    private static final long serialVersionUID = 1L;

    private Long playerId;
    private Long noticeId;
    /**
     * 0未读 1已读
     */
    private Integer status;


}
