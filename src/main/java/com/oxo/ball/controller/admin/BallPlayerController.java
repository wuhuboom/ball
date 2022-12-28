package com.oxo.ball.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.admin.PlayerRepairRecharge;
import com.oxo.ball.bean.dto.req.admin.PlayerRepairWithdrawal;
import com.oxo.ball.bean.dto.req.player.BalanceChangeRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.interceptor.MainOper;
import com.oxo.ball.interceptor.SubOper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.admin.BallPayBehalfServiceImpl;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.MapUtil;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 玩家账号 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/player")
@MainOper("会员管理")
public class BallPlayerController {

    @Resource
    IBallPlayerService ballPlayerService;
    @Resource
    IBasePlayerService iBasePlayerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    IPlayerService playerService;
    @Autowired
    BallAdminService adminService;
    @Autowired
    private IBallVipService vipService;
    @Autowired
    IBallLoggerService loggerService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallWithdrawManagementService withdrawManagementService;
    @Autowired
    IBallVirtualCurrencyService virtualCurrencyService;
    @Autowired
    IBallPayBehalfService payBehalfService;
    @Autowired
    IBallApiConfigService apiConfigService;
    @Autowired
    RedisUtil redisUtil;
    @PostMapping
    public Object index(BallPlayer query, @RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallPlayer> search = ballPlayerService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object getVip(){
        List<BallVip> byAll = vipService.findByAll();
        return BaseResponse.successWithData(byAll);
    }
    @PostMapping("finance")
    public Object finance(BallPlayer query, @RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallPlayer> search = ballPlayerService.searchFinance(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }

    @PostMapping("add")
    public Object add(@RequestBody BallPlayer sysUserRequest){
        if(sysUserRequest.getAccountType()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e5"));
        }
        Object insert = ballPlayerService.insert(sysUserRequest);
        return insert;
    }
    @GetMapping("add")
    public Object addMult(BallPlayer ballPlayer){
        if(ballPlayer.getAccountType()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e5"));
        }
        BaseResponse insert = ballPlayerService.insertMult(ballPlayer);
        return insert;
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallPlayer ballPlayer){
        BaseResponse response = ballPlayerService.edit(ballPlayer);
        return response;
    }
    @PostMapping("edit_pwd")
    public Object editPwd(@RequestBody BallPlayer ballPlayer){
        Boolean aBoolean = ballPlayerService.editPwd(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("edit_pay_pwd")
    public Object editPayPwd(@RequestBody BallPlayer ballPlayer){
        Boolean aBoolean = ballPlayerService.editPayPwd(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("edit_level")
    public Object editLevel(@RequestBody BallPlayer ballPlayer){
        Boolean aBoolean = ballPlayerService.editLevel(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallPlayer ballPlayer){
        Boolean aBoolean = ballPlayerService.editStatus(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("add_balance")
    @SubOper("上/下分")
    public BaseResponse<BallPlayer> addBalance(@RequestBody BallPlayer ballPlayer, HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request);
        ballPlayer.setOperUser(currentUser.getUsername());
        BaseResponse<BallPlayer> response = null;
        try {
            response = ballPlayerService.editAddBalance(ballPlayer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if(response.getCode().equals(BaseResponse.SUCCESS.getCode())){
            response.setRemark("会员["+response.getData().getUsername()+"]上/下分["+ballPlayer.getDbalance()+"]");
        }
        return response;
    }
    @PostMapping("captcha_pass")
    public Object captchaPass(@RequestBody BallPlayer ballPlayer){
        Boolean aBoolean = ballPlayerService.editCaptchaPass(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
    @PostMapping("log")
    public Object balanceChange(BallBalanceChange query,@RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallBalanceChange> response = ballBalanceChangeService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(response);
    }
    @GetMapping("log")
    public Object balanceChangeTotal(BallBalanceChange query,@RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        BallBalanceChange response = ballBalanceChangeService.searchTotal(query, pageNo, pageSize);
        return BaseResponse.successWithData(response);
    }
    @PutMapping("log")
    public Object loginLog(BallLoggerLogin query){
        List<BallLoggerLogin> response = loggerService.search(query);
        return BaseResponse.successWithData(response);
    }
    @PostMapping("info")
    public Object balanceChange(@RequestParam("playerId") Long playerId){
        BallPlayer ballPlayer = BallPlayer.builder().build();
        ballPlayer.setId(playerId);
        return ballPlayerService.info(ballPlayer);
    }
    @PostMapping("repair_re")
    public Object playerRepairRecharge(@RequestBody PlayerRepairRecharge repairRecharge,HttpServletRequest request){
        BaseResponse response = null;
        BallAdmin currentUser = adminService.getCurrentUser(request);
        try {
            response = ballPlayerService.repairRecharge(repairRecharge,currentUser);
        } catch (SQLException e) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e6"));
        } catch (JsonProcessingException e) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e6"));
        }
        return response;
    }
    @GetMapping("repair_re")
    public Object playerRepairRechargePays(){
        List<BallPaymentManagement> all = paymentManagementService.findByAll();
        return BaseResponse.successWithData(all);
    }
    @PostMapping("repair_wi")
    public Object playerRepairWithdrawal(@RequestBody PlayerRepairWithdrawal repairWithdrawal,HttpServletRequest request){
        BaseResponse response = null;
        BallAdmin currentUser = adminService.getCurrentUser(request);
        try {
            response = ballPlayerService.repairWithdrawal(repairWithdrawal,currentUser);
        } catch (SQLException e) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e53"));
        }
        return response;
    }
    @GetMapping("repair_wi")
    public Object playerRepairWithdrawalWis(Long playerId){
        Map<String,Object> data = new HashMap<>();
        data.put("wis",withdrawManagementService.findAll());
        List<BallVirtualCurrency> byPlayerId = virtualCurrencyService.findByPlayerId(playerId);
        data.put("usdts",byPlayerId);
        List<BallPayBehalf> byType = payBehalfService.findByType(3);
        data.put("behalfs",byType);
        return BaseResponse.successWithData(data);
    }
    @GetMapping("edit")
    public Object setPlayerToProxy(Long id){
        BaseResponse response = ballPlayerService.setPlayerToProxy(id);
        return response;
    }
    @PutMapping("rebate")
    public Object sendMessageToPlayerChat(String minMax,Integer type){
        BaseResponse response = null;
        try {
            response = ballPlayerService.sendMessageToPlayerChat(minMax,type,null,false);
        } catch (IOException e) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e7"));
        }
        return response;
    }

    @GetMapping("rebate")
    public Object sendMessageToPlayerChat(){
        return BaseResponse.successWithData(apiConfigService.getApiConfig());
    }
    @PostMapping("rebate")
    public Object configRebateMessage(@RequestBody BallApiConfig ballApiConfig){
        Boolean edit = apiConfigService.edit(BallApiConfig.builder()
                .autoSend(ballApiConfig.getAutoSend())
                .minMax(ballApiConfig.getMinMax())
                .hourPer(ballApiConfig.getHourPer())
                .typeSend(ballApiConfig.getTypeSend())
                .id(ballApiConfig.getId())
                .build());
        if(edit){
            //清空待发送的消息，免得出问题
            redisUtil.del(RedisKeyContant.PLAYER_CHAT_MESSAGE);
            if(ballApiConfig.getAutoSend()==1){
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e68"));
            }else{
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e69"));
            }
        }
        return BaseResponse.failedWithMsg("update failed");
    }
}
