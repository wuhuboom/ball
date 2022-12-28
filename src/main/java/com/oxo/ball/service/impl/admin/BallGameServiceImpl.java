package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallBet;
import com.oxo.ball.bean.dao.BallGame;
import com.oxo.ball.bean.dao.BallGameLossPerCent;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportGameRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.mapper.BallGameMapper;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallBetService;
import com.oxo.ball.service.admin.IBallGameLossPerCentService;
import com.oxo.ball.service.admin.IBallGameService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.service.player.IPlayerGameService;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 游戏赛事 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallGameServiceImpl extends ServiceImpl<BallGameMapper, BallGame> implements IBallGameService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    IPlayerGameService playerGameService;
    @Autowired
    IBallBetService ballBetService;
    @Autowired
    BallAdminService adminService;
    @Value("${static.file}")
    private String staticFile;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IBallGameLossPerCentService lossPerCentService;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    BallGameMapper mapper;
    @Override
    public SearchResponse<BallGame> search(BallGame queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallGame> response = new SearchResponse<>();
        Page<BallGame> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallGame> query = new QueryWrapper<>();
        if(queryParam.getHot()!=null){
            query.eq("hot",queryParam.getHot());
        }
        if(queryParam.getEven()!=null){
            query.eq("even",queryParam.getEven());
        }
        if(queryParam.getTop()!=null){
            query.eq("top",queryParam.getTop());
        }
        if(queryParam.getGameStatus()!=null){
            query.eq("game_status",queryParam.getGameStatus());
        }
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(queryParam.getId()!=null){
            query.eq("id",queryParam.getId());
        }
        if(!StringUtils.isBlank(queryParam.getAllianceName())){
            query.eq("alliance_name",queryParam.getAllianceName());
        }
        if(!StringUtils.isBlank(queryParam.getMainName())){
            query.eq("main_name",queryParam.getMainName());
        }
        if(!StringUtils.isBlank(queryParam.getGuestName())){
            query.eq("guest_name",queryParam.getGuestName());
        }
        if(queryParam.getSettlementType()!=null){
            query.eq("settlement_type",queryParam.getSettlementType());
        }
        String timeQuery = "start_time";
        if(queryParam.getGameStatus()!=null){
            if(queryParam.getGameStatus()==3){
                timeQuery = "settlement_time";
            }
        }
        if (queryParam.getTimeType() != null) {
            switch (queryParam.getTimeType()) {
                case 0:
                    query.ge(timeQuery, TimeUtil.getDayBegin().getTime()-2*TimeUtil.TIME_ONE_DAY);
                    query.le(timeQuery, TimeUtil.getDayEnd().getTime()-2*TimeUtil.TIME_ONE_DAY);
                    break;
                case 1:
                    query.ge(timeQuery, TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le(timeQuery, TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 2:
                    query.ge(timeQuery, TimeUtil.getDayBegin().getTime());
                    query.le(timeQuery, TimeUtil.getDayEnd().getTime());
                    break;
                case 3:
                    query.ge(timeQuery, TimeUtil.getBeginDayOfTomorrow().getTime());
                    query.le(timeQuery, TimeUtil.getEndDayOfTomorrow().getTime());
                    break;
                case 4:
                    query.ge(timeQuery, TimeUtil.getBeginDayOfTomorrow().getTime()+TimeUtil.TIME_ONE_DAY);
                    query.le(timeQuery, TimeUtil.getEndDayOfTomorrow().getTime()+TimeUtil.TIME_ONE_DAY);
                    break;
                case 5:
                    query.ge(timeQuery, TimeUtil.getDayBegin().getTime());
                    query.le(timeQuery, TimeUtil.getDayEnd().getTime()+3*TimeUtil.TIME_ONE_DAY);
                    break;
                case 6:
                    if(!StringUtils.isBlank(queryParam.getBegin())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.ge("settlement_time", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                                query.ge("settlement_time", timeStamp);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    if(!StringUtils.isBlank(queryParam.getEnd())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.le("settlement_time", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                                query.le("settlement_time", timeStamp+TimeUtil.TIME_ONE_DAY);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        //                .settlementTime(System.currentTimeMillis())
        if(queryParam.getBetCount()!=null&&queryParam.getBetCount()==-999){
            query.orderByDesc("start_time");
        }else{
            query.orderByDesc("settlement_time");
        }
        IPage<BallGame> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallGame> searchOnBet(BallGame queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallGame> response = new SearchResponse<>();
        Page<BallGame> page = new Page<>(pageNo, pageSize);
        Map<String,Object> query = new HashMap<>();
        if(queryParam.getGameStatus()!=null){
            query.put("game_status",queryParam.getGameStatus());
        }
        if(queryParam.getId()!=null){
            query.put("id",queryParam.getId());
        }
        IPage<BallGame> pages = mapper.page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public Boolean insert(BallGame ballGame) {
        try {
            return save(ballGame);
        }catch (Exception e){}finally {
            redisUtil.delKeys("ball_player_game_search*");
        }
        return false;
    }

    @Override
    @CacheEvict(value = "ball_player_game_by_id", key = "#ballGame.getId()")
    public BaseResponse edit(BallGame ballGame) {
        boolean b = updateById(ballGame);
        redisUtil.delKeys("ball_player_game_search*");
        return b?BaseResponse.successWithMsg("ok"):BaseResponse.failedWithMsg("edit failed");
    }

    @Override
    @CacheEvict(value = "ball_player_game_by_id", key = "#id")
    public Boolean deleteById(Long id) {
        return removeById(id);
    }

    @Override
    public void whenGameStart() {
        //时间到的设置为正常进行
        UpdateWrapper update = new UpdateWrapper();
        update.lt("start_time",TimeUtil.getNowTimeMill());
        update.eq("game_status",1);
        BallGame edit = BallGame.builder()
                .gameStatus(2)
                .build();
        update(edit,update);
    }

    @Override
    public void autoSetHot() {
        int i = baseMapper.autoSetHot();
        if(i>0){
            redisUtil.delKeys("ball_player_game_search*");
        }
    }

    @Override
    @CacheEvict(value = "ball_player_game_by_id", key = "#ballGame.getId()")
    public Boolean editStatus(BallGame ballGame) {
        BallGame edit = BallGame.builder()
                .status(ballGame.getStatus()==1?2:1)
                .build();
        edit.setId(ballGame.getId());
        edit.setUpdatedAt(TimeUtil.getNowTimeMill());
        boolean b = updateById(edit);
        redisUtil.delKeys("ball_player_game_search*");
        return b;
    }

    @Override
    @CacheEvict(value = "ball_player_game_by_id", key = "#ballGame.getId()")
    public Boolean editStatusTop(BallGame ballGame) {
        BallGame edit = BallGame.builder()
                .top(ballGame.getTop()==1?2:1)
                .build();
        edit.setId(ballGame.getId());
        edit.setUpdatedAt(TimeUtil.getNowTimeMill());
        boolean b = updateById(edit);
        redisUtil.delKeys("ball_player_game_search*");
        return b;
    }

    @Override
    @CacheEvict(value = "ball_player_game_by_id", key = "#ballGame.getId()")
    public Boolean editStatusHot(BallGame ballGame) {
        BallGame edit = BallGame.builder()
                .hot(ballGame.getHot()==1?2:1)
                .build();
        edit.setId(ballGame.getId());
        edit.setUpdatedAt(TimeUtil.getNowTimeMill());
        boolean b = updateById(edit);
        redisUtil.delKeys("ball_player_game_search*");
        return b;
    }

    @Override
    @CacheEvict(value = "ball_player_game_by_id", key = "#ballGame.getId()")
    public Boolean editStatusEven(BallGame ballGame) {
        BallGame edit = BallGame.builder()
                .even(ballGame.getEven()==1?2:1)
                .build();
        edit.setId(ballGame.getId());
        edit.setUpdatedAt(TimeUtil.getNowTimeMill());
        boolean b = updateById(edit);
        //TODO 赛事保本
        if(b){
            lossPerCentService.editStatusEvenByGameId(ballGame.getId(),edit.getEven());
            redisUtil.delKeys("ball_player_game_search*");
        }
        return b;
    }

    @PostMapping("info")
    public Object info(@RequestBody BallGame ballGame){
        BallGame res = playerGameService.findById(ballGame.getId());
        return BaseResponse.successWithData(res);
    }

    @Override
    public BaseResponse recount(BallGame ballGame, BallAdmin currentUser) {
        //修改比分
        edit(BallGame.builder()
                .id(ballGame.getId())
                .homeHalf(ballGame.getHomeHalf())
                .guestHalf(ballGame.getGuestHalf())
                .homeFull(ballGame.getHomeFull())
                .guestFull(ballGame.getGuestFull())
                .homeOvertime(ballGame.getHomeOvertime())
                .guestOvertime(ballGame.getGuestOvertime())
                .homePenalty(ballGame.getHomePenalty())
                .guestPenalty(ballGame.getGuestPenalty())
                .build());
        //再重新计算
        BaseResponse res = ballBetService.betRecount(ballGame.getId(),currentUser);
        return res;
    }

    @Override
    public BaseResponse rollback(BallGame ballGame, BallAdmin currentUser) {
        BaseResponse res = ballBetService.betRollback(ballGame.getId(),currentUser);
        return res;
    }

    @Override
    public List<BallGame> statisReport(ReportDataRequest reportDataRequest) {
        QueryWrapper gameQuery = new QueryWrapper();
        gameQuery.select("ymd,count(id) as id,count(case when game_status=1 then 1 end) game_status");
//        BallPlayerServiceImpl.queryByTime(gameQuery,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        gameQuery.ge("start_time", TimeUtil.getBeginDayOfYesterday().getTime());
        gameQuery.le("start_time", TimeUtil.getEndDayOfYesterday().getTime());
        gameQuery.groupBy("ymd");
        List list = list(gameQuery);
        return list;
    }

    @Override
    public SearchResponse<BallGame> statisReport(ReportGameRequest reportGameRequest) {
        SearchResponse<BallGame> response = new SearchResponse<>();
        Page<BallGame> page = new Page<>(reportGameRequest.getPageNo(),reportGameRequest.getPageSize());
        QueryWrapper<BallGame> query = new QueryWrapper<>();
        if(reportGameRequest.getGameId()!=null){
            query.eq("id",reportGameRequest.getGameId());
        }else{
            if (reportGameRequest.getTime() != null) {
                switch (reportGameRequest.getTime()) {
                    case 0:
                        query.ge("start_time", TimeUtil.getDayBegin().getTime());
                        query.le("start_time", TimeUtil.getDayEnd().getTime());
                        break;
                    case 1:
                        query.ge("start_time", TimeUtil.getBeginDayOfYesterday().getTime());
                        query.le("start_time", TimeUtil.getEndDayOfYesterday().getTime());
                        break;
                    case 2:
                        query.ge("start_time", TimeUtil.getDayBegin().getTime()-3*TimeUtil.TIME_ONE_DAY);
                        query.le("start_time", TimeUtil.getDayEnd().getTime()-3*TimeUtil.TIME_ONE_DAY);
                        break;
                    case 3:
                        query.ge("start_time", TimeUtil.getBeginDayOfWeek().getTime());
                        query.le("start_time", TimeUtil.getDayEnd().getTime());
                        break;
                    case 4:
                        query.ge("start_time", TimeUtil.getBeginDayOfLastWeek().getTime());
                        query.le("start_time", TimeUtil.getEndDayOfLastWeek().getTime());
                        break;
                    case 5:
                        query.ge("start_time", TimeUtil.getBeginDayOfMonth().getTime());
                        query.le("start_time", TimeUtil.getDayEnd().getTime());
                        break;
                    case 6:
                        query.ge("start_time", TimeUtil.getBeginDayOfLastMonth().getTime());
                        query.le("start_time", TimeUtil.getEndDayOfLastMonth().getTime());
                        break;
                    case 7:
                        if(!StringUtils.isBlank(reportGameRequest.getBegin())){
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(reportGameRequest.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                                query.ge("start_time", timeStamp);
                            } catch (ParseException e) {
                            }
                        }
                        if(!StringUtils.isBlank(reportGameRequest.getEnd())){
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(reportGameRequest.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                                query.le("start_time", timeStamp);
                            } catch (ParseException e) {
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        query.orderByDesc("start_time");
        IPage<BallGame> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public synchronized BaseResponse handOpen(BallGame ballGame) {
        //TODO 如果赛事未开赛,不能结算
        BallGame dbGame = playerGameService.findById(ballGame.getId());
        if(dbGame.getGameStatus()==3){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e25"));
        }
        BallGame edit = BallGame.builder()
                .id(ballGame.getId())
                .gameStatus(3)
                .settlementType(1)
                .settlementTime(System.currentTimeMillis())
                .build();
        ThreadPoolUtil.exec(() -> ballBetService.betOpen(edit,true));
        return BaseResponse.SUCCESS;
    }

    @Override
    public Boolean uploadGameLogo(String key, String url,MultipartFile file) {
        String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
        if(file!=null && !file.isEmpty()){
            String webpath = "gameLogo/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            //后缀
            String subfex = originalFilename.substring(originalFilename.lastIndexOf("."));
            String saveName = UUIDUtil.getUUID()+subfex;
            try {
                InputStream inputStream = file.getInputStream();
                String savePath = fileRootPath+saveName;
                FileOutputStream fos = new FileOutputStream(savePath);
                byte[] b = new byte[128];
                int len;
                while((len = inputStream.read(b))!=-1){
                    fos.write(b,0,len);
                }
                fos.flush();
                fos.close();
                inputStream.close();
                redisUtil.set(key,url+webpath+saveName,TimeUtil.TIME_ONE_MIN*10/1000);
                return true;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public BaseResponse add(BallGame ballGame) {
        String key = ballGame.getLogoKey();
        Object mainLogo = redisUtil.get("main" + key);
        Object homeLogo = redisUtil.get("home" + key);
        Object awayLogo = redisUtil.get("away" + key);
        Long startTime = 0L;
        try {
            startTime = TimeUtil.stringToTimeStamp(ballGame.getBegin(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
        } catch (ParseException e) {
        }
        if(ballGame.getOdds()==null||ballGame.getOdds().isEmpty()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e56"));
        }
        BallGame game = BallGame.builder()
                .id(createGameId())
                .status(1)
                .createdAt(TimeUtil.getNowTimeMill())
                .allianceLogo(mainLogo==null?"":mainLogo.toString())
                .allianceName(ballGame.getAllianceName())
                .mainLogo(homeLogo==null?"":homeLogo.toString())
                .mainName(ballGame.getMainName())
                .guestLogo(awayLogo==null?"":awayLogo.toString())
                .guestName(ballGame.getGuestName())
                .startTime(startTime)
                .ymd(TimeUtil.dateFormat(new Date(startTime),TimeUtil.TIME_YYYY_MM_DD))
                //手动添加
                .fromTo(1)
                .build();
        boolean res = insert(game);
        if(res){
            for(BallGameLossPerCent item:ballGame.getOdds()){
                item.setGameId(game.getId());
            }
            lossPerCentService.batchInsert(ballGame.getOdds());
        }
        redisUtil.delKeys("ball_player_game_search*");
        return res?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }

    @Override
    public synchronized BaseResponse cancel(BallGame ballGame, BallAdmin admin) {
        BallGame dbGame = playerGameService.findById(ballGame.getId());
        if(dbGame.getSettlementType()==4 || dbGame.getSettlementType()==5){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e26"));
        }
        edit(BallGame.builder()
                .id(ballGame.getId())
                .settlementType(4)
                .settlementTime(System.currentTimeMillis())
                .build());
        apiLog.info("撤消赛事:{}",dbGame);
        ThreadPoolUtil.exec(() -> {
            List<BallBet> bets = ballBetService.findByGameId(ballGame.getId(), 0);
            apiLog.info("撤消赛事:{},订单数:{}",dbGame,bets.size());
            for(BallBet item:bets){
                try {
                    ballBetService.undo(item,admin, true);
                }catch (Exception e){
                }
            }
            edit(BallGame.builder()
                    .id(ballGame.getId())
                    //撤消完成
                    .settlementType(5)
                    .settlementTime(System.currentTimeMillis())
                    .gameStatus(3)
                    .build());
        });
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e60"));
    }

    private Long createGameId(){
        QueryWrapper query = new QueryWrapper();
        query.select("max(id),id");
        BallGame ballGame = baseMapper.selectOne(query);
        if(ballGame==null){
            return 1L;
        }
        Long id = ballGame.getId()+1;
        while(true){
            BallGame game = getById(id);
            if(game==null){
                return id;
            }
            id++;
        }
    }
}
