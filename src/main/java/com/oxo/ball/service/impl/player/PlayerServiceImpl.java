package com.oxo.ball.service.impl.player;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.RechargeHanderDTO;
import com.oxo.ball.bean.dto.api.PayBackDto;
import com.oxo.ball.bean.dto.api.PayParamDto;
import com.oxo.ball.bean.dto.api.cha.PayNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.cha.PayRequestDtoCHA;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in.PayRequestDtoIN;
import com.oxo.ball.bean.dto.api.in3.PayRequestDto3;
import com.oxo.ball.bean.dto.api.tnz.PayParamDtoTnz;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import com.oxo.ball.bean.dto.queue.MessageQueueDTO;
import com.oxo.ball.bean.dto.queue.MessageQueueLogin;
import com.oxo.ball.bean.dto.req.AuthEditPwdRequest;
import com.oxo.ball.bean.dto.req.admin.RateConfigs;
import com.oxo.ball.bean.dto.req.player.*;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.resp.AuthLoginResponse;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallPlayerMapper;
import com.oxo.ball.service.*;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.admin.BallBalanceChangeServiceImpl;
import com.oxo.ball.service.impl.admin.BallPlayerServiceImpl;
import com.oxo.ball.service.pay.*;
import com.oxo.ball.service.pay.impl.PlayerPayServiceImplFAST;
import com.oxo.ball.service.pay.impl.PlayerPayServiceImplWOW;
import com.oxo.ball.service.player.AuthPlayerService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.*;
import com.oxo.ball.ws.WebSocketManager;
import com.oxo.ball.ws.dto.MessageResponse;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.resource.HttpResource;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Time;
import java.text.MessageFormat;
import java.util.*;

