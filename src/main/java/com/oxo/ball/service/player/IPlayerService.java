package com.oxo.ball.service.player;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.RechargeHanderDTO;
import com.oxo.ball.bean.dto.api.PayBackDto;
import com.oxo.ball.bean.dto.api.cha.PayNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.req.AuthEditPwdRequest;
import com.oxo.ball.bean.dto.req.player.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IPlayerService extends IService<BallPlayer> {

    int  STATUS_DISABLED = 403;
    int  API_TOO_FAST = 409;
    int  COUNTRY_INVALID = 410;
    String REDIS_DATA_CENTER = "ball_player_data_center";
    String REDIS_DATA_CENTER_DETAIL = "ball_player_data_center_detail";

    BallPlayer getCurrentUser(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException;
    BaseResponse registPlayer(PlayerRegistRequest ballPlayer, String ipAddress);
    BaseResponse login(PlayerAuthLoginRequest req, HttpServletRequest request, boolean isNewLogin);
    BaseResponse newDeviceslogin(PlayerAuthLoginNewDevicesRequest req, HttpServletRequest request);
    BaseResponse getVerifyCode() throws IOException;
    BaseResponse checkVerifyCode(String verifyKey,String code);

//    BaseResponse recharge(BallPlayer currentUser, Long money);
    BaseResponse recharge(BallPlayer player, BallLoggerRecharge loggerRecharge, PayBackDto payParamDto, BallPaymentManagement paymentManagement) throws JsonProcessingException;
    BaseResponse recharge(BallPlayer player, BallLoggerRecharge loggerRecharge, PayNoticeDtoIN payParamDto, BallPaymentManagement paymentManagement) throws JsonProcessingException;
    BaseResponse recharge(BallPlayer player, BallLoggerRecharge edit, PayNoticeDtoCHA build, BallPaymentManagement paymentManagement) throws JsonProcessingException;

    RechargeHanderDTO rechargeHander(BallPlayer player, long realMoneySys, BallSystemConfig systemConfig, BallLoggerRecharge loggerRecharge, BallPaymentManagement paymentManagement);

    BaseResponse withdrawal(BallPlayer currentUser, WithdrawalRequest withdrawalRequest);

    Map<String,Object> dataCenter(BallPlayer player, DataCenterRequest dataCenterRequest);

    List<Map<String,Object>> dataCenterDetail(BallPlayer player, DataCenterRequest dataCenterRequest);

    SearchResponse<BallPlayer> searchSub(SubPlayersRequest query, BallPlayer currentUser, Integer pageNo, Integer pageSize);

    BaseResponse bindBank(BallPlayer currentUser, BindBankCardRequest bindBankCardRequest);
    BaseResponse bindBankEdit(BallPlayer currentUser, BindBankCardRequest bindBankCardRequest);

    void clearDayActitiy();

    BaseResponse withdrawalPre(BallPlayer currentUser);

    BaseResponse rechargePre(BallPlayer currentUser, Double money, Long payId);

    BaseResponse rechargeCancel(BallPlayer currentUser);

    BaseResponse getPlayerServices(BallPlayer currentUser);

    BaseResponse getPlayerServices();

    BaseResponse getPhoneCode(PlayerBindPhoneCodeRequest bindPhoneCodeRequest, BallPlayer currentUser);

    BaseResponse bindPhone(PlayerBindPhoneRequest playerBindPhoneRequest, BallPlayer currentUser);

    BaseResponse phoneChangePwd(PlayerPhoneChangePwdRequest phoneChangePwdRequest);

    BaseResponse getPhoneCode(BallPlayer currentUser);

    BaseResponse getPhoneCode(PlayerChangePwdCodeRequest bindPhoneCodeRequest);

    BaseResponse editPwdPay(AuthEditPwdRequest req, BallPlayer player);

    BaseResponse phoneChangePwdPay(BallPlayer player, PlayerPhoneChangePayPwdRequest phoneChangePwdRequest);


    BaseResponse changePwdPay(BallPlayer player, PlayerChangePwdRequest changePwdRequest);

    BaseResponse getPhoneCode(PlayerNewDevicesRequest newDevicesRequest);

    BaseResponse changePwd(BallPlayer player, PlayerPhoneChangePayPwdRequest changePwdRequest);

}
