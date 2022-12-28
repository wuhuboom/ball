package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallGame;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallGameService;
import com.oxo.ball.service.player.IPlayerGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 游戏赛事 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/game/finish")
public class BallGameFinishController {
    @Resource
    IBallGameService gameService;
    @Autowired
    private IPlayerGameService playerGameService;
    @Autowired
    BallAdminService adminService;

    @PostMapping()
    public Object index(BallGame query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallGame> search = gameService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PostMapping("recount")
    public Object recount(@RequestBody BallGame ballGame, HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        BaseResponse res = gameService.recount(ballGame,currentUser);
        return res;
    }
    @PostMapping("rollback")
    public Object rollback(@RequestBody BallGame ballGame,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        BaseResponse res = gameService.rollback(ballGame,currentUser);
        return res;
    }
    @PostMapping("info")
    public Object info(@RequestBody BallGame ballGame){
        BallGame res = playerGameService.findById(ballGame.getId());
        return BaseResponse.successWithData(res);
    }
}
