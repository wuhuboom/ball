package com.oxo.ball.service.impl.admin;

import com.oxo.ball.auth.BallApiFailException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.api.OddsDto;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.api.*;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.player.IPlayerGameService;
import com.oxo.ball.utils.*;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;

@Service
public class ApiServiceImpl implements IApiService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    SomeConfig someConfig;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IBallGameService gameService;
    @Autowired
    IPlayerGameService playerGameService;
    @Autowired
    IBallBetService betService;
    @Autowired
    IBallGameLossPerCentService lossPerCentService;
    @Autowired
    IBallApiConfigService apiConfigService;
    @Autowired
    IBallSystemConfigService systemConfigService;

    /**
     * 更新赛事比分-每分钟更新一次
     * 1.redis中读取联赛去查询赛事,如果redis中没有就查本地数据库的赛事
     * <p>
     * 测试阶段每小时更新一次,不然没有请求次数,查询当前已开赛的
     */
    @Override
    public void refreshFixtures() {
        // https://v3.football.api-sports.io/fixtures?live=all&league=
        //查询未结束比赛
        List<BallGame> unfinish = playerGameService.findUnfinish();
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        apiLog.info("未结束赛事:{}", unfinish.size());
        for (BallGame game : unfinish) {
            if (game.getFromTo() == 1) {
                apiLog.info("手动添加的赛事,过");
                continue;
            }
            //开始时间+90分钟 > 当前时间,不请求,因为时间还没到
            long finishTime = 0;
            if(game.getFinishTime()>0){
                //如果有结束时间,则按结束时间算
                finishTime = game.getFinishTime();
            }else{
                //没有结束时间则按配置时间算
                finishTime = game.getStartTime()+systemConfig.getGameFinishMin()*TimeUtil.TIME_ONE_MIN;
            }
            if (finishTime > System.currentTimeMillis()) {
                continue;
            }
            String res = null;
            try {
                BallApiConfig apiConfig = apiConfigService.getApiConfig();
                String key = StringUtils.isBlank(someConfig.getApiKey()) ? apiConfig.getBallApiKey() : someConfig.getApiKey();
                res = HttpUtil.doGet("https://v3.football.api-sports.io/fixtures?id=" + game.getId(),
                        doRequestHeader(key));
//                apiLog.info("db_fixture:{}", res);
                ApiFixtures apiFixtures = JsonUtil.fromJson(res, ApiFixtures.class);
                List<ApiFixtures.FixtureReponse> resposneList = apiFixtures.getResponse();
                if (resposneList == null || resposneList.isEmpty()) {
                    tgNotice(game.getId(), MessageFormat.format("赛事:{0},接口异常{1}",
                            game.getId().toString(), apiFixtures.getErrors()));
                    continue;
                }
                for (ApiFixtures.FixtureReponse response : resposneList) {
                    ApiFixture fixture = response.getFixture();
                    ApiGoals goals = response.getGoals();
                    ApiScore score = response.getScore();
                    //比赛是否结束,如果结束查数据库,有的话修改状态 并结算订单
                    switch (fixture.getStatus().getShortStatus()) {
                        case ApiFixtureStatus.STATUS_FT:
                            //比赛结束
                            BallGame edit = BallGame.builder()
                                    .id(game.getId())
                                    .homeHalf(score.getHalftime().getHome())
                                    .guestHalf(score.getHalftime().getAway())
                                    .homeFull(goals.getHome())
                                    .guestFull(goals.getAway())
                                    .homeOvertime(score.getExtratime().getHome())
                                    .guestOvertime(score.getExtratime().getAway())
                                    .homePenalty(score.getPenalty().getHome())
                                    .guestPenalty(score.getPenalty().getAway())
                                    .build();
                            //先更新比分
                            if(edit.getHomeFull()==null||edit.getGuestFull()==null){
                            }else{
                                BaseResponse edit1 = gameService.edit(edit);
                                if(edit1.getCode().equals(StatusCodes.OK)){
                                    ThreadPoolUtil.exec(() -> betService.betOpen(edit, false));
                                }else{
                                    apiLog.warn("赛事:{},接口异常{}",game.getId().toString(),fixture);
                                }
                            }
                            break;
                        case ApiFixtureStatus.STATUS_CANC:
                        case ApiFixtureStatus.STATUS_PST:
                        case ApiFixtureStatus.STATUS_INT:
                        case ApiFixtureStatus.STATUS_ABD:
                        case ApiFixtureStatus.STATUS_AWD:
                        case ApiFixtureStatus.STATUS_WO:
                            gameService.edit(BallGame.builder()
                                    .id(game.getId())
                                    .gameStatusRemark(ApiFixtureStatus.GAME_STATUS_EXP.get(fixture.getStatus().getShortStatus()))
                                    .build());
                            //TG警报
                            tgNotice(game.getId(), MessageFormat.format("赛事:{0},状态异常:{1}", game.getId().toString(), ApiFixtureStatus.GAME_STATUS_EXP.get(fixture.getStatus().getShortStatus())));
                            break;
                        default:
                            long time = System.currentTimeMillis() - game.getStartTime();
                            tgNotice(game.getId(), MessageFormat.format("赛事:{0},状态正常,但已超过90分钟,当前已进行:{1}分钟", game.getId().toString(), "" + (time / 1000 / 60)));
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                tgNotice(game.getId(), MessageFormat.format("赛事:{0},接口异常{1}", game.getId().toString(), res));
            } catch (Exception ex) {
                ex.printStackTrace();
                tgNotice(game.getId(), MessageFormat.format("赛事:{0},接口异常{1}", game.getId().toString(), res));
            }
        }
    }

    /**
     * 每天一次,只要未开赛的数据
     *
     * @param isToday
     */
    @Override
    public void refreshFixturesAll(boolean isToday) {
        try {
            List<BallGame> ballGames = new ArrayList<>();
            Date date = null;
            if (isToday) {
                //今天
                date = new Date();
            } else {
                date = TimeUtil.getBeginDayOfTomorrow();
            }
            BallApiConfig apiConfig = apiConfigService.getApiConfig();
            String now = TimeUtil.dateFormat(date, TimeUtil.TIME_YYYY_MM_DD);
            String key = StringUtils.isBlank(someConfig.getApiKey()) ? apiConfig.getBallApiKey() : someConfig.getApiKey();
            String res = HttpUtil.doGet("https://v3.football.api-sports.io/fixtures?status=NS&date=" + now,
                    doRequestHeader(key));
//            apiLog.info("day_fixture:{}", res);
            ApiFixtures apiFixtures = JsonUtil.fromJson(res, ApiFixtures.class);
            List<ApiFixtures.FixtureReponse> resposneList = apiFixtures.getResponse();
            if (resposneList == null || resposneList.isEmpty()) {
                return;
            }
            int count = 0;
            for (ApiFixtures.FixtureReponse response : resposneList) {
                ApiFixture fixture = response.getFixture();
                ApiTeams teams = response.getTeams();
                ApiGoals goals = response.getGoals();
                ApiScore score = response.getScore();
                ApiLeague league = response.getLeague();
                BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
                BallGame game = BallGame.builder()
                        .id(fixture.getId())
                        .status(1)
                        .createdAt(TimeUtil.getNowTimeMill())
                        .allianceLogo(league.getLogo())
                        .allianceName(league.getName())
                        .mainLogo(teams.getHome().getLogo())
                        .mainName(teams.getHome().getName())
                        .guestLogo(teams.getAway().getLogo())
                        .guestName(teams.getAway().getName())
                        .startTime(fixture.getTimestamp() * 1000)
                        .finishTime(fixture.getTimestamp() * 1000+TimeUtil.TIME_ONE_MIN*systemConfig.getGameFinishMin())
                        .ymd(TimeUtil.dateFormat(new Date(fixture.getTimestamp() * 1000), TimeUtil.TIME_YYYY_MM_DD))
                        .fromTo(0)
                        .build();
                //比赛是否结束,如果结束查数据库,有的话修改状态 并结算订单
                switch (fixture.getStatus().getShortStatus()) {
                    case ApiFixtureStatus.STATUS_TBD:
                    case ApiFixtureStatus.STATUS_NS:
                        game.setGameStatus(1);
                        break;
                    default:
//                        gameService.deleteById(game.getId());
                        continue;
                }
                //过期赛事不要
                if (System.currentTimeMillis() + TimeUtil.TIME_ONE_HOUR/2 > game.getStartTime()) {
//                    gameService.deleteById(game.getId());
                    continue;
                }
//                if(total>3){
//                    break;
//                }
                //初始保存
                try {
//                    if (gameService.insert(game)) {
                    //存入redis,然后去查赔率
                    redisUtil.leftSet(RedisKeyContant.GAME_NEED_QUERY_ODDS, game);
                    ballGames.add(game);
                    count++;
//                    }
                } catch (Exception ex) {
                }
            }
            apiLog.info("API读取到[{}]场可用赛事",count);
//            refreshOdds(ballGames);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void refreshFixturesAllTest(boolean isToday) {
        try {
            List<BallGame> ballGames = new ArrayList<>();
            Date date = null;
            if (isToday) {
                //今天
                date = new Date();
            } else {
                date = TimeUtil.getBeginDayOfTomorrow();
            }
            String now = TimeUtil.dateFormat(date, TimeUtil.TIME_YYYY_MM_DD);
            BallApiConfig apiConfig = apiConfigService.getApiConfig();
            String key = StringUtils.isBlank(someConfig.getApiKey()) ? apiConfig.getBallApiKey() : someConfig.getApiKey();
            String res = HttpUtil.doGet("https://v3.football.api-sports.io/fixtures?status=NS&date=" + now,
                    doRequestHeader(key));
//            apiLog.info("day_fixture:{}", res);
            ApiFixtures apiFixtures = JsonUtil.fromJson(res, ApiFixtures.class);
            List<ApiFixtures.FixtureReponse> resposneList = apiFixtures.getResponse();
            if (resposneList == null || resposneList.isEmpty()) {
                return;
            }
            int total = 0;
//            apiLog.info("API读取到[{}]场赛事",resposneList.size());
            for (ApiFixtures.FixtureReponse response : resposneList) {
                ApiFixture fixture = response.getFixture();
                ApiTeams teams = response.getTeams();
                ApiGoals goals = response.getGoals();
                ApiScore score = response.getScore();
                ApiLeague league = response.getLeague();
                BallGame game = BallGame.builder()
                        .id(fixture.getId())
                        .status(1)
                        .createdAt(TimeUtil.getNowTimeMill())
                        .allianceLogo(league.getLogo())
                        .allianceName(league.getName())
                        .mainLogo(teams.getHome().getLogo())
                        .mainName(teams.getHome().getName())
                        .guestLogo(teams.getAway().getLogo())
                        .guestName(teams.getAway().getName())
                        .startTime(fixture.getTimestamp() * 1000)
                        .ymd(TimeUtil.dateFormat(new Date(fixture.getTimestamp() * 1000), TimeUtil.TIME_YYYY_MM_DD))
                        .fromTo(0)
                        .build();
                //比赛是否结束,如果结束查数据库,有的话修改状态 并结算订单
                switch (fixture.getStatus().getShortStatus()) {
                    case ApiFixtureStatus.STATUS_TBD:
                    case ApiFixtureStatus.STATUS_NS:
                        game.setGameStatus(1);
                        break;
                    default:
                        continue;
                }
                //过期赛事不要
                if (System.currentTimeMillis() + TimeUtil.TIME_ONE_HOUR > game.getStartTime()) {
                    continue;
                }
                if (total > 3) {
                    break;
                }
                try {
                    //存入redis,然后去查赔率
                    redisUtil.leftSet(RedisKeyContant.GAME_NEED_QUERY_ODDS, game);
                    ballGames.add(game);
                } catch (Exception ex) {
                }
            }
//            refreshOdds(ballGames);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshOdds(List<BallGame> games) {
        int today = 0;
        int tomrrow = 0;
        apiLog.info("请求到【" + games.size() + "个】赛事");
        // https://v3.football.api-sports.io/odds?bookmaker=&fixture=
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        String key = StringUtils.isBlank(someConfig.getApiKey()) ? apiConfig.getBallApiKey() : someConfig.getApiKey();
        for (BallGame game : games) {
            String res = HttpUtil.doGet("https://v3.football.api-sports.io/odds?fixture=" + game.getId(),
//            String res = HttpUtil.doGet("https://v3.football.api-sports.io/odds?bookmaker=" +
//                            someConfig.getApiBookmaker() + "&fixture=" + game.getId(),
                    doRequestHeader(key));
//            apiLog.info("odds:{}", res);
            try {
                ApiOdds apiOdds = JsonUtil.fromJson(res, ApiOdds.class);
                List<ApiOdds.ApiOddsResponse> response = apiOdds.getResponse();
                if (response == null || response.isEmpty()) {
//                    apiLog.info("odds:{}not found,league:[{}]",game.getId(),game.getAllianceName());
//                    gameService.deleteById(game.getId());
                    continue;
                }
                List<ApiBookmakers> bookmakers = response.get(0).getBookmakers();
                if (bookmakers == null || bookmakers.isEmpty()) {
//                    apiLog.info("odds:{}not found,league:[{}]",game.getId(),game.getAllianceName());
//                    gameService.deleteById(game.getId());
                    continue;
                }
                List<BallGameLossPerCent> byGameId = lossPerCentService.findByGameId(game.getId());
                if (byGameId != null && !byGameId.isEmpty()) {
                    continue;
                }

                if (today > 3) {
//                    gameService.deleteById(game.getId());
                    continue;
                }
                if (tomrrow > 3) {
//                    gameService.deleteById(game.getId());
                    continue;
                }
                ApiBookmakers apiBookmakers = bookmakers.get(0);
                List<ApiBookmakers.ApiBets> bets = apiBookmakers.getBets();
                for (ApiBookmakers.ApiBets bet : bets) {
                    if (bet.getName().equals("Exact Score")) {
                        //全场比分和赔率
                        List<ApiBookmakers.ApiBetItem> values = bet.getValues();
                        List<BallGameLossPerCent> lossPerCents = new ArrayList<>();
                        createPerLoss(game, values, lossPerCents, 2);
                        lossPerCentService.batchInsert(lossPerCents);
                    }
                    if (bet.getName().equals("Correct Score - First Half")) {
                        List<ApiBookmakers.ApiBetItem> values = bet.getValues();
                        List<BallGameLossPerCent> lossPerCents = new ArrayList<>();
                        createPerLoss(game, values, lossPerCents, 1);
                        lossPerCentService.batchInsert(lossPerCents);
                    }
                }
                if (game.getStartTime() < TimeUtil.getDayEnd().getTime()) {
                    today++;
                }
                if (game.getStartTime() < TimeUtil.getEndDayOfTomorrow().getTime()
                        && game.getStartTime() > TimeUtil.getBeginDayOfTomorrow().getTime()) {
                    tomrrow++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 仅一次
     */
    @Override
    public void refreshOdds() {
        // https://v3.football.api-sports.io/odds?bookmaker=&fixture=
        Object lpop = redisUtil.lpop(RedisKeyContant.GAME_NEED_QUERY_ODDS);
        if (lpop == null) {
            return;
        }
        List<BallGameLossPerCent> fullLossPerCents = new ArrayList<>();
        List<BallGameLossPerCent> halfLossPerCents = new ArrayList<>();
        boolean hasFull = false;
        boolean hasHalf = false;

        BallGame game = (BallGame) lpop;
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        String key = StringUtils.isBlank(someConfig.getApiKey()) ? apiConfig.getBallApiKey() : someConfig.getApiKey();
        String res = HttpUtil.doGet("https://v3.football.api-sports.io/odds?fixture=" + game.getId(),
                doRequestHeader(key));
//        apiLog.info("odds:{}", res);
        try {
            ApiOdds apiOdds = JsonUtil.fromJson(res, ApiOdds.class);
            List<ApiOdds.ApiOddsResponse> response = apiOdds.getResponse();
            if (response == null || response.isEmpty()) {
//                apiLog.info("odds:{}not found",game.getId());
//                gameService.deleteById(game.getId());
                throw new BallApiFailException();
            }
            List<ApiBookmakers> bookmakers = response.get(0).getBookmakers();
            if (bookmakers == null || bookmakers.isEmpty()) {
//                apiLog.info("odds:{}not found",game.getId());
//                gameService.deleteById(game.getId());
                throw new BallApiFailException();
            }
            List<BallGameLossPerCent> byGameId = lossPerCentService.findByGameId(game.getId());
            if (byGameId != null && !byGameId.isEmpty()) {
                throw new BallApiFailException();
            }
//            apiLog.info("API读取到[{}][{}]个赔率",game.getId(),bookmakers.size());
            outer:
            for (ApiBookmakers apiBookmakers : bookmakers) {
                List<ApiBookmakers.ApiBets> bets = apiBookmakers.getBets();
                for (ApiBookmakers.ApiBets bet : bets) {
                    if (bet.getName().equals("Exact Score") && !hasFull) {
                        //全场比分和赔率
                        List<ApiBookmakers.ApiBetItem> values = bet.getValues();
                        boolean perLossFull = createPerLossFull(game, values, fullLossPerCents);
                        if (perLossFull) {
                            hasFull = true;
                        } else {
                            continue outer;
                        }
                    }
                    if (bet.getName().equals("Correct Score - First Half") && !hasHalf) {
                        List<ApiBookmakers.ApiBetItem> values = bet.getValues();
                        boolean perLossHalf = createPerLossHalf(game, values, halfLossPerCents);
                        if (perLossHalf) {
                            hasHalf = true;
                        } else {
                            continue outer;
                        }
                    }
                }
                if (hasHalf&&hasFull) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BallApiFailException ex){
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (hasHalf&&hasFull) {
            //保存赛事
            Boolean insert = gameService.insert(game);
            if(insert){
                lossPerCentService.batchInsert(fullLossPerCents);
                lossPerCentService.batchInsert(halfLossPerCents);
            }
            //读取到赔率跳出循环,未有赔率读取下个公司

            apiLog.info("赔率正常增加一场赛事{}",game.getId());
        }else{
            // 使用手动赔率
            createPerLossSelf(OddsDto.full(),game,fullLossPerCents,2);
            createPerLossSelf(OddsDto.half(),game,halfLossPerCents,1);
            //读取到赔率跳出循环,未有赔率读取下个公司
            //保存赛事
            Boolean insert = gameService.insert(game);
            if(insert){
                lossPerCentService.batchInsert(fullLossPerCents);
                lossPerCentService.batchInsert(halfLossPerCents);
            }
            apiLog.info("使用预创建赔率{}",game.getId());
        }
    }

    @Override
    public void refreshOddsTest(Long gameId, String json) {
//        try {
//            BallGame game = gameService.getById(gameId);
//            ApiOdds apiOdds = JsonUtil.fromJson(json, ApiOdds.class);
//            List<ApiOdds.ApiOddsResponse> response = apiOdds.getResponse();
//            if (response == null || response.isEmpty()) {
////                apiLog.info("odds:{}not found",game.getId());
////                gameService.deleteById(game.getId());
//                return;
//            }
//            List<ApiBookmakers> bookmakers = response.get(0).getBookmakers();
//            if (bookmakers == null || bookmakers.isEmpty()) {
////                apiLog.info("odds:{}not found",game.getId());
////                gameService.deleteById(game.getId());
//                return;
//            }
//            List<BallGameLossPerCent> byGameId = lossPerCentService.findByGameId(game.getId());
//            if (byGameId != null && !byGameId.isEmpty()) {
//                return;
//            }
//            ApiBookmakers apiBookmakers = bookmakers.get(0);
//            List<ApiBookmakers.ApiBets> bets = apiBookmakers.getBets();
//            for (ApiBookmakers.ApiBets bet : bets) {
//                if (bet.getName().equals("Exact Score")) {
//                    //全场比分和赔率
//                    List<ApiBookmakers.ApiBetItem> values = bet.getValues();
//                    List<BallGameLossPerCent> lossPerCents = new ArrayList<>();
//                    createPerLossFull(game, values, lossPerCents);
//                    lossPerCentService.batchInsert(lossPerCents);
//                }
//                if (bet.getName().equals("Correct Score - First Half")) {
//                    List<ApiBookmakers.ApiBetItem> values = bet.getValues();
//                    List<BallGameLossPerCent> lossPerCents = new ArrayList<>();
//                    createPerLossHalf(game, values, lossPerCents);
//                    lossPerCentService.batchInsert(lossPerCents);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void tgNotice(Long gameId, String message) {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if (systemConfig.getCloseNotice() == 1) {
            return;
        }
        //比赛有投注才通知
        SearchResponse<BallBet> search = betService.search(BallBet.builder()
                .gameId(gameId)
                .status(1)
                .build(), 1, 1);
        if (search.getResults() == null || search.getResults().isEmpty() || search.getResults().get(0) == null) {
            //赛事无订单直接结束
            gameService.edit(BallGame.builder()
                    .id(gameId)
                    .gameStatus(3)
                    .build());
            return;
        }
        String key = RedisKeyContant.GAME_STATUS_EXT + gameId;
        Object o = redisUtil.get(key);
        if (o != null) {
            return;
        }
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        try {
            message = URLEncoder.encode(message, "utf-8");
            String url = MessageFormat.format("https://api.telegram.org/bot{0}/sendMessage?chat_id={1}&text={2}", apiConfig.getTgToken(), apiConfig.getTgChat(), message);
            //{"ok":true,"result":{"message_id":3,"from":{"id":5762353581,"is_bot":true,"first_name":"botboya","username":"lboya_bot"},"chat":{"id":-1001511925511,"title":"b_bot_test","username":"our_b_bots","type":"supergroup"},"date":1663206257,"text":"hello"}}
            String s = null;
            if (someConfig.getApiSwitch() == null) {
                s = HttpUtil.doGet(url, null);
            } else {
                s = HttpUtil.doGetProxy(url, null);
            }
            try {
                Map map = JsonUtil.fromJson(s, Map.class);
                if ((boolean) map.get("ok")) {
                    redisUtil.set(key, 1, TimeUtil.TIME_ONE_MIN * 5 / 1000);
                }
            } catch (IOException e) {
            }
        } catch (UnsupportedEncodingException e) {
        }catch (Exception e){}
    }

    @Override
    public void tgNotice(String message) {
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        try {
            message = URLEncoder.encode(message, "utf-8");
            String url = MessageFormat.format("https://api.telegram.org/bot{0}/sendMessage?chat_id={1}&text={2}", apiConfig.getTodoToken(), apiConfig.getTodoChat(), message);
            //{"ok":true,"result":{"message_id":3,"from":{"id":5762353581,"is_bot":true,"first_name":"botboya","username":"lboya_bot"},"chat":{"id":-1001511925511,"title":"b_bot_test","username":"our_b_bots","type":"supergroup"},"date":1663206257,"text":"hello"}}
            if (someConfig.getApiSwitch() == null) {
                HttpUtil.doGet(url, null);
            } else {
                HttpUtil.doGetProxy(url, null);
            }
        } catch (UnsupportedEncodingException e) {
        }catch (Exception e){}
    }

    private void createPerLoss(BallGame game, List<ApiBookmakers.ApiBetItem> values, List<BallGameLossPerCent> lossPerCents, int gameType) {
        for (ApiBookmakers.ApiBetItem item : values) {
            String score = item.getValue();
            String[] split = score.split(":");
            BallGameLossPerCent add = BallGameLossPerCent.builder()
                    .lossPerCent(item.getOdd())
                    .scoreHome(split[0])
                    .scoreAway(split[1])
                    .antiPerCent(BigDecimalUtil.antiPerCent(item.getOdd()))
                    .even(2)
                    .status(1)
                    .gameId(game.getId())
                    .gameType(gameType)
                    .build();
            add.setCreatedAt(System.currentTimeMillis());
            add.setUpdatedAt(System.currentTimeMillis());
            lossPerCents.add(add);
        }
    }

    private boolean createPerLossFull(BallGame game, List<ApiBookmakers.ApiBetItem> values, List<BallGameLossPerCent> lossPerCents) {
        Map<String, OddsDto> full = OddsDto.full();
        for (ApiBookmakers.ApiBetItem item : values) {
            String score = item.getValue();
            String[] split = score.split(":");
            if (full.get(score) == null) {
                //如果比分不是指定比分，则略过
                continue;
            }
            BallGameLossPerCent add = BallGameLossPerCent.builder()
                    .lossPerCent(item.getOdd())
                    .antiPerCent(BigDecimalUtil.antiPerCent(item.getOdd()))
                    .scoreHome(split[0])
                    .scoreAway(split[1])
                    .even(2)
                    .status(1)
                    .gameId(game.getId())
                    .gameType(2)
                    .build();
            add.setCreatedAt(System.currentTimeMillis());
            add.setUpdatedAt(System.currentTimeMillis());
            lossPerCents.add(add);
            full.remove(score);
            //移出已保存赔率
        }
        for (OddsDto item : full.values()) {
            if (StringUtils.isBlank(item.getOdds())) {
                return false;
            }
            BallGameLossPerCent add = BallGameLossPerCent.builder()
                    .lossPerCent(BigDecimalUtil.lossPerCent(item.getOdds()))
                    .antiPerCent(item.getOdds())
                    .scoreHome(item.getHome())
                    .scoreAway(item.getAway())
                    .even(2)
                    .status(1)
                    .gameId(game.getId())
                    .gameType(2)
                    .build();
            add.setCreatedAt(System.currentTimeMillis());
            add.setUpdatedAt(System.currentTimeMillis());
            lossPerCents.add(add);
        }
        return true;
    }
    private void createPerLossSelf(Map<String, OddsDto> oddsmap, BallGame game, List<BallGameLossPerCent> lossPerCents, int gameType) {
        for(OddsDto item:oddsmap.values()){
            String odds = OddsDto.getRandomOdd(item.getOdds());
            BallGameLossPerCent add = BallGameLossPerCent.builder()
                    .lossPerCent(BigDecimalUtil.lossPerCent(odds))
                    .antiPerCent(odds)
                    .scoreHome(item.getHome())
                    .scoreAway(item.getAway())
                    .even(2)
                    .status(1)
                    .gameId(game.getId())
                    .gameType(gameType)
                    .build();
            add.setCreatedAt(System.currentTimeMillis());
            add.setUpdatedAt(System.currentTimeMillis());
            lossPerCents.add(add);
        }
    }

    private boolean createPerLossHalf(BallGame game, List<ApiBookmakers.ApiBetItem> values, List<BallGameLossPerCent> lossPerCents) {
        Map<String, OddsDto> half = OddsDto.half();
        for (ApiBookmakers.ApiBetItem item : values) {
            String score = item.getValue();
            String[] split = score.split(":");
            if (half.get(score) == null) {
                //如果比分不是指定比分，则略过
                continue;
            }
            BallGameLossPerCent add = BallGameLossPerCent.builder()
                    .lossPerCent(item.getOdd())
                    .scoreHome(split[0])
                    .scoreAway(split[1])
                    .antiPerCent(BigDecimalUtil.antiPerCent(item.getOdd()))
                    .even(2)
                    .status(1)
                    .gameId(game.getId())
                    .gameType(1)
                    .build();
            add.setCreatedAt(System.currentTimeMillis());
            add.setUpdatedAt(System.currentTimeMillis());
            lossPerCents.add(add);
            half.remove(score);
        }
        for (OddsDto item : half.values()) {
            if (StringUtils.isBlank(item.getOdds())) {
                return false;
            }
            BallGameLossPerCent add = BallGameLossPerCent.builder()
                    .lossPerCent(BigDecimalUtil.lossPerCent(item.getOdds()))
                    .antiPerCent(item.getOdds())
                    .scoreHome(item.getHome())
                    .scoreAway(item.getAway())
                    .even(2)
                    .status(1)
                    .gameId(game.getId())
                    .gameType(1)
                    .build();
            add.setCreatedAt(System.currentTimeMillis());
            add.setUpdatedAt(System.currentTimeMillis());
            lossPerCents.add(add);
        }
        return true;
    }

    private Map<String, String> doRequestHeader(String key) {
        Map<String, String> header = new HashMap<>();
        header.put("x-rapidapi-host", "v3.football.api-sports.io");
        header.put("x-rapidapi-key", key);
        return header;
    }
}
