package com.oxo.ball.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dto.req.admin.PlayerRepairRecharge;
import com.oxo.ball.bean.dto.req.admin.PlayerRepairWithdrawal;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>
 * 玩家账号 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallPlayerService extends IService<BallPlayer> {

    SearchResponse<BallPlayer> search(BallPlayer query, Integer pageNo, Integer pageSize);
    SearchResponse<BallPlayer> searchlike(BallPlayer query, Integer pageNo, Integer pageSize);

    BaseResponse insert(BallPlayer ballPlayer);

    BaseResponse edit(BallPlayer ballPlayer);
    Boolean editPwd(BallPlayer ballPlayer);
    Boolean editPayPwd(BallPlayer ballPlayer);
    Boolean editStatus(BallPlayer ballPlayer);
    BaseResponse editAddBalance(BallPlayer ballPlayer) throws JsonProcessingException;
    Boolean editCaptchaPass(BallPlayer ballPlayer);
    Boolean editLevel(BallPlayer ballPlayer);
    BaseResponse info(BallPlayer ballPlayer);

    SearchResponse<BallPlayer> searchFinance(BallPlayer query, Integer pageNo, Integer pageSize);

    BallPlayer statisTotal();

    BallPlayer statisTotal(ReportDataRequest reportDataRequest);

    BaseResponse getPlayerCards(BallLoggerWithdrawal pid);

    BaseResponse insertMult(BallPlayer ballPlayer);

    Integer statisTotalRegist(Long id, Long begin, Long end);

    Integer statisFirstPayCount(Long id, Long begin, Long end);

    Long statisFrozen();

    List<BallPlayer> searchProxy(BallProxyLogger queryParam, BallPlayer proxyUser);

    SearchResponse<BallPlayer> queryActivePlayer(QueryActivePlayerRequest request, BallPlayer superTree);

    SearchResponse<BallPlayer> queryActivePlayerByBet(QueryActivePlayerRequest request, BallPlayer superTree);

    List<BallPlayer> queryActivePlayerAll(QueryActivePlayerRequest request, BallPlayer s);

    BaseResponse repairRecharge(PlayerRepairRecharge repairRecharge, BallAdmin currentUser) throws SQLException, JsonProcessingException;

    BaseResponse repairWithdrawal(PlayerRepairWithdrawal repairWithdrawal, BallAdmin currentUser) throws SQLException;

    BaseResponse setPlayerToProxy(Long id);

    List<BallPlayer> findProxys();
    List<BallPlayer> findProxys(BallProxyLogger queryParam);
    List<BallPlayer> findSubThree(ReportStandardRequest reportStandardRequest,BallPlayer player);

    void statisFirst(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data);

    void statisSubs(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data);

    void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data, BallSystemConfig systemConfig);

    void dayReward();

    int standard(ReportStandardRequest reportStandardRequest);
    int standardGrouop(ReportStandardRequest reportStandardRequest);

    SearchResponse<BallPlayer> searchStandard(BallPlayer build, Integer pageNo, Integer pageSize);

    BaseResponse sendMessageToPlayerChat(String minMax, Integer type,BallApiConfig ballApiConfig,boolean auto) throws IOException;

    void checkPlayerMessage();

}
