package com.oxo.ball.controller.player;

import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.player.BalanceChangeRequest;
import com.oxo.ball.bean.dto.req.player.PlayerBetRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.player.AuthPlayerService;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * <p>
 * 玩家账号 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/player/home")
@Api(tags = "玩家 - 首页")
public class PlayerHomeController {

    @Resource
    IPlayerService playerService;
    @Resource
    IBasePlayerService basePlayerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Resource
    IPlayerBetService betService;
    @Resource
    IBallSlideshowService slideshowService;
    @Resource
    IBallSystemNoticeService noticeService;
    @Resource
    IBallGameService gameService;
    @Resource
    IBallAnnouncementService announcementService;
    @Autowired
    IBallAppService ballAppService;

    @ApiOperation(
            value = "轮播图",
            notes = "轮播图" ,
            httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "lang",value = "en/zh...",required = true),
    })
    @GetMapping("slider")
    public Object slider(String lang,HttpServletRequest request) {
        SearchResponse<BallSlideshow> search = slideshowService.search(BallSlideshow.builder()
                .status(1)
                .language(lang)
                .build(), 1, 20);
        List<BallSlideshow> results = search.getResults();
        if(results==null||results.isEmpty()){
            search = slideshowService.search(BallSlideshow.builder()
                    .status(1)
                    .language("en")
                    .build(), 1, 20);
            results = search.getResults();
        }
        return BaseResponse.successWithData(results);
    }

    @ApiOperation(
            value = "滚动广告",
            notes = "滚动广告" ,
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("swiper")
    public Object swiper(String lang,HttpServletRequest request) throws TokenInvalidedException {
        SearchResponse<BallAnnouncement> search = announcementService.search(BallAnnouncement.builder()
                .status(1)
                .language(lang)
                .build(),1, 20);
        List<BallAnnouncement> results = search.getResults();
        if(results==null||results.isEmpty()){
            search = announcementService.search(BallAnnouncement.builder()
                    .status(1)
                    .language("en")
                    .build(),1, 20);
            results = search.getResults();
        }
        return BaseResponse.successWithData(results);
    }
    @ApiOperation(
            value = "系统公告",
            notes = "系统公告" ,
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("notice")
    public Object notice(String lang,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        SearchResponse<BallSystemNotice> search = noticeService.searchApp(BallSystemNotice.builder()
                .status(1)
                .language(lang)
                .playerId(currentUser.getId())
                .build(),1, 20);
        List<BallSystemNotice> results = search.getResults();
        if(results==null||results.isEmpty()){
            search = noticeService.searchApp(BallSystemNotice.builder()
                    .status(1)
                    .language("en")
                    .playerId(currentUser.getId())
                    .build(),1, 20);
            results = search.getResults();
        }
        return BaseResponse.successWithData(results);
    }
    @ApiOperation(
            value = "系统公告-已读",
            notes = "系统公告-已读" ,
            httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "noticeId",value = "公告ID",required = true),
    })
    @GetMapping("notice/read")
    public Object notice(Long noticeId,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse response = noticeService.setRead(noticeId,currentUser);
        return response;
    }

    @ApiOperation(
            value = "热门赛事",
            notes = "热门赛事" ,
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("hot")
    public Object bets(HttpServletRequest request) throws TokenInvalidedException {
        SearchResponse<BallGame> search = gameService.search(BallGame.builder()
                .hot(1)
                .gameStatus(1)
                .betCount(-999L)
                .build(), 1, 99);
        return BaseResponse.successWithData(search.getResults());
    }
    @ApiOperation(
            value = "客服服务",
            notes = "客服服务" ,
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("serv")
    public Object services(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse response = playerService.getPlayerServices(currentUser);
        return response;
    }
    @ApiOperation(
            value = "客服服务-临时",
            notes = "客服服务-临时" ,
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("serv_tmp")
    public Object servicesTemp() {
        BaseResponse response = playerService.getPlayerServices();
        return response;
    }

    @ApiOperation(
            value = "APP下载地址",
            notes = "APP下载地址,appType: 0.安卓，1.苹果" ,
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("app_url")
    public Object appUrl() {
        List<BallApp> response = ballAppService.findByAll();
        return BaseResponse.successWithData(response);
    }


}
