package com.oxo.ball.controller.player;

import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.req.AuthEditPwdRequest;
import com.oxo.ball.bean.dto.req.player.PlayerAuthLoginNewDevicesRequest;
import com.oxo.ball.bean.dto.req.player.PlayerAuthLoginRequest;
import com.oxo.ball.bean.dto.req.player.PlayerRegistRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.impl.player.PlayerAuthServiceImpl;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author flooming
 */
@Api(tags = "玩家 - 登录/注册")
@RestController
@RequestMapping("/player/auth")
public class PlayerAuthController {
    @Autowired
    IPlayerService playerService;
    @Resource
    RedisUtil redisUtil;
    @Resource
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBasePlayerService basePlayerService;

    @ApiOperation(
            value = "登录认证",
            notes = "登录认证" +
                    "<br>响应code" +
                    "<br>101-密码输入错误计数"+
                    "<br>102-密码输入错误上限"+
                    "<br>codeTimeOut:验证码已过期"+
                    "<br>codeError:验证码不正确"+
                    "<br>pwdErrorMax:密码输入错误次数已达上限"+
                    "<br>pwdErrorCount:密码错误次数N次"+
                    "",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "账号",required = true),
            @ApiImplicitParam(name = "password",value = "密码",required = true),
            @ApiImplicitParam(name = "code",value = "验证码",required = true),
            @ApiImplicitParam(name = "verifyKey",value = "获取验证码时的key",required = true)
    })
    @PostMapping("/login")
    public BaseResponse login(@Validated PlayerAuthLoginRequest req,HttpServletRequest request) {
        return playerService.login(req,request, false);
    }
    @ApiOperation(
            value = "登录认证-新设备",
            notes = "登录认证-新设备" +
                    "<br>响应code" +
                    "<br>101-密码输入错误计数"+
                    "<br>102-密码输入错误上限"+
                    "<br>codeTimeOut:验证码已过期"+
                    "<br>codeError:验证码不正确"+
                    "<br>pwdErrorMax:密码输入错误次数已达上限"+
                    "<br>pwdErrorCount:密码错误次数N次"+
                    "",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "账号",required = true),
            @ApiImplicitParam(name = "password",value = "密码",required = true),
            @ApiImplicitParam(name = "code",value = "验证码",required = true),
            @ApiImplicitParam(name = "verifyKey",value = "获取验证码时的key",required = true),
            @ApiImplicitParam(name = "phoneCode",value = "手机验证码",required = true)
    })
    @PostMapping("/login_new")
    public BaseResponse loginNew(@Validated PlayerAuthLoginNewDevicesRequest req, HttpServletRequest request) {
        return playerService.newDeviceslogin(req,request);
    }

    /**
     *
     * @param ballPlayer
     * @return
     */
    @ApiOperation(
            value = "注册",
            notes = "注册",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "账号",required = true),
            @ApiImplicitParam(name = "password",value = "密码",required = true),
            @ApiImplicitParam(name = "twoPassword",value = "再次密码",required = true),
            @ApiImplicitParam(name = "invitationCode",value = "邀请码"),
            @ApiImplicitParam(name = "verifyKey",value = "获取验证码时的key",required = true),
            @ApiImplicitParam(name = "code",value = "验证码",required = true),
            @ApiImplicitParam(name = "phone",value = "手机号"),
            @ApiImplicitParam(name = "areaCode",value = "手机区号")
    })
    @PostMapping("/regist")
    public BaseResponse regist(@Validated PlayerRegistRequest ballPlayer,HttpServletRequest request) {
        BaseResponse response = playerService.registPlayer(ballPlayer,IpUtil.getIpAddress(request));
        return response;
    }

    /**
     * 获取验证码方法
     */
    @ApiOperation(
            value = "获取验证码",
            notes = "获取验证码",
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("/verify_code")
    public BaseResponse getVerifyCode() throws IOException {
        return playerService.getVerifyCode();
    }
    /**
     * 获取验证码方法
     */
    @ApiOperation(
            value = "注册获取系统配置",
            notes = "注册获取系统配置,invitation_code:是否需要邀请码1.需要2.不需要\n" +
                    "area_code:区号\n" +
                    "edit_pay_pwd:修改支付密码是否需要手机验证码1需要2不需要",
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("/sys_config")
    public BaseResponse getSystemConfig() {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        Map<String, Object> invitation_code = MapUtil.newMap("invitation_code", systemConfig.getRegisterIfNeedVerificationCode());
        if(StringUtils.isBlank(systemConfig.getPhoneAreaCode())){
            invitation_code.put("area_code",new ArrayList<>());
        }else{
            invitation_code.put("area_code",systemConfig.getPhoneAreaCode().split(","));
        }
        invitation_code.put("edit_pay_pwd",systemConfig.getPayPwdNpc());

        return BaseResponse.successWithData(invitation_code);
    }
    @ApiOperation(
            value = "判定验证码",
            notes = "判定验证码",
            httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "verifyKey",value = "获取验证码时拿到的key",required = true),
    })
    @GetMapping("/verify_code_check")
    public BaseResponse checkVerifyCode(@RequestParam("verifyKey") String verifyKey,@RequestParam("code") String code) {
        return playerService.checkVerifyCode(verifyKey,code);
    }

    @ApiOperation(
            value = "登出",
            notes = "登出",
            httpMethod = "GET")
    @ApiImplicitParams({
    })
    @GetMapping("/logout")
    public BaseResponse logout(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer systemUser = playerService.getCurrentUser(request);
        if(systemUser == null) {
            return new BaseResponse("not login");
        }
        return new BaseResponse(StatusCodes.OK, "注销成功");
    }

    @ApiOperation(
            value = "修改密码",
            notes = "修改密码",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "origin",value = "原密码",required = true),
            @ApiImplicitParam(name = "newpwd",value = "新密码",required = true),
            @ApiImplicitParam(name = "confirmed",value = "再次密码",required = true),

    })
    @PostMapping("/editPwd")
    public BaseResponse editPwd(@Validated AuthEditPwdRequest req, HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);

        if(!player.getPassword().equals(PasswordUtil.genPasswordMd5(req.getOrigin()))) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("origin","originError"));
        }

        if(!req.getConfirmed().equals(req.getNewpwd())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("confirmed","confirmedError"));
        }

        BallPlayer edit = BallPlayer.builder()
                .password(PasswordUtil.genPasswordMd5(req.getConfirmed()))
                .build();
        edit.setId(player.getId());
        if(!basePlayerService.editAndClearCache(edit, player)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("","updateFailed"));
        }
        redisUtil.del(PlayerAuthServiceImpl.REDIS_PLAYER_AUTH_KEY+player.getId());
        return new BaseResponse(StatusCodes.OK, "edit success");
    }
}
