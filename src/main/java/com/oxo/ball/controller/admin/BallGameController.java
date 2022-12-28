package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallGame;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallGameService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.player.IPlayerGameService;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

/**
 * <p>
 * 游戏赛事 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/game")
public class BallGameController {
    @Resource
    IBallGameService gameService;
    @Resource
    IPlayerGameService playerGameService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    private BallAdminService ballAdminService;

    @PostMapping
    public Object index(BallGame query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallGame> search = gameService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object indexGetOnbet(BallGame query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallGame> search = gameService.searchOnBet(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PutMapping
    public Object getTgNotice(){
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        return BaseResponse.successWithData(systemConfig.getCloseNotice());
    }
    @DeleteMapping
    public Object setTgNotice(Integer status){
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        systemConfigService.edit(BallSystemConfig.builder()
                .id(systemConfig.getId())
                .closeNotice(status)
                .build());
        return BaseResponse.SUCCESS;
    }
    @PostMapping("info")
    public Object info(@RequestBody BallGame ballGame){
        BallGame res = playerGameService.findById(ballGame.getId());
        return BaseResponse.successWithData(res);
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallGame ballGame){
        BallGame edit = BallGame.builder()
                .id(ballGame.getId())
                .homeHalf(ballGame.getHomeHalf())
                .guestHalf(ballGame.getGuestHalf())
                .homeFull(ballGame.getHomeFull())
                .guestFull(ballGame.getGuestFull())
                .homeOvertime(ballGame.getHomeOvertime())
                .guestOvertime(ballGame.getGuestOvertime())
                .homePenalty(ballGame.getHomePenalty())
                .guestPenalty(ballGame.getGuestPenalty())
                .build();
        if(!StringUtils.isBlank(ballGame.getFinishTimeStr2())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(ballGame.getFinishTimeStr2(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                edit.setFinishTime(timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        BaseResponse res = gameService.edit(edit);
        return res;
    }
    @PutMapping("edit")
    public Object editMinMax(@RequestBody BallGame ballGame){
        BaseResponse res = gameService.edit(BallGame.builder()
                .id(ballGame.getId())
                .minBet(ballGame.getMinBet())
                .maxBet(ballGame.getMaxBet())
                .totalBet(ballGame.getTotalBet())
                .build());
        return res;
    }
    @PostMapping("status")
    public Object status(@RequestBody BallGame ballGame){
        Boolean aBoolean = gameService.editStatus(ballGame);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("top")
    public Object top(@RequestBody BallGame ballGame){
        Boolean aBoolean = gameService.editStatusTop(ballGame);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("hot")
    public Object hot(@RequestBody BallGame ballGame){
        Boolean aBoolean = gameService.editStatusHot(ballGame);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("even")
    public Object even(@RequestBody BallGame ballGame){
        Boolean aBoolean = gameService.editStatusEven(ballGame);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("hand_open")
    public Object handOpen(@RequestBody BallGame ballGame){
        BaseResponse res = gameService.handOpen(ballGame);
        return  res;
    }
    @PostMapping("cancel")
    public Object cancel(@RequestBody BallGame ballGame, HttpServletRequest request){
        BallAdmin admin = ballAdminService.getCurrentUser(request.getHeader("token"));
        BaseResponse res = gameService.cancel(ballGame,admin);
        return res;
    }
    @PostMapping("add")
    public Object add(@RequestBody BallGame ballGame){
        BaseResponse response = gameService.add(ballGame);
        return response;
    }
    @PostMapping("add/upload")
    public Object addUpload(String key,String url, MultipartFile file){
        Boolean aBoolean = gameService.uploadGameLogo(key,url,file);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
}
