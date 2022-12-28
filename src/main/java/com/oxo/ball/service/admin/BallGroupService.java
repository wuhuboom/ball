package com.oxo.ball.service.admin;


import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallGroup;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallGroupMapper;

import java.util.List;

/**
 * @author flooming
 */
public interface BallGroupService extends IService<BallGroup> {
    BallGroup findById(Long id);
    SearchResponse<BallGroup> search(BallGroup keyword, Integer pageNo, Integer pageSize);
    BallGroup insert(BallGroup BallGroup);
    Boolean delete(Long id);

    Boolean edit(BallGroup editBallGroup);

    List<BallGroup> findAll();
}
