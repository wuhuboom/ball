package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallIpWhite;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 * 系统公告 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallIpWhiteService extends IService<BallIpWhite> {
    SearchResponse<BallIpWhite> search(BallIpWhite query, Integer pageNo, Integer pageSize);
    BallIpWhite insert(BallIpWhite ipWhite);
    Boolean delete(Long id);
    Boolean edit(BallIpWhite ipWhite);
    Boolean status(BallIpWhite ipWhite);
    List<BallIpWhite> findAll();
}
