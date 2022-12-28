package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallConfigWfail;
import com.oxo.ball.bean.dao.BallConfigWfail;
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
public interface IBallConfigWfailService extends IService<BallConfigWfail> {
    SearchResponse<BallConfigWfail> search(BallConfigWfail query, Integer pageNo, Integer pageSize);
    BallConfigWfail findById(Long id);
    List<BallConfigWfail> findByAll();
    BallConfigWfail insert(BallConfigWfail configWfail);
    Boolean delete(Long id);
    Boolean edit(BallConfigWfail configWfail);
    Boolean status(BallConfigWfail configWfail);

}
