package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;

import java.util.List;

public interface IBallPlayerActiveService {
    List<BallPlayer> queryActivePlayer(QueryActivePlayerRequest request);
    void sendMessageToPlayer(String message);
}
