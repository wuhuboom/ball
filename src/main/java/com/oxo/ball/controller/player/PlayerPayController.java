package com.oxo.ball.controller.player;

import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.api.PayCallBackDto;
import com.oxo.ball.bean.dto.api.behalfcha.PayBehalfNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.cha.PayNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in3.PayNoticeDto3;
import com.oxo.ball.bean.dto.api.in3.behalf.PayBehalfNoticeDto3;
import com.oxo.ball.bean.dto.api.inbehalf.PayBehalfNoticeDtoIN;
import com.oxo.ball.bean.dto.api.meta.PayCallBackDtoMeta;
import com.oxo.ball.bean.dto.api.mp.MpPayCallBack;
import com.oxo.ball.bean.dto.api.tnz.PayCallBackDtoTnz;
import com.oxo.ball.bean.dto.api.web.WebPayCallback;
import com.oxo.ball.bean.dto.api.xd.XdPayCallBack;
import com.oxo.ball.bean.dto.req.player.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.impl.admin.BallPayBehalfServiceImpl;
import com.oxo.ball.service.pay.*;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.IpUtil;
import com.oxo.ball.utils.JsonUtil;
import com.oxo.ball.utils.ThreadPoolUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


/**
 * <p>
 * 玩家账号 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/player")
@Api(tags = "玩家 - 支付相关")
public class PlayerPayController {

    @Resource
    IPlayerService playerService;
    @Resource
    IBasePlayerService basePlayerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Resource
    IPlayerBetService betService;
    @Autowired
    IBallLoggerBackService loggerBackService;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    IBallBankService bankService;
    @Autowired
    IBallBankCardService bankCardService;
    @Autowired
    IBallVirtualCurrencyService virtualCurrencyService;
    @Autowired
    IPlayerPayService playerPayService;
    @Autowired
    IPlayerPayServiceIN playerPayServiceIN;
    @Autowired
    IPlayerPayServiceCHA playerPayServiceCHA;
    @Autowired
    IPlayerPayServiceFAST playerPayServiceFAST;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallPayBehalfService payBehalfService;
    @Autowired
    private IPlayerPayService3 playerPayService3;
    @Autowired
    private IPlayerPayServiceWOW playerPayServiceWOW;
    @Autowired
    private IPlayerPayServiceAllPay playerPayServiceAllPay;
    @Autowired
    private IPlayerPayServiceTNZ playerPayServiceTNZ;
    @Autowired
    private IPlayerPayServiceMETA playerPayServiceMETA;
    @Autowired
    private IPlayerPayServiceMETAGG playerPayServiceMETAGG;
    @Autowired
    IBallSimCurrencyService simCurrencyService;
    @Autowired
    private IPlayerPayServiceWEB playerPayServiceWEB;
    @Autowired
    private IPlayerPayServiceXD playerPayServiceXD;
    @Autowired
    private IPlayerPayServiceMP playerPayServiceMP;

    @ApiOperation(
            value = "银行",
            notes = "银行" ,
            httpMethod = "GET")
    @GetMapping("banks")
    public Object banks(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
//        List<BallBankArea> enabled = bankService.findEnabled();
//        List<BallBank> all = bankService.findAll(enabled.get(0).getCode(), enabled);
        Map<String,Object> data = new HashMap<>();
//        data.put("banks",all);
        BallPlayer currentUser = playerService.getCurrentUser(request);
        data.put("areaId",currentUser.getAreaCode());
        List<BallBank> all = bankService.findAll(currentUser);

        List<BallBank> allSet = new ArrayList<>();
        Set<String> bankNames = new HashSet<>();
        for(BallBank item: all){
            item.setBankCname(item.getBankCname().toUpperCase());
            boolean add = bankNames.add(item.getBankCname());
            if(add){
                allSet.add(item);
            }
        }
        data.put("banks",allSet);
        return BaseResponse.successWithData(data);
    }
    @ApiOperation(
            value = "绑定银行卡",
            notes = "绑定银行卡" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId",value = "银行ID",required = true),
            @ApiImplicitParam(name = "country",value = "国"),
            @ApiImplicitParam(name = "province",value = "省"),
            @ApiImplicitParam(name = "city",value = "城市"),
            @ApiImplicitParam(name = "subBranch",value = "支行",required = true),
            @ApiImplicitParam(name = "cardName",value = "姓名",required = true),
            @ApiImplicitParam(name = "cardNumber",value = "银行卡",required = true),
            @ApiImplicitParam(name = "cardNumberTwice",value = "再次银行卡",required = true),
            @ApiImplicitParam(name = "backCode",value = "银行编码"),
            @ApiImplicitParam(name = "payPwd",value = "支付密码",required = true),
            @ApiImplicitParam(name = "code",value = "手机验证码",required = true)
    })
    @PostMapping("bind_bank_card")
    public Object index(@Validated BindBankCardRequest bindBankCardRequest,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        return playerService.bindBank(currentUser,bindBankCardRequest);
//        ThreadPoolUtil.exec(new Runnable() {
//            @Override
//            public void run() {
//                for(int i=0;i<3;i++){
//                    BaseResponse response = playerService.bindBank(currentUser, bindBankCardRequest);
//                    System.out.println(response);
//                }
//            }
//        });
//        return BaseResponse.SUCCESS;
    }
    @ApiOperation(
            value = "修改绑定银行卡",
            notes = "修改绑定银行卡" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId",value = "银行ID",required = true),
            @ApiImplicitParam(name = "country",value = "国"),
            @ApiImplicitParam(name = "province",value = "省"),
            @ApiImplicitParam(name = "city",value = "城市"),
            @ApiImplicitParam(name = "subBranch",value = "支行",required = true),
            @ApiImplicitParam(name = "cardName",value = "姓名",required = true),
            @ApiImplicitParam(name = "cardNumber",value = "银行卡",required = true),
            @ApiImplicitParam(name = "cardNumberTwice",value = "再次银行卡",required = true),
            @ApiImplicitParam(name = "backCode",value = "银行编码",required = true),
            @ApiImplicitParam(name = "payPwd",value = "支付密码",required = true),
            @ApiImplicitParam(name = "code",value = "手机验证码",required = true)
    })
    @PostMapping("bind_bank_card_edit")
    public Object indexEdit(@Validated BindBankCardRequest bindBankCardRequest,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse res = playerService.bindBankEdit(currentUser,bindBankCardRequest);
        return res;
    }

    @ApiOperation(
            value = "银行卡信息",
            notes = "银行卡信息" ,
            httpMethod = "GET")
    @GetMapping("bank_card_info")
    public Object bankCardInfo(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        return BaseResponse.successWithData(bankCardService.findByPlayerId(currentUser.getId()));
    }

    @ApiOperation(
            value = "绑定SIM号",
            notes = "绑定SIM号" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sim",value = "sim号",required = true),
            @ApiImplicitParam(name = "name",value = "姓名", required = true),
            @ApiImplicitParam(name = "payPwd",value = "支付密码",required = true),
            @ApiImplicitParam(name = "code",value = "手机验证码",required = true)
    })
    @PostMapping("bind_sim_card")
    public Object bind_sim_card(@Validated SimCurrencyRequest simCurrencyRequest,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        return simCurrencyService.insert(currentUser,simCurrencyRequest);
    }
    @ApiOperation(
            value = "修改绑定SIM号",
            notes = "修改绑定SIM号" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sim",value = "sim号",required = true),
            @ApiImplicitParam(name = "name",value = "姓名",required = true),
            @ApiImplicitParam(name = "payPwd",value = "支付密码",required = true),
            @ApiImplicitParam(name = "code",value = "手机验证码",required = true)
    })
    @PostMapping("bind_sim_card_edit")
    public Object bind_sim_card_edit(@Validated SimCurrencyRequest simCurrencyRequest,
                            HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse res = simCurrencyService.edit(currentUser,simCurrencyRequest);
        return res;
    }

    @ApiOperation(
            value = "SIM信息",
            notes = "SIM信息" ,
            httpMethod = "GET")
    @GetMapping("sim_card_info")
    public Object sim_card_info(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        return BaseResponse.successWithData(simCurrencyService.findByPlayerId(currentUser.getId()));
    }

    @ApiOperation(
            value = "虚拟币-添加",
            notes = "虚拟币-添加,,type,subType随便传" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type",value = "类型",required = true),
            @ApiImplicitParam(name = "subType",value = "小类型",required = true),
            @ApiImplicitParam(name = "addr",value = "地址",required = true),
            @ApiImplicitParam(name = "payPwd",value = "支付密码",required = true),
            @ApiImplicitParam(name = "code",value = "手机验证码",required = true)
    })
    @PostMapping("virtual_currency_add")
    public Object addVirtualCurrency(@Validated VirtualCurrencyRequest virtualCurrencyRequest,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse res = virtualCurrencyService.insert(currentUser,virtualCurrencyRequest);
        return res;
    }
    @ApiOperation(
            value = "虚拟币-列表",
            notes = "虚拟币-列表" ,
            httpMethod = "POST")
    @PostMapping("virtual_currency_list")
    public Object virtualCurrencyList(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        List<BallVirtualCurrency> res = virtualCurrencyService.findByPlayerId(currentUser.getId());
        return BaseResponse.successWithData(res);
    }
    @ApiOperation(
            value = "虚拟币-删除",
            notes = "虚拟币-删除" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "虚拟币ID",required = true)
//            @ApiImplicitParam(name = "code",value = "手机验证码",required = true)
    })
    @PostMapping("virtual_currency_del")
    public Object virtualCurrencyDel(VirtualCurrencyDelRequest virtualCurrencyDelRequest,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse res = virtualCurrencyService.del(currentUser,virtualCurrencyDelRequest);
        return res;
    }
    @ApiOperation(
            value = "虚拟币-修改",
            notes = "虚拟币-修改" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "虚拟币ID",required = true),
            @ApiImplicitParam(name = "addr",value = "虚拟币地址",required = true),
            @ApiImplicitParam(name = "payPwd",value = "支付密码",required = true),
            @ApiImplicitParam(name = "code",value = "手机验证码",required = true)
    })
    @PostMapping("virtual_currency_edit")
    public Object virtualCurrencyDel(VirtualCurrencyEditRequest virtualCurrencyEditRequest,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse res = virtualCurrencyService.edit(currentUser,virtualCurrencyEditRequest);
        return res;
    }

    @ApiOperation(value = "", hidden = true)
    @PostMapping(value = "pay/callback/1",consumes = "application/octet-stream")
    public Object payCallback(@RequestBody String callback,HttpServletRequest request)  {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
        PayCallBackDto payCallBackDto = null;
        try {
            payCallBackDto = JsonUtil.fromJson(callback,PayCallBackDto.class);
        } catch (IOException e) {
            e.printStackTrace();
            return BaseResponse.failedWithMsg("无法解析回调数据:"+callback);
        }
        BallPaymentManagement paymentManagement = paymentManagementService.findByCallback("/pay/callback/1");
        String ipAddress = IpUtil.getIpAddress(request);
        if(!StringUtils.isBlank(paymentManagement.getWhiteIp())
                &&!paymentManagement.getWhiteIp().contains(ipAddress)){
            //有配置白名单，名单中未有IP
            return BaseResponse.SUCCESS;
        }
        playerPayService.payCallBack(paymentManagement,payCallBackDto);
        return BaseResponse.SUCCESS;
    }

    /**
     * 印度支付回调
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/2")
    public Object payCallback2(PayNoticeDtoIN payNotice,HttpServletRequest request)  {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
//        BallPaymentManagement paymentManagement = paymentManagementService.findByCallback("/pay/callback/2");
        payNotice.setAttach(IpUtil.getIpAddress(request));
        playerPayServiceIN.payCallBack(payNotice);
        return BaseResponse.SUCCESS;
    }

    /**
     * 代付回调
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/3")
    public Object payCallback3(PayBehalfNoticeDtoIN payNotice,HttpServletRequest request)  {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
        // /pay/callback/7
        payNotice.setAttach(IpUtil.getIpAddress(request));
        payBehalfService.payCallBack(payNotice);
        return BaseResponse.SUCCESS;
    }
    /**
     * 加纳支付
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/4")
    public Object payCallback4(PayNoticeDtoCHA payNotice,HttpServletRequest request)  {
        payNotice.setSign(IpUtil.getIpAddress(request));
        playerPayServiceCHA.payCallBack(payNotice);
//        return BaseResponse.SUCCESS;
        return "success";
    }


    /**
     * 代付回调-加纳
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/5")
    public Object payCallback5(PayBehalfNoticeDtoCHA payNotice,HttpServletRequest request)  {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
        payNotice.setSign(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackCha(payNotice);
        return "success";
    }

    /**
     * 印度fastpay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/6")
    public Object payCallback6(@RequestBody PayCallBackDtoFast payNotice,HttpServletRequest request) {
        payNotice.setExt(IpUtil.getIpAddress(request));
        playerPayServiceFAST.payCallBack(payNotice);
        return "success";
    }


    /**
     * 代付回调-印度 fast
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/7")
    public Object payCallback7(@RequestBody PayCallBackDtoFast payNotice,HttpServletRequest request) {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
        payNotice.setExt(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackFast(payNotice);
        return "success";
    }


    /**
     * 印度支付3
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/8")
    public Object payCallback8(PayNoticeDto3 payNotice,HttpServletRequest request) {
        payNotice.setExtra(IpUtil.getIpAddress(request));
        playerPayService3.payCallBack(payNotice);
        return "success";
    }


    /**
     * 代付回调-印度支付3
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/9")
    public Object payCallback9(PayBehalfNoticeDto3 payNotice,HttpServletRequest request) {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
        payNotice.setExtra(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackIn3(payNotice);
        return "success";
    }


    /**
     * 印度wow支付
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/10")
    public Object payCallback10(PayNoticeDtoCHA payNotice,HttpServletRequest request)  {
        payNotice.setSign(IpUtil.getIpAddress(request));
        playerPayServiceWOW.payCallBack(payNotice);
//        return BaseResponse.SUCCESS;
        return "success";
    }


    /**
     * 代付回调-印度wow
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/11")
    public Object payCallback11(PayBehalfNoticeDtoCHA payNotice,HttpServletRequest request)  {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
        payNotice.setSign(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackWow(payNotice);
        return "success";
    }
    /**
     * 印度all_pay支付
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/12")
    public Object payCallback12(PayNoticeDtoCHA payNotice,HttpServletRequest request)  {
        payNotice.setSign(IpUtil.getIpAddress(request));
        playerPayServiceAllPay.payCallBack(payNotice);
        return "success";
    }


    /**
     * 代付回调-印度wow
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/13")
    public Object payCallback13(PayBehalfNoticeDtoCHA payNotice,HttpServletRequest request)  {
        //这个地址是1号ustd专用地址,http://localhost:100010/pay/callback/1
        payNotice.setSign(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackAllPay(payNotice);
        return "success";
    }
    /**
     * 支付回调-加纳-tnz
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/14")
    public Object payCallback14(@RequestBody PayCallBackDtoTnz payNotice,HttpServletRequest request)  {
        payNotice.setExtInfo(IpUtil.getIpAddress(request));
        playerPayServiceTNZ.payCallBack(payNotice);
        return "OK";
    }

    /**
     * 代付回调-加纳tnz
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @PostMapping("pay/callback/15")
    public Object payCallback15(@RequestBody PayCallBackDtoTnz payNotice,HttpServletRequest request)  {
        payNotice.setExtInfo(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackTNZ(payNotice);
        return "OK";
    }
    /**
     * 支付回调-metapay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/16")
    public Object payCallback16(PayCallBackDtoMeta payNotice, HttpServletRequest request)  {
        payNotice.setExtInfo(IpUtil.getIpAddress(request));
        playerPayServiceMETA.payCallBack(payNotice);
        return "SUCCESS";
    }

    /**
     * 代付回调-metapay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/17")
    public Object payCallback17(PayCallBackDtoMeta payNotice,HttpServletRequest request)  {
        payNotice.setExtInfo(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackMETA(payNotice);
        return "SUCCESS";
    }
    /**
     * 支付回调-metapay gg
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/18")
    public Object payCallback18(PayCallBackDtoMeta payNotice, HttpServletRequest request)  {
        payNotice.setExtInfo(IpUtil.getIpAddress(request));
        playerPayServiceMETAGG.payCallBack(payNotice);
        return "SUCCESS";
    }

    /**
     * 代付回调-metapaygg
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/19")
    public Object payCallback19(PayCallBackDtoMeta payNotice,HttpServletRequest request)  {
        payNotice.setExtInfo(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackMETAGG(payNotice);
        return "SUCCESS";
    }
    /**
     * 支付回调-webpay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/20")
    public Object payCallback20(@RequestBody WebPayCallback payNotice, HttpServletRequest request)  {
        payNotice.setOtherData(IpUtil.getIpAddress(request));
        playerPayServiceWEB.payCallBack(payNotice);
        return "success";
    }

    /**
     * 代付回调-webpay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/21")
    public Object payCallback19(@RequestBody WebPayCallback payNotice, HttpServletRequest request)  {
        payNotice.setOtherData(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackWeb(payNotice);
        return "success";
    }
    /**
     * 支付回调-xdpay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/22")
    public Object payCallback22(@RequestBody XdPayCallBack payNotice, HttpServletRequest request)  {
        payNotice.setExt(IpUtil.getIpAddress(request));
        playerPayServiceXD.payCallBack(payNotice);
        return "success";
    }

    /**
     * 代付回调-webpay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/23")
    public Object payCallback23(@RequestBody XdPayCallBack payNotice, HttpServletRequest request)  {
        payNotice.setExt(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackXD(payNotice);
        return "success";
    }
    /**
     * 支付回调-mppay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/24")
    public Object payCallback24(@RequestBody MpPayCallBack payNotice, HttpServletRequest request)  {
        payNotice.setExt(IpUtil.getIpAddress(request));
        playerPayServiceMP.payCallBack(payNotice);
        return "success";
    }

    /**
     * 代付回调-Mppay
     * @param payNotice
     * @return
     * @throws TokenInvalidedException
     * @throws PlayerDisabledException
     */
    @ApiOperation(value = "", hidden = true)
    @RequestMapping("pay/callback/25")
    public Object payCallback25(@RequestBody MpPayCallBack payNotice, HttpServletRequest request)  {
        payNotice.setExt(IpUtil.getIpAddress(request));
        payBehalfService.payCallBackMp(payNotice);
        return "success";
    }
}
