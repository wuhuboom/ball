package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerRebate;
import com.oxo.ball.bean.dao.BallTodo;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.UndoTaskDto;

import java.util.List;

/**
 * <p>
 * 反佣策略 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallTodoService extends IService<BallTodo> {
    SearchResponse<BallTodo> search(BallTodo query, Integer pageNo, Integer pageSize, BallAdmin currentUser);
    List<BallTodo> search(BallTodo queryParam, BallAdmin currentUser);
    BallTodo insert(BallTodo todo);
    Boolean delete(Long id);
    BaseResponse edit(BallTodo todo, BallAdmin currentUser);
    BallTodo info(Long id);

    BaseResponse unCheck();

    List<UndoTaskDto> currentUnDo();
}
