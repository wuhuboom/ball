package com.oxo.ball.controller.admin.todo;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerBack;
import com.oxo.ball.bean.dao.BallLoggerBet;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallLoggerBackService;
import com.oxo.ball.service.admin.IBallLoggerBetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 日志表 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/todo/bet")
public class BallLoggerRebateController {
    @Resource
    IBallLoggerBackService loggerBackService;
    @Autowired
    private BallAdminService adminService;

    @PostMapping
    public Object index(@RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        SearchResponse<BallLoggerBack> search = loggerBackService.searchUnSettlement(null, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }
    @DeleteMapping
    public Object indexCount(@RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        SearchResponse<BallLoggerBack> search = loggerBackService.searchUnSettlement(null, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object indexGet(BallLoggerBack query,@RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                           HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        SearchResponse<BallLoggerBack> search = loggerBackService.searchUnSettlementByPlayer(query, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }

    @PutMapping()
    public Object doSettlement(Long gameId){
        BaseResponse response = loggerBackService.settlementOnWeek(gameId);
        return response;
    }
}
