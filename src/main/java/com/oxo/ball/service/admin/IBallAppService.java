package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallApp;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
public interface IBallAppService extends IService<BallApp> {
    SearchResponse<BallApp> search(BallApp query, Integer pageNo, Integer pageSize);
    BallApp findById(Long id);
    List<BallApp> findByAll();
    BallApp insert(BallApp ballApp);
    Boolean delete(Long id);
    Boolean edit(BallApp ballApp);
    Boolean status(BallApp ballApp);

}