/**
 * <p>
 * 玩家账号 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class PlayerServiceImpl extends ServiceImpl<BallPlayerMapper, BallPlayer> implements IPlayerService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Resource
    private RedisUtil redisUtil;
    @Resource
    AuthPlayerService authService;

    @Resource
    IBallSystemConfigService systemConfigService;
    @Resource
    IBasePlayerService basePlayerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Resource
    IBallLoggerService loggerService;
    @Resource
    IMessageQueueService messageQueueService;
    @Resource
    IBallBankCardService bankCardService;
    @Autowired
    IBallLoggerBindCardService loggerBindCardService;
    @Autowired
    IBallBankService bankService;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    IBallVirtualCurrencyService virtualCurrencyService;
    @Autowired
    IBallSimCurrencyService simCurrencyService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallDepositPolicyService depositPolicyService;
    @Autowired
    IPlayerPayService playerPayService;
    @Autowired
    IPlayerPayServiceIN playerPayServiceIN;
    @Autowired
    IPlayerPayServiceFAST playerPayServiceFAST;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    ISmsService smsService;
    @Autowired
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    IPlayerPayServiceCHA playerPayServiceCHA;
    @Autowired
    IBallWithdrawManagementService withdrawManagementService;
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Autowired
    WebSocketManager webSocketManager;
    @Autowired
    private IApiService apiService;
    @Autowired
    private BallAdminService adminService;
    @Autowired
    private IPlayerPayService3 playerPayServiceIn3;
    @Resource
    private IPlayerPayServiceWOW playerPayServiceWOW;
    @Resource
    private IPlayerPayServiceAllPay playerPayServiceAllPay;
    @Autowired
    IPlayerPayServiceTNZ playerPayServiceTNZ;
    @Autowired
    IPlayerPayServiceMETA playerPayServiceMETA;
    @Autowired
    private IPlayerPayServiceMETAGG playerPayServiceMETAGG;
    @Autowired
    private IPlayerPayServiceWEB playerPayServiceWEB;
    @Autowired
    private IPlayerPayServiceXD playerPayServiceXD;
    @Autowired
    private IPlayerPayServiceMP playerPayServiceMP;

    @Override
    public BallPlayer getCurrentUser(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        Long userId;
        try {
            List<String> audience = JWT.decode(request.getHeader("token")).getAudience();
            userId = Long.parseLong(audience.get(0));
            BallPlayer byId = basePlayerService.findById(userId);
            byId.setIp(IpUtil.getIpAddress(request));
            if (byId.getStatus() == 2) {
                throw new PlayerDisabledException();
            }
            return byId;
        } catch (JWTDecodeException j) {
            throw new TokenInvalidedException();
        } catch (PlayerDisabledException e) {
            throw new PlayerDisabledException();
        } catch (Exception ex) {
            throw new TokenInvalidedException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse registPlayer(PlayerRegistRequest registRequest, String ipAddress) {
        Long parentPlayerId = -1L;
        String superTree = "_";
        String parentPlayerName = "";
        BallPlayer parentPlayer = null;
        //TODO 注册判定验证码 先判断验证码是否正确
        if(someConfig.getApiSwitch()==null){
            BaseResponse baseResponse = checkVerifyCode(registRequest.getVerifyKey(), registRequest.getCode());
            if (baseResponse.getCode() != StatusCodes.OK) {
                return baseResponse;
            }
        }
        //检查数据库里面是否有用户名
        BallPlayer ballPlayer = basePlayerService.findByUsername(registRequest.getUsername());
        if (ballPlayer != null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("username", "nameExists"));
        }
        //手机号是否已注册
        if (!StringUtils.isBlank(registRequest.getAreaCode()) && !StringUtils.isBlank(registRequest.getPhone())) {
            BallPlayer byPhone = basePlayerService.findByPhone(registRequest.getAreaCode(), registRequest.getPhone());
            if(byPhone!=null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("phone", "phoneExsit"));
            }
        }

        //判断两次密码是否相等
        if (!registRequest.getPassword().equals(registRequest.getTwoPassword())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("password", "passwordDiff"));
        }
        //是否有输入邀请码,判定当前系统是否开启了邀请码
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if (systemConfig.getRegisterIfNeedVerificationCode() == 1) {
            //开启了邀请码,必须输入邀请码
            if (StringUtils.isBlank(registRequest.getInvitationCode())) {
                //没有邀请码
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("invitationCode", "invitationCodeIsEmpty"));
            } else {
                //邀请码是否正确
                parentPlayer = basePlayerService.findByInvitationCode(registRequest.getInvitationCode());
                if (parentPlayer == null || parentPlayer.getStatus() == 2) {
                    // 查询不到或者账号被禁用
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("invitationCode", "invitationCodeError"));
                } else {
                    //是否开启了未充值不能邀请
                    if (systemConfig.getSwitchNoRecharge() == 1) {
                        if (parentPlayer.getCumulativeTopUp() == null || parentPlayer.getFirstTopUp() == 0) {
                            //是否有手动上分
                            SearchResponse<BallLoggerHandsup> search = loggerHandsupService.search(BallLoggerHandsup.builder()
                                    .playerId(parentPlayer.getId())
                                    .build(), 1, 1);
                            if (search.getTotalCount() == 0) {
                                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                                        ResponseMessageUtil.responseMessage("invitationCode", "invitationCodeError"));
                            }
                        }
                    }
                    apiLog.info("regist_parent:{}", parentPlayer);
                    parentPlayerName = parentPlayer.getUsername();
                    //邀请码正确,注册账号上级为邀请码关联账号
                    parentPlayerId = parentPlayer.getId();
                    //tree
                    superTree = (StringUtils.isBlank(parentPlayer.getSuperTree()) ? "_" : parentPlayer.getSuperTree()) + parentPlayer.getId() + "_";
                }
            }
        } else {
            //TODO 当前没有指定默认邀请码
            //没有指定邀请码就是顶端tree path
//            superTree = "_";
            if (StringUtils.isBlank(systemConfig.getDefaultProxy())) {
                superTree = "_";
            } else {
                parentPlayer = basePlayerService.findByUsername(systemConfig.getDefaultProxy());
                if (parentPlayer == null) {
                    //账号被禁用
                    superTree = "_";
                } else {
                    apiLog.info("regist_parent_default:{}", parentPlayer);
                    parentPlayerName = parentPlayer.getUsername();
                    //邀请码正确,注册账号上级为邀请码关联账号
                    parentPlayerId = parentPlayer.getId();
                    //tree
                    superTree = (StringUtils.isBlank(parentPlayer.getSuperTree()) ? "_" : parentPlayer.getSuperTree()) + parentPlayer.getId() + "_";
                }
            }
        }
        String invitationCode = "";
        while (true) {
            invitationCode = String.valueOf(TimeUtil.getRandomNum(1234567, 9876543));
            BallPlayer byInvitationCode = basePlayerService.findByInvitationCode(invitationCode);
            if (byInvitationCode == null) {
                break;
            }
        }
        BallPlayer save = BallPlayer.builder()
                .version(1L)
                .username(registRequest.getUsername())
                .password(PasswordUtil.genPasswordMd5(registRequest.getPassword()))
                .invitationCode(invitationCode)
                .userId(basePlayerService.createUserId())
                .statusOnline(0)
                .superiorId(parentPlayerId)
                .superTree(superTree)
                .vipRank(BallPlayer.getTreeCount(superTree))
                .accountType(2)
                .status(1)
                .balance(0L)
                .superiorName(parentPlayerName)
                .vipLevel(0)
                .build();
//                        .phone(registRequest.getPhone())
//                .areaCode(registRequest.getAreaCode())
        if (!StringUtils.isBlank(registRequest.getAreaCode()) && !StringUtils.isBlank(registRequest.getPhone())) {
            save.setAreaCode(registRequest.getAreaCode());
            save.setPhone(registRequest.getPhone());
        }
        MapUtil.setCreateTime(save);

        boolean res = save(save);
        if (res) {
            //如果保存成功，并且有上级，重新计算上级的团队和下属计数
            if (save.getSuperiorId() != 0 && save.getSuperiorId() != -1) {
                //上级直属下级+1,团队人数+1
                while (true) {
                    BallPlayer parentPlayerEdit = BallPlayer.builder()
                            .version(parentPlayer.getVersion())
                            .directlySubordinateNum(parentPlayer.getDirectlySubordinateNum() + 1)
                            .groupSize(parentPlayer.getGroupSize() + 1)
                            .build();
                    parentPlayerEdit.setId(parentPlayerId);
                    boolean isSucc = basePlayerService.editAndClearCache(parentPlayerEdit, parentPlayer);
                    if (isSucc) {
                        break;
                    } else {
                        parentPlayer = basePlayerService.findById(parentPlayerId);
                    }
                }
                //上级的上上上。。级团队人数+1
                String treePath = parentPlayer.getSuperTree();
                if (!StringUtils.isBlank(treePath) && !treePath.equals("_")) {
                    String ids = StringUtils.join(treePath.split("_"), ",").substring(1);
                    if (!StringUtils.isBlank(ids)) {
                        basePlayerService.editMultGroupNum(ids, 1);
                    }
                }

            }
            redisUtil.del(RedisKeyContant.VERIFY_KEY + registRequest.getVerifyKey());
        }
        return res ? BaseResponse.successWithMsg("ok") : BaseResponse.failedWithMsg("failed");
    }


    @Override
    public BaseResponse login(PlayerAuthLoginRequest req, HttpServletRequest request, boolean isNewLogin) {
        //判定账号密码输入错误次数
        String redisKey = RedisKeyContant.PLAYER_LOGIN_FAIL_COUNT + req.getUsername();
        String ip = IpUtil.getIpAddress(request);
        Object failCountRedis = redisUtil.get(redisKey);
        int failCount = 0;
        if (failCountRedis != null) {
            failCount = Integer.parseInt(failCountRedis.toString());
        }
        BaseResponse baseResponse = checkVerifyCode(req.getVerifyKey(), req.getCode());
        if (baseResponse.getCode() != StatusCodes.OK) {
            return baseResponse;
        }
        BallPlayer ballPlayer = null;
        try {
            ballPlayer = basePlayerService.findByUsername(req.getUsername());
            if (ballPlayer.getStatus() == 2) {
                return BaseResponse.failedWithMsg(IPlayerService.STATUS_DISABLED, "");
            }
        } catch (Exception e) {
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if (ballPlayer == null || !ballPlayer.getPassword().equals(PasswordUtil.genPasswordMd5(req.getPassword()))) {
            if (failCount == systemConfig.getPasswordMaxErrorTimes()) {
                List<Map<String, Object>> errorList = ResponseMessageUtil.responseMessage("password", "pwdErrorMax");
                ResponseMessageUtil.responseMessage(errorList, "time", String.valueOf(redisUtil.getExpire(redisKey)));
                return BaseResponse.failedWithData(BaseResponse.LOGIN_PWD_ERROR,
                        errorList);
            }
            //缓存当前输入错误次数
            redisUtil.incr(redisKey, 1);
            redisUtil.expire(redisKey, systemConfig.getPasswordErrorLockTime());
            failCount++;
            if (failCount == systemConfig.getPasswordMaxErrorTimes()) {
                //指定秒后解锁
                redisUtil.expire(redisKey, systemConfig.getPasswordErrorLockTime());
                List<Map<String, Object>> errorList = ResponseMessageUtil.responseMessage("password", "pwdErrorMax");
                ResponseMessageUtil.responseMessage(errorList, "time", String.valueOf(redisUtil.getExpire(redisKey)));
                ResponseMessageUtil.responseMessageValue(errorList, "failCount", String.valueOf(failCount));
                return BaseResponse.failedWithData(BaseResponse.LOGIN_PWD_ERROR,
                        errorList);
            }
            List<Map<String, Object>> errorList = ResponseMessageUtil.responseMessage("password", "pwdErrorCount");
            ResponseMessageUtil.responseMessageValue(errorList, "failCount", String.valueOf(failCount));
            return BaseResponse.failedWithData(BaseResponse.LOGIN_PWD_ERROR,
                    errorList);
        }
        //TODO loginFrozening 如果 密码次数错误上限,但是输入正确 了,提示稍后
        if (failCount == systemConfig.getPasswordMaxErrorTimes()) {
            List<Map<String, Object>> errorList = ResponseMessageUtil.responseMessage("password", "loginFrozening");
            ResponseMessageUtil.responseMessage(errorList, "time", String.valueOf(redisUtil.getExpire(redisKey)));
            return BaseResponse.failedWithData(BaseResponse.LOGIN_PWD_ERROR,
                    errorList);
        }
        //判定是否换设备登录
        //判定是否是新设备登录,开关为打开才判定
        String ua = IpUtil.getUa(request);
        if (systemConfig.getNewDevices() == 1) {
            BallLoggerLogin playerLastLogin = loggerService.findPlayerLastLogin(ballPlayer);
            //TODO 新设备 不是从新设备登录过来&绑定了手机&登录过&登录改变才需要手机验证码
            if (!isNewLogin && !StringUtils.isBlank(ballPlayer.getPhone()) && playerLastLogin != null && !ua.equals(playerLastLogin.getDevices())) {
                return BaseResponse.failedWithMsg(BaseResponse.NEW_DEVICES_LOGIN, "new devices");
            }
        }

        AuthLoginResponse userBean = new AuthLoginResponse();
        userBean.setId(ballPlayer.getId());
        userBean.setUserName(ballPlayer.getUsername());
        //TODO 同时只能1个设备登录
//        Object hget = redisUtil.get(PlayerAuthServiceImpl.REDIS_PLAYER_AUTH_KEY + ballPlayer.getId());
//        if (hget == null || StringUtils.isBlank(hget.toString())) {
//            userBean.setToken(authService.buildToken(ballPlayer));
//        } else {
//            userBean.setToken(hget.toString());
//        }
        userBean.setToken(authService.buildToken(ballPlayer));
        try {
            messageQueueService.putMessage(MessageQueueDTO.builder()
                    .type(MessageQueueDTO.TYPE_LOG_LOGIN)
                    .data(JsonUtil.toJson(MessageQueueLogin.builder()
                            .ballPlayer(ballPlayer)
                            .ip(ip)
                            .device(ua)
                            .build()))
                    .build());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //登录日志和修改账号登录IP
        return new BaseResponse<>(userBean);
    }

    @Override
    public BaseResponse newDeviceslogin(PlayerAuthLoginNewDevicesRequest req, HttpServletRequest request) {
        BallPlayer ballPlayer = basePlayerService.findByUsername(req.getUsername());
        //账号和手机是否匹配
//        if(!ballPlayer.getPhone().equals(req.getPhone())){
//            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
//                    ResponseMessageUtil.responseMessage("", "userNotMatchPhone"));
//        }
        //手机验证码判定成功后执行老登录逻辑
        String key = RedisKeyContant.PLAYER_PHONE_CODE + ballPlayer.getId();
        Object o = redisUtil.get(key);
        if (o == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
        }
        if (!o.equals(req.getPhoneCode())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeError"));
        }
        PlayerAuthLoginRequest authLoginRequest = new PlayerAuthLoginRequest();
        authLoginRequest.setCode(req.getCode());
        authLoginRequest.setPassword(req.getPassword());
        authLoginRequest.setUsername(req.getUsername());
        authLoginRequest.setVerifyKey(req.getVerifyKey());
//        redisUtil.del(key);
        return login(authLoginRequest, request, true);
    }

    @Override
    public BaseResponse getVerifyCode() throws IOException {
        //使用验证码工具类生成4位验证码
        String code = VerifyCodeUtils.generateVerifyCode(4);
        //将验证码放入redis
        String key = UUIDUtil.getUUID();
        redisUtil.set(RedisKeyContant.VERIFY_KEY + key, code, VerifyCodeUtils.TIME_OUT);
        //将验证码放入图片
        String imageBase64 = VerifyCodeUtils.getImageBase64(220, 60, code);
        Map<String, Object> data = new HashMap<>();
        data.put("verifyKey", key);
        data.put("time", VerifyCodeUtils.TIME_OUT);
        data.put("img", imageBase64);
        return BaseResponse.successWithData(data);
    }

    @Override
    public BaseResponse checkVerifyCode(String verifyKey, String code) {
        Object hget = redisUtil.get(RedisKeyContant.VERIFY_KEY + verifyKey);
        if (hget == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("code", "codeTimeOut"));
        }
        if (hget.equals(code)) {
            return BaseResponse.successWithMsg("");
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("code", "codeError"));
    }

//    @Override
//    public BaseResponse recharge(BallPlayer currentUser, Long money) {
//        BallPlayer player = basePlayerService.findOne(currentUser.getId());
//        Long realMoney = money * BigDecimalUtil.PLAYER_MONEY_UNIT;
//        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
//        BallPlayer edit = BallPlayer.builder()
//                .balance(player.getBalance() + realMoney)
//                //累计充值
//                .cumulativeTopUp((player.getCumulativeTopUp() == null ? 0 : player.getCumulativeTopUp()) + realMoney)
//                //线上充值金额
//                .onLineTopUp((player.getOnLineTopUp() == null ? 0 : player.getOnLineTopUp()) + realMoney)
//                .build();
//        boolean isFirst = false;
//        if (player.getFirstTopUp() == null || player.getFirstTopUp() == 0) {
//            //首充
//            edit.setFirstTopUp(realMoney);
//            edit.setFirstTopUpTime(TimeUtil.getNowTimeMill());
//            isFirst = true;
//        }
//        if (player.getMaxTopUp() == null || player.getMaxTopUp() == 0) {
//            // 最大充值金额
//            edit.setMaxTopUp(realMoney);
//        } else if (player.getMaxTopUp() < realMoney) {
//            // 本次比上次大
//            edit.setMaxTopUp(realMoney);
//        }
//        //次数+1
//        edit.setTopUpTimes(player.getTopUpTimes()==null?1:player.getTopUpTimes()+1);
//
//        if (systemConfig.getRegisterIfNeedVerificationCode() != null && systemConfig.getRegisterIfNeedVerificationCode() > 0) {
//            //累计打码量,查询配置,是否需要*比例
//            Double mul = BigDecimalUtil.mul(realMoney, BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
//            edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + mul.intValue());
//        } else {
//            edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + realMoney);
//        }
//        edit.setId(currentUser.getId());
//        edit.setVersion(currentUser.getVersion());
//        //查询是否有充值优惠
//        Long discountQuota = 0L;
//        BallDepositPolicy discount = depositPolicyService.findDiscount(isFirst);
//        if(discount!=null){
//            //优惠计算 金额*百分比
//            double div = BigDecimalUtil.div(discount.getPreferentialPer(), 100);
//            Double disc = BigDecimalUtil.mul(realMoney, div);
//            discountQuota = disc.longValue();
//            if(discountQuota>discount.getPreferentialTop()*BigDecimalUtil.PLAYER_MONEY_UNIT){
//                discountQuota = discount.getPreferentialTop()*BigDecimalUtil.PLAYER_MONEY_UNIT;
//            }
//            //余额+优惠
//            edit.setBalance(edit.getBalance()+discountQuota);
//        }
//        //VIP等级是否要提升
//        while (true) {
//            boolean b = basePlayerService.editAndClearCache(edit, player);
//            if (b) {
//                //插入账变 discountQuota
//                BallBalanceChange saveChange = BallBalanceChange.builder()
//                        .playerId(player.getId())
//                        .accountType(player.getAccountType())
//                        .userId(player.getUserId())
//                        .parentId(player.getSuperiorId())
//                        .username(player.getUsername())
//                        .superTree(player.getSuperTree())
//                        .initMoney(player.getBalance())
//                        .changeMoney(realMoney)
//                        .dnedMoney(edit.getBalance()-discountQuota)
//                        .createdAt(System.currentTimeMillis())
//                        .balanceChangeType(1)
//                        .orderNo(Long.parseLong(TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS))+ loggerRechargeService.getDayOrderNo())
//                        .discount(discountQuota)
//                        .build();
//                ballBalanceChangeService.insert(saveChange);
//                if(discountQuota>0){
//                    BallBalanceChange saveChangeDiscount = BallBalanceChange.builder()
//                            .playerId(player.getId())
//                            .accountType(player.getAccountType())
//                            .userId(player.getUserId())
//                            .parentId(player.getSuperiorId())
//                            .username(player.getUsername())
//                            .superTree(player.getSuperTree())
//                            .initMoney(edit.getBalance()-discountQuota)
//                            .changeMoney(discountQuota)
//                            .dnedMoney(edit.getBalance())
//                            .createdAt(System.currentTimeMillis())
//                            .balanceChangeType(19)
//                            .orderNo(saveChange.getOrderNo())
//                            .build();
//                    ballBalanceChangeService.insert(saveChangeDiscount);
//                }
//                messageQueueService.putMessage(MessageQueueDTO.builder()
//                        .type(MessageQueueDTO.TYPE_LOG_RECHARGE)
//                        .data(saveChange)
//                        .build());
//                messageQueueService.putMessage(MessageQueueDTO.builder()
//                        .type(MessageQueueDTO.TYPE_LOG_RECHARGE_LOG)
//                        .data(saveChange)
//                        .build());
//                return BaseResponse.successWithMsg("ok");
//            } else {
//                //更新失败再次更新
//                player = basePlayerService.findOne(currentUser.getId());
//                edit.setVersion(player.getVersion());
//                edit.setBalance(player.getBalance() + realMoney);
//            }
//        }
//    }

    @Override
    public BaseResponse recharge(BallPlayer player, BallLoggerRecharge loggerRecharge, PayBackDto payParamDto, BallPaymentManagement paymentManagement) throws JsonProcessingException {
        //真实支付金额
        Double mulReal = payParamDto.getAccountPractical().doubleValue();
        //1u=6.7换算成系统单位
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //TODO 汇率
        if (paymentManagement.getRate() != null) {
            double rate = Double.parseDouble(paymentManagement.getRate());
            mulReal = BigDecimalUtil.mul(mulReal, rate);
        } else {
            Double aDouble = Double.valueOf(systemConfig.getUsdtWithdrawPer());
            if (aDouble > 0) {
                mulReal = BigDecimalUtil.mul(mulReal, aDouble);
            }
        }
        //系统应加金额
        Long realMoneySys = mulReal.longValue();
        RechargeHanderDTO rechargeHander = rechargeHander(player, realMoneySys, systemConfig, loggerRecharge, paymentManagement);
        long totalDiscount = 0L;
        for (RechargeRebateDto item : rechargeHander.getDiscountQuota()) {
            totalDiscount += item.getDiscount();
        }
        rechargeHander.setTotalDiscount(totalDiscount);
        loggerRecharge.setMoneyDiscount(totalDiscount);
        loggerRecharge.setMoneySys(realMoneySys);
        loggerRecharge.setFirst(rechargeHander.isFirst()?1:0);
        loggerRecharge.setUpdatedAt(System.currentTimeMillis());
        if(StringUtils.isBlank(loggerRecharge.getOperUser())){
            loggerRecharge.setOperUser("sys");
        }
        boolean editRes = loggerRechargeService.edit(loggerRecharge);
        if (!editRes) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e50"));
        }
        BallLoggerRecharge edited = loggerRechargeService.findById(loggerRecharge.getId());
        afterRechargeSuccess(player, realMoneySys, rechargeHander, edited, systemConfig, paymentManagement);
        return BaseResponse.SUCCESS;
    }

    @Override
    public RechargeHanderDTO rechargeHander(BallPlayer player, long realMoneySys, BallSystemConfig systemConfig, BallLoggerRecharge loggerRecharge, BallPaymentManagement paymentManagement) {
        BallPlayer edit = BallPlayer.builder()
                .balance((player.getBalance() == null ? 0 : player.getBalance()) + realMoneySys)
                //累计充值
                .cumulativeTopUp((player.getCumulativeTopUp() == null ? 0 : player.getCumulativeTopUp()) + realMoneySys)
                //线上充值金额
                .onLineTopUp((player.getOnLineTopUp() == null ? 0 : player.getOnLineTopUp()) + realMoneySys)
                .build();
        boolean isFirst = false;
        boolean isSecond = false;
        if (player.getFirstTopUp() == null || player.getFirstTopUp() == 0) {
            //首充
            edit.setFirstTopUp(realMoneySys);
            edit.setFirstTopUpTime(loggerRecharge.getCreatedAt());
            isFirst = true;
        } else if (player.getSecondTopUp() == 0) {
            edit.setSecondTopUp(realMoneySys);
            edit.setSecondTopUpTime(loggerRecharge.getCreatedAt());
            isSecond = true;
        }
        if (player.getMaxTopUp() == null || player.getMaxTopUp() == 0) {
            // 最大充值金额
            edit.setMaxTopUp(realMoneySys);
        } else if (player.getMaxTopUp() < realMoneySys) {
            // 本次比上次大
            edit.setMaxTopUp(realMoneySys);
        }
        //次数+1
        edit.setTopUpTimes(player.getTopUpTimes() == null ? 1 : player.getTopUpTimes() + 1);

        if (systemConfig.getRegisterIfNeedVerificationCode() != null && systemConfig.getRegisterIfNeedVerificationCode() > 0) {
            //累计打码量,查询配置,是否需要*比例
            Double mul = BigDecimalUtil.mul(realMoneySys, BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
            edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + mul.intValue());
        } else {
            edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + realMoneySys);
        }

        edit.setId(player.getId());
        edit.setVersion(player.getVersion());
        //查询是否有充值优惠,首充优惠
        List<RechargeRebateDto> discount = depositPolicyService.findDiscount(loggerRecharge.getMoneyReal(), isFirst, isSecond, paymentManagement);
        for (RechargeRebateDto item : discount) {
            BallPlayerServiceImpl.rechargeDiscount(realMoneySys, item, paymentManagement);
        }
        return RechargeHanderDTO.builder()
                .discountQuota(discount)
                .first(isFirst)
                .edit(edit)
                .build();
    }

    @Override
    public BaseResponse recharge(BallPlayer player, BallLoggerRecharge loggerRecharge, PayNoticeDtoIN payParamDto, BallPaymentManagement paymentManagement) throws JsonProcessingException {
        //真实支付金额
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //系统应加金额
        Long realMoneySys = payParamDto.getPay_amount().longValue();
        //TODO 汇率
        Double rate = Double.valueOf(paymentManagement.getRate());
        if (rate > 0) {
            Double rmoney = BigDecimalUtil.mul(realMoneySys, rate);
            realMoneySys = rmoney.longValue();
        }

        RechargeHanderDTO rechargeHander = rechargeHander(player, realMoneySys, systemConfig, loggerRecharge, paymentManagement);
        long totalDiscount = 0L;
        for (RechargeRebateDto item : rechargeHander.getDiscountQuota()) {
            totalDiscount += item.getDiscount();
        }
        loggerRecharge.setMoneyDiscount(totalDiscount);
        loggerRecharge.setMoneySys(realMoneySys);
        loggerRecharge.setFirst(rechargeHander.isFirst()?1:0);
        loggerRecharge.setUpdatedAt(System.currentTimeMillis());
        if(StringUtils.isBlank(loggerRecharge.getOperUser())){
            loggerRecharge.setOperUser("sys");
        }

        boolean editRes = loggerRechargeService.edit(loggerRecharge);
        if (!editRes) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e50"));
        }
        BallLoggerRecharge edited = loggerRechargeService.findById(loggerRecharge.getId());
        afterRechargeSuccess(player, realMoneySys, rechargeHander, edited, systemConfig, paymentManagement);
        return BaseResponse.SUCCESS;
    }

    private void afterRechargeSuccess(BallPlayer player, Long realMoneySys, RechargeHanderDTO rechargeHander, BallLoggerRecharge edited, BallSystemConfig systemConfig, BallPaymentManagement paymentManagement) throws JsonProcessingException {
        while (true) {
            boolean b = basePlayerService.editAndClearCache(rechargeHander.getEdit(), player);
            if (b) {
                //插入账变 discountQuota
                BallBalanceChange saveChange = BallBalanceChange.builder()
                        .playerId(player.getId())
                        .accountType(player.getAccountType())
                        .userId(player.getUserId())
                        .parentId(player.getSuperiorId())
                        .username(player.getUsername())
                        .superTree(player.getSuperTree())
                        .initMoney(player.getBalance())
                        .changeMoney(realMoneySys)
                        //+充值金额
                        .dnedMoney(rechargeHander.getEdit().getBalance())
                        .createdAt(System.currentTimeMillis())
                        .balanceChangeType(edited.getType() == 1 ? 1 : 11)
                        .orderNo(edited.getOrderNo())
                        .build();
                ballBalanceChangeService.insert(saveChange);
                //优惠列表
                for (RechargeRebateDto item : rechargeHander.getDiscountQuota()) {
                    if (item.getDiscount() == 0) {
                        continue;
                    }
                    int changeType = 0;
                    //TODO 充值优惠类型同步 0邀请首充 1 首充 2 活动 3次充 4固定日 99返佣
                    switch (item.getDepositPolicyType()){
                        case 1:
                            changeType=24;
                            break;
                        case 2:
                            changeType=25;
                            break;
                        case 3:
                            changeType=26;
                            break;
                        case 4:
                            changeType=27;
                            break;
                        case 0:
                            changeType=28;
                            break;
                        default:
                            break;
                    }
                    //自动结算
                    if (item.isAutoSettlement()) {
                        player = basePlayerService.findById(player.getId());
                        while (true) {
                            BallPlayer rebateEdit = BallPlayer.builder()
                                    .version(player.getVersion())
                                    .balance(player.getBalance() + item.getDiscount())
                                    .build();
                            rebateEdit.setId(player.getId());
                            boolean b1 = basePlayerService.editAndClearCache(rebateEdit, player);
                            if (b1) {
                                //+充值优惠
                                BallBalanceChange saveChangeDiscount = BallBalanceChange.builder()
                                        .playerId(player.getId())
                                        .accountType(player.getAccountType())
                                        .userId(player.getUserId())
                                        .parentId(player.getSuperiorId())
                                        .username(player.getUsername())
                                        .superTree(player.getSuperTree())
                                        .initMoney(rechargeHander.getEdit().getBalance())
                                        .changeMoney(item.getDiscount())
                                        .dnedMoney(rechargeHander.getEdit().getBalance() + item.getDiscount())
                                        .createdAt(System.currentTimeMillis())
                                        .balanceChangeType(changeType)
                                        .orderNo(saveChange.getOrderNo())
                                        .build();
                                ballBalanceChangeService.insert(saveChangeDiscount);
                                //优惠 日志
                                BallLoggerRebate insert = BallLoggerRebate.builder()
                                        .accountType(player.getAccountType())
                                        .playerId(player.getId())
                                        .playerName(player.getUsername())
                                        .money(item.getDiscount())
                                        .moneyReal(realMoneySys)
                                        .payTypeOnff(paymentManagement.getPayTypeOnff())
                                        .payType(paymentManagement.getPayType())
                                        .rateUsdt(paymentManagement.getRate())
                                        .payId(item.getPayId())
                                        .moneyUsdt(edited.getMoneyReal())
                                        .rate(item.getRate().toString())
                                        .fixed(item.getFixed())
                                        .status(2)
                                        .orderNo(edited.getOrderNo())
                                        .superTree(player.getSuperTree())
                                        .type(item.getDepositPolicyType())
                                        .build();
//                                if(paymentManagement.getPayType()==1){
//                                    insert.setMoneyUsdt(edited.getMoneyReal());
//                                }
                                insert.setCreatedAt(edited.getUpdatedAt());
                                //上级代理
                                editRebateLoggerProxy(insert, player);
                                loggerRebateService.insert(insert);
                                //TG会员群消息
                                loggerRebateService.sendPlayerChat(insert,player);
                                break;
                            } else {
                                player = basePlayerService.findById(player.getId());
                            }
                        }
                    } else {
                        //不马上结算,保存订单
                        BallLoggerRebate insert = BallLoggerRebate.builder()
                                .accountType(player.getAccountType())
                                .playerId(player.getId())
                                .playerName(player.getUsername())
                                .money(item.getDiscount())
                                .moneyReal(realMoneySys)
                                .rate(item.getRate().toString())
                                .fixed(item.getFixed())
                                .payType(paymentManagement.getPayType())
                                .payId(item.getPayId())
                                .status(1)
                                .orderNo(edited.getOrderNo())
                                .superTree(player.getSuperTree())
                                .type(item.getDepositPolicyType())
                                .rateUsdt(paymentManagement.getRate())
                                .payTypeOnff(paymentManagement.getPayTypeOnff())
                                .build();
                        insert.setMoneyUsdt(edited.getMoneyReal());
//                        if(paymentManagement.getPayType()==1){
//                        }
                        insert.setCreatedAt(edited.getUpdatedAt());
                        editRebateLoggerProxy(insert, player);
                        loggerRebateService.insert(insert);
                    }
                }
                //充值返佣
                edited.setMoneyMin(rechargeHander.isFirst()?1:0);
                messageQueueService.startMessage(MessageQueueDTO.builder()
                        .type(MessageQueueDTO.TYPE_LOG_RECHARGE)
                        .data(JsonUtil.toJson(edited))
                        .build());
//                //充值成功
//                messageQueueService.putMessage(MessageQueueDTO.builder()
//                        .type(MessageQueueDTO.TYPE_LOG_RECHARGE_LOG)
//                        .data(JsonUtil.toJson(edited))
//                        .build());
                //是能提升等级
                messageQueueService.startMessage(MessageQueueDTO.builder()
                        .type(MessageQueueDTO.TYPE_LOG_RECHARGE_UP)
                        .data(JsonUtil.toJson(player))
                        .build());
                if (rechargeHander.isFirst()) {
                    //首充是否触发上级奖金
                    messageQueueService.startMessage(MessageQueueDTO.builder()
                            .type(MessageQueueDTO.TYPE_RECHARGE_PARENT_BONUS)
                            .data(JsonUtil.toJson(saveChange))
                            .build());
//                    //首充日志
//                    messageQueueService.putMessage(MessageQueueDTO.builder()
//                            .type(MessageQueueDTO.TYPE_RECHARGE_FIRST)
//                            .data(JsonUtil.toJson(edited))
//                            .build());
                }
                break;
            } else {
                //更新失败再次更新
                player = basePlayerService.findById(player.getId());
                rechargeHander = rechargeHander(player, realMoneySys, systemConfig, edited, paymentManagement);
            }
        }
    }

    private void editRebateLoggerProxy(BallLoggerRebate insert, BallPlayer finalPlayer1) {
        String superTree = finalPlayer1.getSuperTree();
        if (superTree.equals("0")) {
        } else if (superTree.equals("_")) {
        } else {
            String[] split = superTree.split("_");
            try {
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                insert.setTopUsername(superPlayer.getUsername());
                if (split.length == 2) {
                    insert.setFirstUsername(superPlayer.getUsername());
                } else if (split.length > 2) {
                    BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                    insert.setFirstUsername(firstPlayer.getUsername());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public BaseResponse withdrawal(BallPlayer currentUser, WithdrawalRequest withdrawalRequest) {
        BallPlayer player = basePlayerService.findById(currentUser.getId());
        if (StringUtils.isBlank(currentUser.getPhone())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneNotBind"));
        }
        if (StringUtils.isBlank(player.getPayPassword())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "originNotSet"));
        }
        //支付密码
        if (!player.getPayPassword().equals(PasswordUtil.genPasswordMd5(withdrawalRequest.getPayPwd()))) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "payPwdError"));
        }
        BallBankCard byPlayerId = null;
        if (withdrawalRequest.getType() == 2) {
            if (withdrawalRequest.getUsdtId() == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("usdtId", "usdtIdIsNull"));
            }
            BallVirtualCurrency usdt = virtualCurrencyService.findById(withdrawalRequest.getUsdtId());
            if (usdt == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("usdtId", "usdtIdIsNull"));
            }
        } else if(withdrawalRequest.getType()==1) {
            byPlayerId = bankCardService.findByPlayerId(player.getId());
            if (byPlayerId == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("bank", "bankNotSet"));
            }
        } else if(withdrawalRequest.getType()==3){
            BallSimCurrency byPlayerId1 = simCurrencyService.findByPlayerId(player.getId());
            if(byPlayerId1==null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("sim", "simNotSet"));
            }
        }
        //手机验证码
        String smsCodeKey = RedisKeyContant.PLAYER_PHONE_CODE + currentUser.getId();
        if (someConfig.getApiSwitch() == null) {
            Object o = redisUtil.get(smsCodeKey);
            if (o == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
            }
            if (!o.equals(withdrawalRequest.getCode())) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        int min = withdrawalRequest.getType() == 2 ? systemConfig.getUsdtWithdrawMin() : systemConfig.getWithdrawMin();
        Double reqMoney = Double.valueOf(withdrawalRequest.getMoney());
        if (reqMoney < min) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("money", "ltMinMoney"));
        }
        //是否达到每日最大提取数
        Integer todayCount = loggerWithdrawalService.todayCount(currentUser.getId());
        if (systemConfig.getEverydayWithdrawTimes() > 0 && systemConfig.getEverydayWithdrawTimes() <= todayCount) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("money", "dayMaxTimes"));
        }
        //是否达到打码量
        if (player.getNeedQr() > 0) {
            if (player.getCumulativeQr() < player.getNeedQr()) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("money", "cumulativeNotEnough"));
            }
        }

        // 并发问题处理
        Integer wiInterval = systemConfig.getWiInterval();
        String key = RedisKeyContant.PLAYER_WITHDRAWAL_LIMIT + player.getId();
        Object o = redisUtil.get(key);
        if(o!=null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("limit", "justMinsWi"));
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,wiInterval*60);
        if(incr>1){
            return BaseResponse.SUCCESS;
        }

        //实际扣除金额
        long money = Double.valueOf(BigDecimalUtil.mul(reqMoney, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue();
        long sourceMoney = Double.valueOf(BigDecimalUtil.mul(reqMoney, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue();
        //手续费
        Long commission = 0L;
        //手续费
        int commissionRate = 0;
        //usdt费率
//        double usdtRate = 0;
//        long usdtMoney = 0;
        //今天是否还有免费提现次数
        Integer everydayWithdrawFree = systemConfig.getEverydayWithdrawFree();
        boolean noFree = true;
        if (everydayWithdrawFree > 0) {
            //有免费次数，查询是否还有免费次数
            if (todayCount < everydayWithdrawFree) {
                //还有免费
                noFree = false;
            }
        }
        if (noFree) {
            //TODO 提现手续费 = 提现金额 *手续费,实际金额 = 提现金额-手续费
            commissionRate = withdrawalRequest.getType() !=2 ? systemConfig.getWithdrawalRate() : systemConfig.getUsdtWithdrawalRate();
            if (commissionRate > 0) {
                int rmin = withdrawalRequest.getType() !=2 ? systemConfig.getWithdrawalRateMin() : systemConfig.getUsdtWithdrawalRateMin();
                int rmax = withdrawalRequest.getType() !=2 ? systemConfig.getWithdrawalRateMax() : systemConfig.getUsdtWithdrawalRateMax();
                Double d = BigDecimalUtil.div(BigDecimalUtil.mul(reqMoney, commissionRate), 100);
                commission = d.longValue();
                if (commission < rmin * BigDecimalUtil.PLAYER_MONEY_UNIT) {
                    commission = rmin * BigDecimalUtil.PLAYER_MONEY_UNIT;
                }
                if (commission > rmax * BigDecimalUtil.PLAYER_MONEY_UNIT) {
                    commission = rmax * BigDecimalUtil.PLAYER_MONEY_UNIT;
                }
            }
        }
        money -= commission;
//        if(withdrawalRequest.getType()==2){
//            usdtRate = systemConfig.getUsdtWithdrawPer()/100d;
//            usdtMoney = Double.valueOf(BigDecimalUtil.div(money,usdtRate)).longValue();
//        }else{
//            //银行也有汇率，查询当前银行汇率
//        }
        if (player.getBalance() - sourceMoney < 0) {
            //余额不足
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "balanceNotEnough"));
        }

        while (true) {
            BallPlayer edit = BallPlayer.builder()
                    .balance(player.getBalance() - sourceMoney)
                    //TODO 提现冻结
                    .frozenWithdrawal((player.getFrozenWithdrawal() == null ? 0 : player.getFrozenWithdrawal()) + sourceMoney)
                    .version(player.getVersion())
                    .build();
//        firstReflect 首次提现
            edit.setId(currentUser.getId());
            boolean b = basePlayerService.editAndClearCache(edit, player);
            if (b) {
                //插入账变
                long orderNo = Long.parseLong(TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS)) + loggerWithdrawalService.getDayOrderNo();
                ballBalanceChangeService.insert(BallBalanceChange.builder()
                        .playerId(player.getId())
                        .accountType(player.getAccountType())
                        .userId(player.getUserId())
                        .parentId(player.getSuperiorId())
                        .username(player.getUsername())
                        .superTree(player.getSuperTree())
                        .initMoney(player.getBalance())
                        .changeMoney(-sourceMoney)
                        .dnedMoney(edit.getBalance())
//                        .remark("提现")
//                        .remark("withdrawal_self")
                        .createdAt(System.currentTimeMillis())
                        .balanceChangeType(2)
                        .frozenStatus(0)
                        .orderNo(orderNo)
                        .build());
                //插入提现记录
                BallLoggerWithdrawal insert = BallLoggerWithdrawal.builder()
                        .createdAt(System.currentTimeMillis())
                        .money(sourceMoney)
                        .commission(commission)
                        .rate(commissionRate)
//                        .usdtRate(String.valueOf(usdtRate))
//                        .usdtMoney(usdtMoney)
                        .playerName(player.getUsername())
                        .superTree(player.getSuperTree())
                        .orderNo(orderNo)
                        .playerId(player.getId())
                        .accountType(player.getAccountType())
                        .status(1)
                        .usdtId(withdrawalRequest.getUsdtId())
                        .type(withdrawalRequest.getType())
                        .ipAddr(withdrawalRequest.getIpAddr())
                        .build();
                if(withdrawalRequest.getType()==1){
                    insert.setToBank(byPlayerId.getCardNumber());
                    insert.setToBankAccount(byPlayerId.getCardName());
                }
                loggerWithdrawalService.insert(insert);
                BallPlayer finalPlayer = player;
                ThreadPoolUtil.exec(() -> {
                    BallLoggerWithdrawal edit1 = BallLoggerWithdrawal.builder()
                            .id(insert.getId())
                            .build();
                    String superTree = currentUser.getSuperTree();
                    boolean hasSuper = false;
                    if (superTree.equals("0")) {
                    } else if (superTree.equals("_")) {
                    } else {
                        String[] split = superTree.split("_");
                        try {
                            BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                            edit1.setTopUsername(superPlayer.getUsername());
                            hasSuper = true;
                            if (split.length == 2) {
                                edit1.setFirstUsername(superPlayer.getUsername());
                            } else if (split.length > 2) {
                                BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                                edit1.setFirstUsername(firstPlayer.getUsername());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (hasSuper) {
                        loggerWithdrawalService.edit(edit1);
                    }
                    webSocketManager.sendMessage(null,MessageResponse.builder()
                            .data("")
                            .type(MessageResponse.DEEP_TYPE_W)
                            .build());
                    //TG提醒
                    if(currentUser.getAccountType()==1){
                        return;
                    }
                    //统计返利
                    ReportDataRequest reportDataRequest = new ReportDataRequest();
                    reportDataRequest.setUserId(finalPlayer.getId());
                    reportDataRequest.setTime(-1);
                    BallLoggerRebate loggerRebate = loggerRebateService.statisDiscount(reportDataRequest);
                    if(StringUtils.isBlank(edit1.getTopUsername())){
                        // 上次提现,查询<本次id的第一条
                        BallLoggerWithdrawal last = loggerWithdrawalService.findLast(insert);
                        long days = 0;
                        String date = "--";
                        String lastMoney = "--";
                        if(last!=null){
                            days = (System.currentTimeMillis()-last.getCreatedAt())/TimeUtil.TIME_ONE_DAY;
                            date = TimeUtil.longToStringYmd(last.getCreatedAt(),TimeUtil.TIME_MM_DD);
                            lastMoney = BigDecimalUtil.toString(BigDecimalUtil.div(last.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT));
                        }
                        String message = MessageFormat.format("用户 {0} \n" +
                                        "总充值 {3} 总提现 {4} 存提差 {6} \n" +
                                        "顶级代理 无 余额 {10}\n" +
                                        "提现金额 {1} 提现方式 {2} \n" +
                                        "上次提款 {7}天前({8}) 提款金额 {9}\n"+
                                        "人工加款 {5}\n"+
                                        "优惠返利 {11}",
                                insert.getPlayerName(),
                                BigDecimalUtil.toString(BigDecimalUtil.div(insert.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                BallLoggerWithdrawal.getTypeString(insert.getType()),
                                BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getCumulativeTopUp(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getCumulativeReflect(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getArtificialAdd(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                BigDecimalUtil.toString(BigDecimalUtil.div((finalPlayer.getCumulativeTopUp()- finalPlayer.getCumulativeReflect()),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                String.valueOf(days),
                                date,
                                lastMoney,
                                BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getBalance(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                BigDecimalUtil.toString(BigDecimalUtil.div(loggerRebate.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT))
                        );
                        message+="\n首充时间 "+ (finalPlayer.getFirstTopUpTime().equals(0L)?"--":TimeUtil.longToStringYmd(finalPlayer.getFirstTopUpTime(),TimeUtil.TIME_YYYY_MM_DD));
                        apiService.tgNotice(message);
                    }else{
                        // 上次提现,查询<本次id的第一条
                        BallLoggerWithdrawal last = loggerWithdrawalService.findLast(insert);
                        long days = 0;
                        String date = "--";
                        String lastMoney = "--";
                        if(last!=null){
                            days = (System.currentTimeMillis()-last.getCreatedAt())/TimeUtil.TIME_ONE_DAY;
                            date = TimeUtil.longToStringYmd(last.getCreatedAt(),TimeUtil.TIME_MM_DD);
                            lastMoney = BigDecimalUtil.toString(BigDecimalUtil.div(last.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT));
                        }
                        Set<String> names = new HashSet<>();
                        List<BallAdmin> byPlayername = adminService.findByPlayername(edit1.getTopUsername());
                        if(!byPlayername.isEmpty()){
                            for(BallAdmin item:byPlayername){
                                if(names.contains(item.getTgName())){
                                    continue;
                                }
                                if(StringUtils.isBlank(item.getTgName())){
                                    // 没有配置TGname不发
                                    continue;
                                }
                                names.add(item.getTgName());
                                String message = MessageFormat.format("{0} \n" +
                                                "用户 {1} \n" +
                                                "总充值 {5} 总提现 {6} 存提差 {8} \n" +
                                                "顶级代理 {2} 余额 {12}\n" +
                                                "提现金额 {3} 提现方式 {4} \n" +
                                                "上次提款 {9}天前({10}) 提款金额 {11}\n"+
                                                "人工加款 {7}\n"+
                                                "优惠返利 {13}",
                                        StringUtils.isBlank(item.getTgName())?"":"@"+item.getTgName(),
                                        insert.getPlayerName(),
                                        edit1.getTopUsername(),
                                        ""+BigDecimalUtil.div(insert.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT),
                                        BallLoggerWithdrawal.getTypeString(insert.getType()),
                                        BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getCumulativeTopUp(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                        BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getCumulativeReflect(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                        BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getArtificialAdd(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                        BigDecimalUtil.toString(BigDecimalUtil.div((finalPlayer.getCumulativeTopUp()- finalPlayer.getCumulativeReflect()),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                        days,
                                        date,
                                        lastMoney,
                                        BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getBalance(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                        BigDecimalUtil.toString(BigDecimalUtil.div(loggerRebate.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT))
                                );
                                message+="\n首充时间 "+ (finalPlayer.getFirstTopUpTime().equals(0L)?"--":TimeUtil.longToStringYmd(finalPlayer.getFirstTopUpTime(),TimeUtil.TIME_YYYY_MM_DD));
                                apiService.tgNotice(message);
                            }
                        }else{
                            String message = MessageFormat.format("用户 {0} \n" +
                                            "总充值 {4} 总提现 {5} 存提差 {7} \n" +
                                            "顶级代理 {1} 余额 {11}\n" +
                                            "提现金额 {2} 提现方式 {3}\n" +
                                            "上次提款 {8}天前({9}) 提款金额 {10}\n"+
                                            "人工加款 {6}\n"+
                                            "优惠返利 {12}",
                                    insert.getPlayerName(),
                                    edit1.getTopUsername(),
                                    BigDecimalUtil.toString(BigDecimalUtil.div(insert.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                    BallLoggerWithdrawal.getTypeString(insert.getType()),
                                    BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getCumulativeTopUp(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                    BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getCumulativeReflect(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                    BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getArtificialAdd(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                    BigDecimalUtil.toString(BigDecimalUtil.div((finalPlayer.getCumulativeTopUp()- finalPlayer.getCumulativeReflect()),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                    days,
                                    date,
                                    BigDecimalUtil.toString(BigDecimalUtil.div(finalPlayer.getBalance(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                    BigDecimalUtil.toString(BigDecimalUtil.div(last.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT)),
                                    BigDecimalUtil.toString(BigDecimalUtil.div(loggerRebate.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT))
                                    );
                            message+="\n首充时间 "+ (finalPlayer.getFirstTopUpTime().equals(0L)?"--":TimeUtil.longToStringYmd(finalPlayer.getFirstTopUpTime(),TimeUtil.TIME_YYYY_MM_DD));
                            apiService.tgNotice(message);
                        }
                    }
                });
//                redisUtil.del(smsCodeKey);
                return BaseResponse.successWithMsg("ok~");
            } else {
                //更新失败再次判定余额是否足够,
                player = basePlayerService.findById(currentUser.getId());
                if (player.getBalance() - money < 0 || player.getBalance() < systemConfig.getWithdrawMin()) {
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "balanceNotEnough"));
                }
            }
        }
    }

    @Override
//    @Cacheable(cacheNames = REDIS_DATA_CENTER, key = "#currPlayer.getId()+'_'+#dataCenterRequest.getTime()+'_'+#dataCenterRequest.getUsername()", unless = "#result==null")
    public Map<String, Object> dataCenter(BallPlayer currPlayer, DataCenterRequest dataCenterRequest) {
        if (!StringUtils.isBlank(dataCenterRequest.getUsername())) {
            BallPlayer byUsername = basePlayerService.findByUsername(dataCenterRequest.getUsername());
            if (!byUsername.getSuperTree().contains(currPlayer.getSuperTree())) {
                Map<String, Object> data = new HashMap<>();
                //没有查询到用户返回全0
                data.put("playerCount", 0);
                data.put("netProfit", 0);
                data.put("totalBalance", 0);
                data.put("totalRecharge", 0);
                data.put("totalWithdrawal", 0);
                data.put("cumulativeWinning", 0);
                data.put("newPlayer", 0);
                data.put("totalBetBalance", 0);
                data.put("totalBetPlayer", 0);
                data.put("playerActive", 0);
                data.put("playerOffline", 0);
                data.put("cumulativeDiscount", 0);
                data.put("cumulativeActivity", 0);
            } else {
                currPlayer = byUsername;
            }
        }

        // 统计: 团队余额(all) 团队充值(time) 团队提现(time) 净利润(time)
        // 团队人数(all) 新增注册 投注金额(time) 投注人数(time)
        // 中奖金额(time) 活动奖励(time) 活跃人数 未登录人数 优惠金额(time)
        Map<String, Object> data = new HashMap<>();
        QueryWrapper<BallPlayer> queryWrapper = new QueryWrapper();
        queryWrapper.likeRight("super_tree", currPlayer.getSuperTree() + currPlayer.getId() + "\\_");
//        if (!StringUtils.isBlank(dataCenterRequest.getUsername())) {
//            queryWrapper.eq("username", dataCenterRequest.getUsername());
//        }
        List<BallPlayer> list = list(queryWrapper);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(currPlayer);
        data.put("playerCount", list.size());
        long totalBalance = 0L;
        long totalRecharge = 0L;
        long totalWithdrawal = 0L;
        long cumulativeWinning = 0L;
        int newPlayer = 0;
        long totalBetBalance = 0L;
        int playerActive = 0;
        int playerOffline = 0;
        long cumulativeDiscount = 0L;
        long cumulativeActivity = 0L;
        List<Long> ids = new ArrayList<>();
        for (BallPlayer player : list) {
            totalBalance += player.getBalance();
            //2天不在线算未上线人数
            int day = caseDayCount(dataCenterRequest.getTime());
            if (player.getTheNewLoginTime() == 0) {
                //未登录
                playerOffline++;
            } else {
                //活跃
                if (System.currentTimeMillis() - player.getTheNewLoginTime() < TimeUtil.TIME_ONE_DAY) {
                    playerActive++;
                }
            }
            //新增按注册时间和条件过滤，比如查今天的话，就是今天注册的，
            if (dataCenterRequest.getTime() == null) {
                newPlayer++;
            } else {
                if (dataCenterRequest.getTime() == 2) {
                    // 昨天的话只算昨天注册,注册时间是大于昨天开始，小于昨天结束
                    if (player.getCreatedAt() > TimeUtil.getBeginDayOfYesterday().getTime()
                            && player.getCreatedAt() < TimeUtil.getEndDayOfYesterday().getTime()) {
                        newPlayer++;
                    }
                } else if (dataCenterRequest.getTime() == 1) {
                    // 今天的话只算今天注册,注册时间是大于今天开始，小于今天结束
                    if (player.getCreatedAt() > TimeUtil.getDayBegin().getTime()
                            && player.getCreatedAt() < TimeUtil.getDayEnd().getTime()) {
                        newPlayer++;
                    }
                } else {
                    if (System.currentTimeMillis() - player.getCreatedAt() < TimeUtil.TIME_ONE_DAY * day) {
                        newPlayer++;
                    }
                }
            }
            ids.add(player.getId());
        }
        //统计账变数据
        List<BallBalanceChange> balanceChanges = getBallBalanceChanges(dataCenterRequest, ids);
        Set<Long> betCount = new HashSet<>();
        for (BallBalanceChange balanceChange : balanceChanges) {
            switch (balanceChange.getBalanceChangeType()) {
                case 1:
                case 11:
                case 6:
                    totalRecharge += balanceChange.getChangeMoney();
                    break;
                case 2:
                    totalWithdrawal += Math.abs(balanceChange.getChangeMoney());
                    break;
                case 4:
                    cumulativeWinning += balanceChange.getChangeMoney();
                    break;
                case 3:
                    totalBetBalance += Math.abs(balanceChange.getChangeMoney());
                    betCount.add(balanceChange.getPlayerId());
                    break;
                case 18:
                    cumulativeActivity += balanceChange.getChangeMoney();
                    break;
                case 15:
                case 19:
                    cumulativeDiscount += balanceChange.getChangeMoney();
                    break;
                default:
                    break;
            }
        }
        //净利润 = 累计中奖-累计投注
        data.put("netProfit", cumulativeWinning - totalBetBalance);
        data.put("totalBalance", totalBalance);
        data.put("totalRecharge", totalRecharge);
        data.put("totalWithdrawal", totalWithdrawal);
        data.put("cumulativeWinning", cumulativeWinning);
        data.put("newPlayer", newPlayer);
        data.put("totalBetBalance", totalBetBalance);
        data.put("totalBetPlayer", betCount.size());
        data.put("playerActive", playerActive);
        data.put("playerOffline", playerOffline);
        data.put("cumulativeDiscount", cumulativeDiscount);
        data.put("cumulativeActivity", cumulativeActivity);
        return data;
    }

    private int caseDayCount(Integer time) {
        if (time == null) {
            return 0;
        }
        switch (time) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 7;
            case 4:
                return 10;
            case 5:
                return 30;
            default:
                break;
        }
        return 0;
    }

    @Override
//    @Cacheable(cacheNames = REDIS_DATA_CENTER_DETAIL, key = "#player.getId()+'_'+#dataCenterRequest.getTime()", unless = "#result==null")
    public List<Map<String, Object>> dataCenterDetail(BallPlayer player, DataCenterRequest dataCenterRequest) {
        Map<Integer, Map<String, Object>> data = new HashMap<>();
        // 下级层级 新增 充值 提现
        QueryWrapper<BallPlayer> queryWrapper = new QueryWrapper();
        queryWrapper.likeRight("super_tree", player.getSuperTree() + player.getId() + "\\_");
        BallBalanceChangeServiceImpl.queryCaseTime(dataCenterRequest, queryWrapper);
        //要查询所有账号,不能按时间,查所有账号再按时间过滤
        List<BallPlayer> list = list(queryWrapper);
        if (list == null || list.isEmpty()) {
            return null;
        }
        //下级层级 新增 充值 提现
        //按注册过滤出来下级
        for (BallPlayer p : list) {
            String substring = p.getSuperTree().substring(player.getSuperTree().length());
            String[] level = substring.split("_");
            Map<String, Object> item = getItem(data, level.length);
            if (item.get("levelType") == null) {
                item.put("levelType", level.length);
                item.put("newPlayer", 0);
                item.put("recharge", 0);
                item.put("cumulativeReflect", 0);
            }
            Object newPlayer = item.get("newPlayer");
            item.put("newPlayer", Integer.parseInt(newPlayer.toString()) + 1);
            if (p.getCumulativeTopUp() > 0) {
                Object recharge = item.get("recharge");
                item.put("recharge", Integer.parseInt(recharge.toString()) + 1);
            }
            if (p.getCumulativeReflect() > 0) {
                Object cumulativeReflect = item.get("cumulativeReflect");
                item.put("cumulativeReflect", Integer.parseInt(cumulativeReflect.toString()) + 1);
            }
        }
        return new ArrayList<>(data.values());
    }

    @Override
    public SearchResponse<BallPlayer> searchSub(SubPlayersRequest queryParam, BallPlayer currPlayer, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPlayer> response = new SearchResponse<>();

        Page<BallPlayer> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();

        query.select("id", "username", "balance", "the_new_login_time",
                "status");
        query.likeRight("super_tree", currPlayer.getSuperTree() + currPlayer.getId() + "\\_");
        if (!StringUtils.isBlank(queryParam.getUsername())) {
            query.eq("username", queryParam.getUsername());
        }
        if (queryParam.getTime() != null) {
            switch (queryParam.getTime()) {
                case 2:
                    query.le("the_new_login_time", TimeUtil.getDayEnd().getTime() - 3 * TimeUtil.TIME_ONE_DAY);
                    break;
                case 3:
                    query.le("the_new_login_time", TimeUtil.getDayEnd().getTime() - 7 * TimeUtil.TIME_ONE_DAY);
                    break;
                default:
                    break;
            }
        }

        IPage<BallPlayer> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        return response;
    }

    @Override
    public BaseResponse bindBank(BallPlayer currentUser, BindBankCardRequest bindBankCardRequest) {
        if (StringUtils.isBlank(currentUser.getPhone())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneNotBind"));
        }
        BallBankCard playerBankCard = bankCardService.findByPlayerId(currentUser.getId());
        if (playerBankCard != null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bankCardExists"));
        }
        BallBankCard hasCard = bankCardService.findByCardNo(bindBankCardRequest.getCardNumber());
        // 此卡有人绑定,则不能绑定
        if (hasCard != null && hasCard.getPlayerId()!=null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bankCardExists"));
        }else if(hasCard!=null && hasCard.getHasWithdrawal()==1){
            //有人用卡提现成功过,不能绑定
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bankCardExists"));
        }

        if (!bindBankCardRequest.getCardNumber().equals(bindBankCardRequest.getCardNumberTwice())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("cardNumberTwice", "cardNumberTwiceDiff"));
        }
        if (StringUtils.isBlank(currentUser.getPayPassword())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "originNotSet"));
        }
        if (!currentUser.getPayPassword().equals(PasswordUtil.genPasswordMd5(bindBankCardRequest.getPayPwd()))) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "payPwdError"));
        }
        String key = RedisKeyContant.PLAYER_PHONE_CODE + currentUser.getId();
        Object o = redisUtil.get(key);
//        redisUtil.del(key);
        if(someConfig.getApiSwitch()==null){
            if (o == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
            }
            if (!o.equals(bindBankCardRequest.getCode())) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }

        //防止并发
        String key1 = RedisKeyContant.PLAYER_PAY_CALLBACK + bindBankCardRequest.getCardNumber();
        Object o1 = redisUtil.get(key1);
        if(o1!=null){
            return BaseResponse.successWithMsg("ok");
        }
        long incr = redisUtil.incr(key1, 1);
        redisUtil.expire(key1,3);
        if(incr>1){
            return BaseResponse.successWithMsg("ok");
        }

        // 情况1,如果卡号不存在,增加新卡
        // 情况2,如果卡号存在,则换绑为自己
        boolean bindSuccess = false;
        BallBank bank = bankService.findById(bindBankCardRequest.getBankId());
        BallBankCard save = BallBankCard.builder()
                .cardName(bindBankCardRequest.getCardName())
                .cardNumber(bindBankCardRequest.getCardNumber())
                .bankName(bank.getBankCname())
//                .bankId(bank.getId())
                .backEncoding(bindBankCardRequest.getBackCode())
                .province(bindBankCardRequest.getProvince())
                .city(bindBankCardRequest.getCity())
                .subBranch(bindBankCardRequest.getSubBranch())
                .identityCard(bindBankCardRequest.getIdentityCard())
                .phone(bindBankCardRequest.getPhone())
                .status(1)
                .playerId(currentUser.getId())
                .userId(currentUser.getUserId())
                .username(currentUser.getUsername())
                .statusCheck(0)
                .country(bindBankCardRequest.getCountry())
                .build();
        if(hasCard==null){
            save.setCreatedAt(System.currentTimeMillis());
            bindSuccess = bankCardService.insert(save);
        }else{
            save.setId(hasCard.getId());
            save.setUpdatedAt(System.currentTimeMillis());
            bindSuccess = bankCardService.editById(save);
        }

        if (bindSuccess) {
            BallLoggerBindCard logger = BallLoggerBindCard.builder()
                    .backEncoding(save.getBackEncoding())
//                        .bankId(save.getBankId())
                    .bankName(save.getBankName())
                    .cardName(save.getCardName())
                    .cardNumber(save.getCardNumber())
                    .city(save.getCity())
                    .country(save.getCountry())
                    .identityCard(save.getIdentityCard())
                    .phone(save.getPhone())
                    .playerId(save.getPlayerId())
                    .province(save.getProvince())
                    .subBranch(save.getSubBranch())
                    .username(save.getUsername())
                    .build();
            logger.setCreatedAt(System.currentTimeMillis());
            loggerBindCardService.insert(logger);
            return BaseResponse.successWithMsg("ok");
        }else{
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bindCardFailed"));
        }
    }

    @Override
    public void clearDayActitiy() {
        redisUtil.delKeys(RedisKeyContant.PLAYER_ACTIVITY + "*");
        redisUtil.delKeys(RedisKeyContant.PLAYER_PHONE_SMS_COUNT + "*");
    }

    @Override
    public BaseResponse withdrawalPre(BallPlayer currentUser) {
//        Map<String,Object> data = new HashMap<>();
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //USDT相关
        //转usdt汇率
//        data.put("usdtWithdrawPer",BigDecimalUtil.div(systemConfig.getUsdtWithdrawPer(),100,2));
        //手续费
        //提现列表
        List<BallWithdrawManagement> byAll = withdrawManagementService.findAll();
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (BallWithdrawManagement item : byAll) {
//            if(item.getCountry()!=null && !item.getCountry().equals(currentUser.getLoginContry())){
//                continue;
//            }
            Map<String, Object> dmap = new HashMap<>();
            //免手续费次数
            dmap.put("everydayWithdrawFree", systemConfig.getEverydayWithdrawFree());
            //最大提现次数
            dmap.put("everydayWithdrawTimes", systemConfig.getEverydayWithdrawTimes());
            //今日提现
            dmap.put("withdrawalToday", loggerWithdrawalService.todayCount(currentUser.getId()));
            if (item.getType() == 1 || item.getType() == 3) {
                //手续费
                dmap.put("withdrawalRate", BigDecimalUtil.div(systemConfig.getWithdrawalRate(), 100, 2));
                //最大提现金额
                dmap.put("withdrawMax", systemConfig.getWithdrawMax());
                //最小提现金额
                dmap.put("withdrawMin", systemConfig.getWithdrawMin());
                //最大提现手续费
                dmap.put("withdrawalRateMax", systemConfig.getWithdrawalRateMax());
                //最小提现手续费
                dmap.put("withdrawalRateMin", systemConfig.getWithdrawalRateMin());
                List<RateConfigs> rateConfigs = null;
                boolean hasRate = false;
                try {
                    rateConfigs = JsonUtil.fromJsonToList(systemConfig.getEuroRate(), RateConfigs.class);
                    for(RateConfigs item1:rateConfigs){
                        //查找匹配玩家的汇率
                        String[] split = item1.getAreaCode().split(",");
                        List<String> strings = Arrays.asList(split);
                        if(strings.contains(currentUser.getAreaCode())){
                            hasRate = true;
                            dmap.put("rate", item1.getRate());
                            dmap.put("currencySymbol", item1.getName());
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(!hasRate){
                    dmap.put("rate", systemConfig.getUsdtWithdrawPer());
                    dmap.put("currencySymbol", "USDT");
                }
            } else if (item.getType() == 2) {
                dmap.put("withdrawalRate", BigDecimalUtil.div(systemConfig.getUsdtWithdrawalRate(), 100, 2));
                //最大提现金额
                dmap.put("withdrawMax", systemConfig.getUsdtWithdrawMax());
                //最小提现金额usdtW
                dmap.put("withdrawMin", systemConfig.getUsdtWithdrawMin());
                //最大提现手续费usdtW
                dmap.put("withdrawalRateMax", systemConfig.getUsdtWithdrawalRateMax());
                //最小提现手续费usdtW
                dmap.put("withdrawalRateMin", systemConfig.getUsdtWithdrawalRateMin());
                dmap.put("rate", systemConfig.getUsdtWithdrawPer());
                dmap.put("currencySymbol", "USDT");
            }
            dmap.put("img", item.getIamgeUrl());
            dmap.put("type", item.getType());
            dmap.put("name", item.getName());
            responseList.add(dmap);
        }
//        data.put("withdrawalList",responseList);
        return BaseResponse.successWithData(responseList);
    }

    @Override
    public BaseResponse rechargePre(BallPlayer currentUser, Double money, Long payId) {
//        String key = RedisKeyContant.PLAYER_PAY_ORDER+currentUser.getUsername();
//        Object cache = redisUtil.get(key);
//        if(cache!=null){
//            BallLoggerRecharge loggerRecharge=null;
//            try {
//                loggerRecharge = JsonUtil.fromJson(cache.toString(), BallLoggerRecharge.class);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return BaseResponse.failedWithData(BaseResponse.PAY_UNFINISH,loggerRecharge.getPayUrl());
//        }
        Long orderId = Long.parseLong(TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS)) + loggerRechargeService.getDayOrderNo();
        BallPaymentManagement payType = paymentManagementService.findById(payId);
        if (payType == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "payTypeDisabled"));
        }
        //是否维护
        if (payType.getUnhold() == 1) {
            List<Map<String, Object>> maps = ResponseMessageUtil.responseMessage("unhold", "payUnhold");
            return BaseResponse.failedWithData(BaseResponse.SMS_UNHOLD, StringUtils.isBlank(payType.getUnholdMessage()) ? "invalid" : payType.getUnholdMessage());
        }
        if (!StringUtils.isBlank(payType.getMinMax())) {
            String[] split = payType.getMinMax().split("-");
            Integer min = Integer.parseInt(split[0]);
            Integer max = Integer.parseInt(split[1]);
            if (money < min || money > max) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "payMoneyTooMinOrMax"));
            }
        }
        //单位时间内是否拉起过多
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        String key = RedisKeyContant.PLAYER_PAY_MINMAX+currentUser.getId();
        Object o = null;
        if(systemConfig.getReMax()!=null&&systemConfig.getReMax()>0){
            o = redisUtil.get(key);
            if(o!=null){
                int count = Integer.parseInt(o.toString());
                if(count>=systemConfig.getReMax()){
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "payFrequently"));
                }
            }
        }
        BaseResponse baseResponse = null;
        //TODO 新支付
        if ("/pay/callback/1".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getUsdtPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/2".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getINPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/4".equals(payType.getUstdCallbackPath())) {
            baseResponse = getChaPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/6".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getInFastPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/8".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getIn3PayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/10".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getWowPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/12".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getAllPayPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/14".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getTnzPayPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/16".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getMetaPayPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/18".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getMetaggPayPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/20".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getWebPayPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/22".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getXdPayPayUrl(currentUser, money, orderId, payType);
        }
        if ("/pay/callback/24".equals(payType.getUstdCallbackPath())) {
            baseResponse =  getMpPayPayUrl(currentUser, money, orderId, payType);
        }
        if(baseResponse!=null&&baseResponse.getCode().equals(StatusCodes.OK)){
            //缓存拉起次数
            if(o!=null){
                int count = Integer.parseInt(o.toString());
                long expire = redisUtil.getExpire(key);
                redisUtil.set(key,count+1,expire);
            }else{
                redisUtil.set(key,1,TimeUtil.TIME_ONE_MIN/1000*systemConfig.getReTime());
            }
            return baseResponse;
        }else if(baseResponse==null){
            //TODO 其它地址暂时无法使用
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "payTypeNotUse"));
        }else{
            return baseResponse;
        }
    }

    private BaseResponse getMpPayPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceMP.requestPayUrl(payType, PayParamDtoFast.builder()
                .orderNo(orderId.toString())
                .amount(money.toString())
                .username(currentUser.getUserId().toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getXdPayPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceXD.requestPayUrl(payType, PayParamDtoFast.builder()
                .orderNo(orderId.toString())
                .amount(money.toString())
                .username(currentUser.getUserId().toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getWebPayPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
//生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceWEB.requestPayUrl(payType, PayParamDtoFast.builder()
                .orderNo(orderId.toString())
                .amount(money.toString())
                .username(currentUser.getUserId().toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getMetaggPayPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceMETAGG.requestPayUrl(payType, PayParamDtoTnz.builder()
                .orderNo(orderId.toString())
                .amount(money.toString())
                .username(currentUser.getUserId().toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);
        return BaseResponse.successWithData(map);
    }

    private BaseResponse getMetaPayPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceMETA.requestPayUrl(payType, PayParamDtoTnz.builder()
                .orderNo(orderId.toString())
                .amount(money.toString())
                .username(currentUser.getUserId().toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getTnzPayPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceTNZ.requestPayUrl(payType, PayParamDtoTnz.builder()
                .orderNo(orderId.toString())
                .amount(money.toString())
                .username(currentUser.getUsername())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getAllPayPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceAllPay.requestPayUrl(payType, PayRequestDtoCHA.builder()
                .orderNo(orderId.toString())
                .orderAmount(money.toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getWowPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceWOW.requestPayUrl(payType, PayRequestDtoCHA.builder()
                .orderNo(orderId.toString())
                .orderAmount(money.toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getIn3PayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                .status(1)
                .type(payType.getPayTypeOnff())
                .createdAt(System.currentTimeMillis())
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceIn3.requestPayUrl(payType, PayRequestDto3.builder()
                .orderNo(orderId.toString())
                .orderAmount(money.toString())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getInFastPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                .status(1)
                .type(payType.getPayTypeOnff())
                .createdAt(System.currentTimeMillis())
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceFAST.requestPayUrl(payType, PayParamDtoFast.builder()
                .orderNo(orderId.toString())
                .amount(money.toString())
                .username(currentUser.getUsername())
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getChaPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceCHA.requestPayUrl(payType, PayRequestDtoCHA.builder()
                .orderNo(orderId.toString())
                .orderAmount(money.toString())
                .phone("123456789")
//                .phone(currentUser.getPhone().replaceFirst(currentUser.getAreaCode(),""))
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getINPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayServiceIN.requestPayUrl(payType, PayRequestDtoIN.builder()
                .orderNo(orderId.toString())
                .orderAmount((int) (money * BigDecimalUtil.PLAYER_MONEY_UNIT))
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        Map map = new HashMap();
        map.put("UrlAddress", payUrl);
        build.setPayUrl(map);
        rechargeEditSuperName(currentUser, build);

        return BaseResponse.successWithData(map);
    }

    private BaseResponse getUsdtPayUrl(BallPlayer currentUser, Double money, Long orderId, BallPaymentManagement payType) {
        //生成充值订单,缓存3600秒
        BallLoggerRecharge build = BallLoggerRecharge.builder()
                .playerId(currentUser.getId())
                .accountType(currentUser.getAccountType())
                .username(currentUser.getUsername())
                .superTree(currentUser.getSuperTree())
                .userId(currentUser.getUserId())
                .money(Double.valueOf(BigDecimalUtil.mul(money, BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue())
                .orderNo(orderId)
                //记录原金额
                .status(1)
                .type(payType.getPayTypeOnff())
                //优惠
                .createdAt(System.currentTimeMillis())
                //充值渠道
                .payName(payType.getName())
                .payId(payType.getId())
                .ip(currentUser.getTheNewIp())
                .country(currentUser.getLoginContry())
                .phone(currentUser.getAreaCode()+currentUser.getPhone())
                .build();
        //请求充值地址
        String payUrl = playerPayService.requestPayUrl(payType, PayParamDto.builder()
                .accountOrders(Double.valueOf(money).floatValue())
                .backUrl(payType.getUstdCallback())
                .platformOrder(String.valueOf(orderId))
                .username(currentUser.getUsername())
                .rechargeType(PayParamDto.PAY_TYPE_USDT)
                .build());
        if (StringUtils.isBlank(payUrl)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
        }
        try {
            Map map = JsonUtil.fromJson(payUrl, Map.class);
            build.setPayUrl(map);
//            loggerRechargeService.insert(build);
            rechargeEditSuperName(currentUser, build);
            return BaseResponse.successWithData(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "rechargeUrlError"));
    }

    private void rechargeEditSuperName(BallPlayer currentUser, BallLoggerRecharge build) {
        ThreadPoolUtil.exec(() -> {
            String superTree = currentUser.getSuperTree();
            if (superTree.equals("0")) {
            } else if (superTree.equals("_")) {
            } else {
                String[] split = superTree.split("_");
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                build.setTopUsername(superPlayer.getUsername());
                if (split.length == 2) {
                    build.setFirstUsername(superPlayer.getUsername());
                } else if (split.length > 2) {
                    BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                    build.setFirstUsername(firstPlayer.getUsername());
                }
            }
            loggerRechargeService.insert(build);
        });
    }

    @Override
    public BaseResponse rechargeCancel(BallPlayer currentUser) {
        String key = RedisKeyContant.PLAYER_PAY_ORDER + currentUser.getUsername();
        redisUtil.del(key);
        return BaseResponse.SUCCESS;
    }

    @Override
    public BaseResponse getPlayerServices(BallPlayer currentUser) {
        try {
            BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
            String url = systemConfig.getServerUrl();
            Map<String, Object> data = new HashMap<>();
            url = url.replace("{0}", Base64Util.encrypt(currentUser.getUserId().toString()));
            url = url.replace("{1}", currentUser.getUsername());
            data.put("serviceAddr", url);
            return BaseResponse.successWithData(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "noServiceAddr"));
        }
    }

    @Override
    public BaseResponse getPlayerServices() {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        Map<String, Object> data = new HashMap<>();
        data.put("serviceAddr", systemConfig.getServerUrlTmp());
        return BaseResponse.successWithData(data);
    }

    @Override
    public BaseResponse getPhoneCode(PlayerBindPhoneCodeRequest bindPhoneCodeRequest, BallPlayer currentUser) {
        String maxSmsKey = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + bindPhoneCodeRequest.getPhone();
        Object o = redisUtil.get(maxSmsKey);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        String maxSmsKeyName = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + currentUser.getUsername();
        o = redisUtil.get(maxSmsKeyName);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        //TODO 给指定手机号发送验证码
        String s = VerifyCodeUtils.generateVerifyCode(4);
        String key = RedisKeyContant.PLAYER_PHONE_CODE + currentUser.getId();
//        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
//        redisUtil.set(key, s, TimeUtil.TIME_ONE_MIN * systemConfig.getSmsInterval() / 1000);
        BaseResponse response = smsService.sendSms(currentUser.getUsername(), bindPhoneCodeRequest.getPhone(), s, key);
        return response;
    }

    @Override
    public BaseResponse bindPhone(PlayerBindPhoneRequest playerBindPhoneRequest, BallPlayer currentUser) {
        //手机号是否已注册
        if (!StringUtils.isBlank(playerBindPhoneRequest.getAreaCode()) && !StringUtils.isBlank(playerBindPhoneRequest.getPhone())) {
            BallPlayer byPhone = basePlayerService.findByPhone(playerBindPhoneRequest.getAreaCode(), playerBindPhoneRequest.getPhone());
            if(byPhone!=null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("phone", "phoneExsit"));
            }
        }
        if(someConfig.getApiSwitch()==null){
            String key =RedisKeyContant.PLAYER_PHONE_CODE + currentUser.getId();
            Object o = redisUtil.get(key);
//            redisUtil.del(key);
            if (o == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
            }
            if (!o.equals(playerBindPhoneRequest.getCode())) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }
        BallPlayer edit = BallPlayer.builder()
                .phone(playerBindPhoneRequest.getPhone())
                .areaCode(playerBindPhoneRequest.getAreaCode())
                .build();
        edit.setId(currentUser.getId());
        boolean b = basePlayerService.editAndClearCache(edit, currentUser);
        return b ? BaseResponse.successWithMsg("ok") : BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "phoneBindError"));
    }

    @Override
    public BaseResponse phoneChangePwd(PlayerPhoneChangePwdRequest phoneChangePwdRequest) {
        String key = RedisKeyContant.PLAYER_PHONE_CODE + phoneChangePwdRequest.getPhone();
        Object o = redisUtil.get(key);
        if (o == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
        }
        if (!o.equals(phoneChangePwdRequest.getCode())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeError"));
        }
        if (!phoneChangePwdRequest.getNewPwd().equals(phoneChangePwdRequest.getTwicePwd())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "confirmedError"));
        }
        BallPlayer byPhone = basePlayerService.findByUsername(phoneChangePwdRequest.getUsername());
        if (byPhone == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneChangePwdError"));
        }
        BallPlayer edit = BallPlayer.builder()
                .password(PasswordUtil.genPasswordMd5(phoneChangePwdRequest.getNewPwd()))
                .build();
        edit.setId(byPhone.getId());
        boolean b = basePlayerService.editAndClearCache(edit, byPhone);
        if (b) {
//            redisUtil.del(key);
            //登录token删除
            authService.clearAuth(byPhone);
        }
        return b ? BaseResponse.SUCCESS : BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "phoneChangePwdError"));
    }

    @Override
    public BaseResponse getPhoneCode(BallPlayer currentUser) {
        if (StringUtils.isBlank(currentUser.getPhone())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneNotBind"));
        }
        String maxSmsKey = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + currentUser.getPhone();
        Object o = redisUtil.get(maxSmsKey);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        String maxSmsKeyName = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + currentUser.getUsername();
        o = redisUtil.get(maxSmsKeyName);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        //TODO 给绑定手机号发送验证码
        String s = VerifyCodeUtils.generateVerifyCode(4);
        String key = RedisKeyContant.PLAYER_PHONE_CODE + currentUser.getId();
//        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
//        redisUtil.set(key, s, TimeUtil.TIME_ONE_MIN * systemConfig.getSmsInterval() / 1000);
        BaseResponse response = smsService.sendSms(currentUser.getUsername(), currentUser.getPhone(), s, key);
        return response;
    }

    private boolean smsIsMax(Object o) {
        if (o != null) {
            int i = Integer.parseInt(o.toString());
            BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
            if (i >= systemConfig.getMaxSms()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BaseResponse getPhoneCode(PlayerChangePwdCodeRequest bindPhoneCodeRequest) {
        BallPlayer ballPlayer = basePlayerService.findByUsername(bindPhoneCodeRequest.getUsername());
        if (ballPlayer == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "usernameNotExist"));
        }
        //账号和手机是否匹配
        if (!bindPhoneCodeRequest.getPhone().equals(ballPlayer.getPhone())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "userNotMatchPhone"));
        }
        String maxSmsKey = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + bindPhoneCodeRequest.getPhone();
        Object o = redisUtil.get(maxSmsKey);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        String maxSmsKeyName = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + ballPlayer.getUsername();
        o = redisUtil.get(maxSmsKeyName);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        //TODO 给指定手机号发送验证码
        String s = VerifyCodeUtils.generateVerifyCode(4);
        String key = RedisKeyContant.PLAYER_PHONE_CODE + bindPhoneCodeRequest.getPhone();
//        redisUtil.set(key, s, TimeUtil.TIME_ONE_MIN * systemConfigService.getSystemConfig().getSmsInterval() / 1000);
        BaseResponse response = smsService.sendSms(ballPlayer.getUsername(), bindPhoneCodeRequest.getPhone(), s, key);
        return response;
    }

    @Override
    public BaseResponse editPwdPay(AuthEditPwdRequest req, BallPlayer player) {
        if (StringUtils.isBlank(player.getPayPassword())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("origin", "originNotSet"));
        }
        if (!player.getPayPassword().equals(PasswordUtil.genPasswordMd5(req.getOrigin()))) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("origin", "originError"));
        }
        if (!req.getConfirmed().equals(req.getNewpwd())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("confirmed", "confirmedError"));
        }

        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        String key = RedisKeyContant.PLAYER_PHONE_CODE + player.getId();
        if (systemConfig.getPayPwdNpc() == 1) {
            if (StringUtils.isBlank(req.getCode())) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "smsCodeIsEmpty"));
            }
            //需要短信验证码
            Object o = redisUtil.get(key);
            if (o == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
            }
            if (!o.equals(req.getCode())) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }
        BallPlayer edit = BallPlayer.builder()
                .payPassword(PasswordUtil.genPasswordMd5(req.getConfirmed()))
                .build();
        edit.setId(player.getId());
        if (!basePlayerService.editAndClearCache(edit, player)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "updateFailed"));
        }
//        redisUtil.del(key);
        return new BaseResponse(StatusCodes.OK, "edit success");
    }

    @Override
    public BaseResponse phoneChangePwdPay(BallPlayer player, PlayerPhoneChangePayPwdRequest phoneChangePwdRequest) {
        String key = RedisKeyContant.PLAYER_PHONE_CODE + player.getId();
        Object o = redisUtil.get(key);
        if (o == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
        }
        if (!o.equals(phoneChangePwdRequest.getCode())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeError"));
        }
        if (!phoneChangePwdRequest.getNewPwd().equals(phoneChangePwdRequest.getTwicePwd())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "confirmedError"));
        }
        BallPlayer edit = BallPlayer.builder()
                .payPassword(PasswordUtil.genPasswordMd5(phoneChangePwdRequest.getNewPwd()))
                .build();
        edit.setId(player.getId());
        boolean b = basePlayerService.editAndClearCache(edit, player);
        if (b) {
//            redisUtil.del(key);
        }
        return b ? BaseResponse.SUCCESS : BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "phoneChangePwdError"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse bindBankEdit(BallPlayer currentUser, BindBankCardRequest bindBankCardRequest) {
        if (StringUtils.isBlank(currentUser.getPayPassword())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "originNotSet"));
        }
        BallBankCard playerBankCard = bankCardService.findByPlayerId(currentUser.getId());
        if (playerBankCard == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bankNotSet"));
        }
        if (!bindBankCardRequest.getCardNumber().equals(bindBankCardRequest.getCardNumberTwice())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("cardNumberTwice", "cardNumberTwiceDiff"));
        }
        if (!currentUser.getPayPassword().equals(PasswordUtil.genPasswordMd5(bindBankCardRequest.getPayPwd()))) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("payPwd", "payPwdError"));
        }
        BallBankCard hashCard = bankCardService.findByCardNo(bindBankCardRequest.getCardNumber());
        //有卡并且不是自己卡
        if (hashCard != null
                && hashCard.getPlayerId()!=null
                &&!hashCard.getPlayerId().equals(currentUser.getId())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bankCardExists"));
        }else if(hashCard!=null
                && hashCard.getPlayerId()==null
                && hashCard.getHasWithdrawal()==1){
            //有人用卡提现成功过,不能绑定
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "bankCardExists"));
        }

        String key = RedisKeyContant.PLAYER_PHONE_CODE + currentUser.getId();
        Object o = redisUtil.get(key);
        if(someConfig.getApiSwitch()==null){
            if (o == null) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
            }
            if (!o.equals(bindBankCardRequest.getCode())) {
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "phoneCodeError"));
            }
        }
        BallBank bank = bankService.findById(bindBankCardRequest.getBankId());
        BallBankCard save = BallBankCard.builder()
                .cardName(bindBankCardRequest.getCardName())
                .cardNumber(bindBankCardRequest.getCardNumber())
                .bankName(bank.getBankCname())
//                .bankId(bank.getId())
                .backEncoding(bindBankCardRequest.getBackCode())
                .province(bindBankCardRequest.getProvince())
                .city(bindBankCardRequest.getCity())
                .subBranch(bindBankCardRequest.getSubBranch())
                .phone(bindBankCardRequest.getPhone())
                .identityCard(bindBankCardRequest.getIdentityCard())
                .country(bindBankCardRequest.getCountry())
                .status(1)
                .playerId(currentUser.getId())
                .username(currentUser.getUsername())
                .userId(currentUser.getUserId())
                .statusCheck(1)
                .country(bindBankCardRequest.getCountry())
                .build();
        boolean sqlSucc = false;
        // 情况1,修改自己当前卡号信息,但是未修改卡号,这时候不解绑,只修改信息
        // 情况2,修改自己当前卡号,如果卡号不存在,那么原卡号解绑,再增加新卡
        // 情况3,修改自己当前卡号,如果卡号存在,则换绑为自己
        if(playerBankCard.getCardNumber().equals(bindBankCardRequest.getCardNumber())){
            //修改自己的银行卡信息
            save.setId(playerBankCard.getId());
            save.setUpdatedAt(System.currentTimeMillis());
            sqlSucc = bankCardService.editById(save);
        }else{
            //原卡解绑
            bankCardService.unbind(playerBankCard);
            if(hashCard==null){
                save.setCreatedAt(System.currentTimeMillis());
                sqlSucc = bankCardService.insert(save);
            }else{
                save.setId(hashCard.getId());
                save.setUpdatedAt(System.currentTimeMillis());
                sqlSucc = bankCardService.editById(save);
            }
        }
        if (sqlSucc) {
//            redisUtil.del(key);
            if(!playerBankCard.getCardNumber().equals(bindBankCardRequest.getCardNumber())) {
                BallLoggerBindCard logger = BallLoggerBindCard.builder()
                        .backEncoding(save.getBackEncoding())
                        .bankName(save.getBankName())
                        .cardName(save.getCardName())
                        .cardNumber(save.getCardNumber())
                        .city(save.getCity())
                        .country(save.getCountry())
                        .identityCard(save.getIdentityCard())
                        .phone(save.getPhone())
                        .playerId(currentUser.getId())
                        .province(save.getProvince())
                        .subBranch(save.getSubBranch())
                        .username(currentUser.getUsername())
                        .build();
                logger.setCreatedAt(System.currentTimeMillis());
                loggerBindCardService.insert(logger);
            }
            return BaseResponse.successWithMsg("ok");
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "editCardFailed"));
    }

    @Override
    public BaseResponse changePwdPay(BallPlayer player, PlayerChangePwdRequest req) {
        if (StringUtils.isBlank(player.getPayPassword())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("origin", "originNotSet"));
        }
        if (!player.getPayPassword().equals(PasswordUtil.genPasswordMd5(req.getOrigin()))) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("origin", "originError"));
        }

        if (!req.getTwicePwd().equals(req.getNewPwd())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("confirmed", "confirmedError"));
        }

        BallPlayer edit = BallPlayer.builder()
                .payPassword(PasswordUtil.genPasswordMd5(req.getTwicePwd()))
                .build();
        edit.setId(player.getId());
        if (!basePlayerService.editAndClearCache(edit, player)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "updateFailed"));
        }
        return BaseResponse.SUCCESS;
    }

    @Override
    public BaseResponse getPhoneCode(PlayerNewDevicesRequest newDevicesRequest) {
        //TODO 给指定手机号发送验证码
        BallPlayer byUsername = basePlayerService.findByUsername(newDevicesRequest.getUsername());
        if (byUsername == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "usernameNotExist"));
        }
        if (StringUtils.isBlank(byUsername.getPhone())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneNotBind"));
        }
        String maxSmsKey = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + byUsername.getPhone();
        Object o = redisUtil.get(maxSmsKey);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        String maxSmsKeyName = RedisKeyContant.PLAYER_PHONE_SMS_COUNT + byUsername.getUsername();
        o = redisUtil.get(maxSmsKeyName);
        if (smsIsMax(o)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "maxDaySms"));
        }
        String s = VerifyCodeUtils.generateVerifyCode(4);
        String key = RedisKeyContant.PLAYER_PHONE_CODE + byUsername.getId();
//        redisUtil.set(key, s, TimeUtil.TIME_ONE_MIN * systemConfigService.getSystemConfig().getSmsInterval() / 1000);
        BaseResponse response = smsService.sendSms(byUsername.getUsername(), byUsername.getPhone(), s, key);
        return response;
    }

    @Override
    public BaseResponse changePwd(BallPlayer player, PlayerPhoneChangePayPwdRequest changePwdRequest) {
        String key = RedisKeyContant.PLAYER_PHONE_CODE + player.getId();
        Object o = redisUtil.get(key);
        if (o == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeTimeout"));
        }
        if (!o.equals(changePwdRequest.getCode())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneCodeError"));
        }
        if (!changePwdRequest.getNewPwd().equals(changePwdRequest.getTwicePwd())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "confirmedError"));
        }
        BallPlayer edit = BallPlayer.builder()
                .password(PasswordUtil.genPasswordMd5(changePwdRequest.getNewPwd()))
                .build();
        edit.setId(player.getId());
        boolean b = basePlayerService.editAndClearCache(edit, player);
        if (b) {
//            redisUtil.del(key);
        }
        return b ? BaseResponse.SUCCESS : BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "phoneChangePwdError"));
    }

    @Override
    public BaseResponse recharge(BallPlayer player, BallLoggerRecharge loggerRecharge, PayNoticeDtoCHA payParamDto, BallPaymentManagement paymentManagement) throws JsonProcessingException {
        //真实支付金额
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //系统应加金额
        Long realMoneySys = Long.parseLong(payParamDto.getAmount());
        //TODO 汇率
        Double rate = Double.valueOf(paymentManagement.getRate());
        if (rate > 0) {
            Double rmoney = BigDecimalUtil.mul(realMoneySys, rate);
            realMoneySys = rmoney.longValue();
        }
        RechargeHanderDTO rechargeHander = rechargeHander(player, realMoneySys, systemConfig, loggerRecharge, paymentManagement);
        long totalDiscount = 0L;
        for (RechargeRebateDto item : rechargeHander.getDiscountQuota()) {
            totalDiscount += item.getDiscount();
        }
        loggerRecharge.setMoneyDiscount(totalDiscount);
        loggerRecharge.setMoneySys(realMoneySys);
        loggerRecharge.setFirst(rechargeHander.isFirst()?1:0);
        loggerRecharge.setUpdatedAt(System.currentTimeMillis());
        if(StringUtils.isBlank(loggerRecharge.getOperUser())){
            loggerRecharge.setOperUser("sys");
        }

        boolean editRes = loggerRechargeService.edit(loggerRecharge);
        if (!editRes) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e50"));
        }
        BallLoggerRecharge edited = loggerRechargeService.findById(loggerRecharge.getId());
        afterRechargeSuccess(player, realMoneySys, rechargeHander, edited, systemConfig, paymentManagement);
        return BaseResponse.SUCCESS;
    }

    private Map<String, Object> getItem(Map<Integer, Map<String, Object>> data, int level) {
        Map<String, Object> map = data.get(level);
        if (map == null) {
            map = new HashMap<>();
            data.put(level, map);
        }
        return map;
    }

    private List<BallBalanceChange> getBallBalanceChanges(DataCenterRequest dataCenterRequest, List<Long> ids) {
        //统计账变
        QueryWrapper<BallBalanceChange> changeQueryWrapper = new QueryWrapper<>();
        if (ids != null) {
            changeQueryWrapper.in("player_id", ids);
        }
        BallBalanceChangeServiceImpl.queryCaseTime(dataCenterRequest, changeQueryWrapper);
        return ballBalanceChangeService.list(changeQueryWrapper);
    }

}
