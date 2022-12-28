package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallLoggerLogin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallLoggerService extends IService<BallLoggerLogin> {
    SearchResponse<BallLoggerLogin> search(BallLoggerLogin queryParam, Integer pageNo, Integer pageSize);
    List<BallLoggerLogin> search(BallLoggerLogin queryParam);
    BallLoggerLogin insert(BallLoggerLogin announcement);
    BallLoggerLogin findPlayerLastLogin(BallPlayer ballPlayer);

    BallLoggerLogin search(String username, String ipAddr);
}
