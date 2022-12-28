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
@RequestMapping("/ball/todo/bonus")
public class BallBonusController {
    @Resource
    IBallTodoService todoService;
    @Autowired
    BallAdminService adminService;
    @PostMapping
    public Object index(BallTodo query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        SearchResponse<BallTodo> search = todoService.search(query, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }
    @DeleteMapping
    public Object indexCount(BallTodo query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        query.setStatus(0);
        SearchResponse<BallTodo> search = todoService.search(query, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }
    @PutMapping()
    public Object editSave(@RequestBody BallTodo todo,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        BaseResponse response = todoService.edit(todo,currentUser);
        return response;
    }
//    @GetMapping("del")
//    public Object del(@RequestParam("id") Long id){
//        Boolean delete = todoService.delete(id);
//        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
//    }
    @GetMapping
    public Object info(@RequestParam("id") Long id){
        BallTodo info = todoService.info(id);
        return BaseResponse.successWithData(info);
    }
}
