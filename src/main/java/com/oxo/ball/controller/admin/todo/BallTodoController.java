package com.oxo.ball.controller.admin.todo;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallTodo;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.UndoTaskDto;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 奖金策略 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/todo_list")
public class BallTodoController {
    @Resource
    IBallTodoService todoService;
    @Autowired
    BallAdminService adminService;
    @PutMapping
    public Object indexList(){
        List<UndoTaskDto> list =  todoService.currentUnDo();
        return BaseResponse.successWithData(list);
    }

    @GetMapping
    public Object index(){
        BaseResponse response = todoService.unCheck();
        return response;
    }
}
