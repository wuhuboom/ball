package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallBet;
import com.oxo.ball.bean.dao.BallLoggerWithdrawal;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallBetService;
import com.oxo.ball.utils.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 下注 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/bets")
public class BallBetController {

    @Resource
    IBallBetService betService;
    @Autowired
    private PoiUtil poiUtil;
    @Autowired
    private BallAdminService ballAdminService;

    @PostMapping
    public Object index(BallBet query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallBet> search = betService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }

    @PostMapping("undo")
    public Object undo(@RequestBody BallBet query, HttpServletRequest request){
        BallAdmin admin = ballAdminService.getCurrentUser(request.getHeader("token"));
        BaseResponse res = betService.undo(query,admin,false);
        return res;
    }
    @PutMapping("undo")
    public Object rollBack(@RequestBody BallBet query,HttpServletRequest request){
        BallAdmin admin = ballAdminService.getCurrentUser(request.getHeader("token"));
        BaseResponse res = betService.betRollbackByBetId(query.getId(),admin);
        return res;
    }
    @PostMapping("info")
    public Object info(@RequestBody BallBet query){
        BaseResponse res = betService.info(query);
        return res;
    }

    @PostMapping("export")
    public Object exportExcel(@RequestBody BallBet query){
        BaseResponse res = poiUtil.exportBet(query);
        return res;
    }
    @PostMapping("settle")
    public Object settle(@RequestBody BallBet query){
        BaseResponse res = betService.betOpen(query.getId());
        return res;
    }

    @PostMapping("repair")
    public Object indexGet(String playerName){
        BaseResponse search = betService.checkRebate(playerName);
        return search;
    }
    @GetMapping("repair")
    public Object indexGetQueue(){
        BaseResponse search = betService.checkRebateQueue();
        return search;
    }
}
