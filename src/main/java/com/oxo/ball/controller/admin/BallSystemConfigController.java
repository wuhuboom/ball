package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallIpWhite;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.req.admin.RateConfigs;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.service.admin.IBallIpWhiteService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.admin.IBallSystemNoticeService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.JsonUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.utils.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 系统配置 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/merchant/param")
public class BallSystemConfigController {

    @Autowired
    IBallIpWhiteService ipWhiteService;
    @Resource
    IBallSystemConfigService systemConfigService;

    @GetMapping()
    public Object getConfig(){
        return BaseResponse.successWithData(systemConfigService.getSystemConfig());
    }

    @PostMapping("loreg")
    public Object loreg(@RequestBody BallSystemConfig systemConfig){
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .switchNoRecharge(systemConfig.getSwitchNoRecharge())
                //验证码频率
                .maxSms(systemConfig.getMaxSms())
                //是否需要邀请码
                .registerIfNeedVerificationCode(systemConfig.getRegisterIfNeedVerificationCode())
                //密码连续错误次数
                .passwordMaxErrorTimes(systemConfig.getPasswordMaxErrorTimes())
                //锁定时间
                .passwordErrorLockTime(systemConfig.getPasswordErrorLockTime())
                //区号
                //是否需要手机验证码修改支付密码
                .payPwdNpc(systemConfig.getPayPwdNpc())
                .defaultProxy(systemConfig.getDefaultProxy())
                .newDevices(systemConfig.getNewDevices())
                .phoneAreaCode(systemConfig.getPhoneAreaCode())
                .smsInterval(systemConfig.getSmsInterval())
                .build();
//        if(!StringUtils.isBlank(systemConfig.getPhoneAreaCode())){
//            String replace = systemConfig.getPhoneAreaCode().trim().replace("，", ",");
//            String[] split = replace.split(",");
//            List<String> strings = Arrays.asList(split);
//            Set<String> strings1 = new HashSet<>(strings);
//            edit.setPhoneAreaCode(StringUtils.join(strings1,","));
//        }
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("servicer")
    public Object servicer(@RequestBody BallSystemConfig systemConfig){
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .serverUrl(systemConfig.getServerUrl())
                .serverUrlTmp(systemConfig.getServerUrlTmp())
                .build();
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("finance")
    public Object finance(@RequestBody BallSystemConfig systemConfig){
        Double betHandMoneyRate = BigDecimalUtil.mul(Double.parseDouble(systemConfig.getBetHandMoneyRateStr()), 100);
        Double rechargeCodeConversionRate = BigDecimalUtil.mul(Double.parseDouble(systemConfig.getRechargeCodeConversionRateStr()), 100);
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .reMax(systemConfig.getReMax())
                .reTime(systemConfig.getReTime())
                .switchRebateFirst(systemConfig.getSwitchRebateFirst())
                .switchRebateEvery(systemConfig.getSwitchRebateEvery())
                .switchRebate(systemConfig.getSwitchRebate())
                .rebateTime(systemConfig.getRebateTime())
                .rebateWeek(systemConfig.getRebateWeek())
                .autoUp(systemConfig.getAutoUp())
                .autoUpOff(systemConfig.getAutoUpOff())
                .rechargeAreaSwtich(systemConfig.getRechargeAreaSwtich())
                .rechargeMax(systemConfig.getRechargeMax())
                .cardCanNeedNums(systemConfig.getCardCanNeedNums())
                .rechargeCodeConversionRate(rechargeCodeConversionRate.longValue())
                .captchaThreshold(systemConfig.getCaptchaThreshold())
                .betHandMoneyRate(betHandMoneyRate.intValue())
                .fastMoney(systemConfig.getFastMoney())
                .withdrawUsdtAutomaticPer(systemConfig.getWithdrawUsdtAutomaticPer())
                .evenNeedHandMoney(systemConfig.getEvenNeedHandMoney())
                .maxUsdtAccountNums(systemConfig.getMaxUsdtAccountNums())
                .maxPixAccountNums(systemConfig.getMaxPixAccountNums())
                .withdrawPasswordCanUpdate(systemConfig.getWithdrawPasswordCanUpdate())
                .canWithdrawContinuity(systemConfig.getCanWithdrawContinuity())
                .withdrawPasswordShowNeed(systemConfig.getWithdrawPasswordShowNeed())
                .build();
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("risk")
    public Object risk(@RequestBody BallSystemConfig systemConfig){
        Double usdtWithdrawalRate = BigDecimalUtil.mul(Double.parseDouble(systemConfig.getUsdtWithdrawalRateStr()), 100);
        Double withdrawalRate = BigDecimalUtil.mul(Double.parseDouble(systemConfig.getWithdrawalRateStr()), 100);
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .everydayWithdrawTimes(systemConfig.getEverydayWithdrawTimes())
                .everydayWithdrawFree(systemConfig.getEverydayWithdrawFree())
                .withdrawMin(systemConfig.getWithdrawMin())
                .withdrawMax(systemConfig.getWithdrawMax())
                .withdrawalRate(withdrawalRate.intValue())
                .withdrawalRateMax(systemConfig.getWithdrawalRateMax())
                .withdrawalRateMin(systemConfig.getWithdrawalRateMin())
                .usdtWithdrawMin(systemConfig.getUsdtWithdrawMin())
                .usdtWithdrawMax(systemConfig.getUsdtWithdrawMax())
                .usdtWithdrawalRate(usdtWithdrawalRate.intValue())
                .usdtWithdrawalRateMax(systemConfig.getUsdtWithdrawalRateMax())
                .usdtWithdrawalRateMin(systemConfig.getUsdtWithdrawalRateMin())
                .wiInterval(systemConfig.getWiInterval())
//                .bindCard(systemConfig.getBindCard())
                .build();
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("operate")
    public Object operate(@RequestBody BallSystemConfig systemConfig){
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .playerBetMax(systemConfig.getPlayerBetMax())
                .playerBetMin(systemConfig.getPlayerBetMin())
                .build();
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("share")
    public Object share(@RequestBody BallSystemConfig systemConfig){
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .gameFinishMin(systemConfig.getGameFinishMin())
                .build();
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("sys")
    public Object sysConfig(@RequestBody BallSystemConfig systemConfig){
        if(systemConfig.getOpenWhite()==1){
            List<BallIpWhite> all = ipWhiteService.findAll();
            if(all==null||all.isEmpty()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e14"));

            }
        }
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .openGoogle(systemConfig.getOpenGoogle())
                .openWhite(systemConfig.getOpenWhite())
                .todoModel(systemConfig.getTodoModel())
                .statisTime(systemConfig.getStatisTime())
                .build();
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }

    @PostMapping("rate")
    public Object rate(@RequestBody BallSystemConfig systemConfig){
        BallSystemConfig edit = BallSystemConfig.builder()
                .id(systemConfig.getId())
                .usdtWithdrawPer(systemConfig.getUsdtWithdrawPer())
                .euroRate(systemConfig.getEuroRate())
                .build();
        try {
            List<RateConfigs> rateConfigs = JsonUtil.fromJsonToList(systemConfig.getEuroRate(), RateConfigs.class);
            for(RateConfigs item:rateConfigs){
                if(StringUtils.isBlank(item.getAreaCode())||
                        StringUtils.isBlank(item.getRate())||
                        StringUtils.isBlank(item.getSymbol())){
                    return BaseResponse.failedWithMsg("config error");
                }
                if(UUIDUtil.hasZhChar(item.getName())){
                    return BaseResponse.failedWithMsg("config error name must not be chinese");
                }
            }
        } catch (IOException e) {
            return BaseResponse.failedWithMsg("config error");
        }
        Boolean aBoolean = systemConfigService.edit(edit);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }

}
