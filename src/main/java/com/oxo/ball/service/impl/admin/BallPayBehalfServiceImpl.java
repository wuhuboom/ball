package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.api.behalfcha.PayBehalfCallBackDtoCHA;
import com.oxo.ball.bean.dto.api.behalfcha.PayBehalfNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamBackDtoFast;
import com.oxo.ball.bean.dto.api.in3.behalf.PayBehalfCallBackDto3;
import com.oxo.ball.bean.dto.api.in3.behalf.PayBehalfNoticeDto3;
import com.oxo.ball.bean.dto.api.inbehalf.PayBehalfCallBackDtoIN;
import com.oxo.ball.bean.dto.api.inbehalf.PayBehalfNoticeDtoIN;
import com.oxo.ball.bean.dto.api.meta.PayCallBackDtoMeta;
import com.oxo.ball.bean.dto.api.meta.PayResponseDtoMeta;
import com.oxo.ball.bean.dto.api.mp.MpBehalfResponse;
import com.oxo.ball.bean.dto.api.mp.MpPayCallBack;
import com.oxo.ball.bean.dto.api.tnz.PayCallBackDtoTnz;
import com.oxo.ball.bean.dto.api.tnz.PayParamBackDtoTnz;
import com.oxo.ball.bean.dto.api.web.WebPayCallback;
import com.oxo.ball.bean.dto.api.web.WebPayResponse;
import com.oxo.ball.bean.dto.api.xd.XdPayCallBack;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallPayBehalfMapper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.text.MessageFormat;
import java.util.*;

/**
 * <p>
 * ???????????? ???????????????
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallPayBehalfServiceImpl extends ServiceImpl<BallPayBehalfMapper, BallPayBehalf> implements IBallPayBehalfService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    private IBallSystemConfigService systemConfigService;
    @Autowired
    private IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    private IBallLoggerRebateService loggerBehalfService;
    @Autowired
    IBallBankCardService bankCardService;
    @Autowired
    RestHttpsUtil restHttpsUtil;
    @Autowired
    private IBasePlayerService basePlayerService;
    @Autowired
    private IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    IBallBankService ballBankService;
    @Value("${static.file}")
    private String staticFile;
    @Autowired
    private SomeConfig someConfig;
    @Autowired
    BallAdminService adminService;
    @Autowired
    IApiService apiService;
    @Autowired
    IBallPayBehalfService payBehalfService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    IBallCountryService countryService;

    @Override
    public SearchResponse<BallPayBehalf> search(BallPayBehalf queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPayBehalf> response = new SearchResponse<>();
        Page<BallPayBehalf> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallPayBehalf> query = new QueryWrapper<>();
        if(queryParam.getPayType2()!=null){
            query.eq("pay_type2",queryParam.getPayType2());
        }
        if(queryParam.getCountryId()!=null){
            query.eq("country_id",queryParam.getCountryId());
        }
        IPage<BallPayBehalf> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallPayBehalf findById(Long id) {
        return getById(id);
    }

    @Override
    public List<BallPayBehalf> findByType(Integer type) {
        QueryWrapper<BallPayBehalf> query = new QueryWrapper();
        query.eq("pay_type2",type);
        query.eq("status",1);
        return list(query);
    }

    @Override
    public List<BallPayBehalf> findByAll() {
        QueryWrapper<BallPayBehalf> query = new QueryWrapper();
        List<BallBankArea> enabled = ballBankService.findEnabled();
        List<Long> ids = new ArrayList<>();
        for(BallBankArea ballBankArea:enabled){
            ids.add(ballBankArea.getId());
        }
        query.and(QueryWrapper -> QueryWrapper.in("area_id",ids)
                .or().in("pay_type",3,4));
//        query.eq("pay_type",enabled.getCode());
        return list(query);
    }

    @Override
    public List<BallPayBehalf> findByAllTrue() {
        return list();
    }

    private void setCallBackUrl(BallPayBehalf payBehalf) {
        if(payBehalf.getPayType2()==null){
            return;
        }
        //TODO ?????????
        switch (payBehalf.getPayType2()) {
            case 1:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/3");
                break;
            case 2:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/5");
                break;
            case 4:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/7");
                break;
            case 5:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/9");
                break;
            case 6:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/11");
                break;
            case 7:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/13");
                break;
            case 8:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/15");
                break;
            case 9:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/17");
                break;
            case 10:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/19");
                break;
            case 11:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/21");
                break;
            case 12:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/23");
                break;
            case 13:
                payBehalf.setLocalCallback(someConfig.getServerUrl()+"player/pay/callback/25");
                break;
            default:
                break;
        }
    }
    @Override
    public BallPayBehalf insert(BallPayBehalf paymentManagement, MultipartFile file) {
        paymentManagement.setCreatedAt(System.currentTimeMillis());
//        1.?????? 2?????? 3?????? 4??????fast 5??????UPI 6??????wow
        setCallBackUrl(paymentManagement);
        if(!StringUtils.isBlank(paymentManagement.getLocalCallback())){
            //?????????serverPath,??????usdt??????1?????????path
            paymentManagement.setCallbackPath(UUIDUtil.getServletPath(paymentManagement.getLocalCallback()));
        }
//        //????????????????????????????????????
//        switch (paymentManagement.getPayType2()){
//            case 1:
//            case 4:
//            case 5:
//                paymentManagement.setAreaId(1L);
//                break;
//            case 6:
//                paymentManagement.setAreaId(5L);
//                break;
//            case 2:
//                paymentManagement.setAreaId(2L);
//                break;
//        }
        save(paymentManagement);
        return paymentManagement;
    }

    @Override
    public Boolean delete(Long id) {
        boolean b = removeById(id);
        return b;
    }

    @Override
    public Boolean edit(BallPayBehalf paymentManagement, MultipartFile file) {
        setCallBackUrl(paymentManagement);
        if(!StringUtils.isBlank(paymentManagement.getLocalCallback())){
            //?????????serverPath,??????usdt??????1?????????path
            paymentManagement.setCallbackPath(UUIDUtil.getServletPath(paymentManagement.getLocalCallback()));
        }
//        uploadFile(paymentManagement,file);
        boolean b = updateById(paymentManagement);
        return b;
    }

    @Override
    public Boolean status(BallPayBehalf ballVip) {
        BallPayBehalf edit = BallPayBehalf.builder()
                .status(ballVip.getStatus())
                .build();
        edit.setId(ballVip.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        Boolean succ = edit(edit,null);
        return succ;
    }

    @Override
    public List<BallPayBehalf> findByCallback(String s) {
        QueryWrapper query = new QueryWrapper();
        query.eq("callback_path",s);
        return list(query);
    }

    @Override
    public void payCallBack(PayBehalfNoticeDtoIN payNotice) {
        apiLog.info("IN????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOrder_no());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getAttach())){
            apiLog.info("IN??????,??????IP??????:{}",payNotice.getAttach());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOrder_no();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if(payNotice.getPay_status()==2){
            //????????????
            edit.setStatus(4);
        }else if(payNotice.getPay_status()==4){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail(payNotice.getCancel_message());
        }else{
            return;
        }

        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    static void onWithdrawalSuccess(BallLoggerWithdrawal withdrawal, BallPlayer player,
                                    IBasePlayerService basePlayerService,
                                    IBallBalanceChangeService ballBalanceChangeService,
                                    BallAdminService adminService, IApiService apiService,
                                    IBallLoggerWithdrawalService loggerWithdrawalService,
                                    IBallBankCardService bankCardService) {
        while (true) {
            BallPlayer editPlayer = BallPlayer.builder()
                    .version(player.getVersion())
                    .build();
            if (player.getFirstReflect() == null || player.getFirstReflect() == 0) {
                editPlayer.setFirstReflect(withdrawal.getMoney());
            }
            //??????????????????
            editPlayer.setReflectTimes(player.getReflectTimes() == null ? 1 : player.getReflectTimes() + 1);
            //??????????????????
            editPlayer.setCumulativeReflect(player.getCumulativeReflect() == null ? withdrawal.getMoney() : player.getCumulativeReflect() + withdrawal.getMoney());
            //????????????
            if (player.getMaxReflect() == null || player.getMaxReflect() == 0 || withdrawal.getMoney() > player.getMaxReflect()) {
                editPlayer.setMaxReflect(withdrawal.getMoney());
            }
            editPlayer.setId(player.getId());
            //TODO ??????????????????
            long frozen = player.getFrozenWithdrawal()-withdrawal.getMoney();
            editPlayer.setFrozenWithdrawal(frozen<0?0:frozen);
            boolean b = basePlayerService.editAndClearCache(editPlayer, player);
            if (b) {
                //?????????????????????????????????
                BallBalanceChange change = ballBalanceChangeService.findByOrderId(2, withdrawal.getOrderNo());
                ballBalanceChangeService.edit(BallBalanceChange.builder()
                        .id(change.getId())
                        .frozenStatus(1)
                        .build());
                //????????????????????????????????????
                BallBankCard byPlayerId = bankCardService.findByPlayerId(player.getId());
                if(byPlayerId!=null){
                    bankCardService.withdrawalSuccess(byPlayerId.getId());
                }
//                BallPlayer finalPlayer = player;
//                ThreadPoolUtil.exec(new Runnable() {
//                    @Override
//                    public void run() {
//                        //TG?????? @?????????TG??????,??????????????????????????????tg???????????????
////                        List<BallAdmin> byTg = adminService.findByTg();
////                        Set<String> names = new HashSet<>();
////                        if(!byTg.isEmpty()){
////                            for(BallAdmin item:byTg){
////                                if(names.contains(item.getTgName())){
////                                    continue;
////                                }
////                                names.add(item.getTgName());
////                                String message = MessageFormat.format("@{0} ?????? {1}  ???????????? {2} ???????????? {3} ???????????? {4} ???????????? {5}",
////                                        item.getTgName(),
////                                        withdrawal.getPlayerName(),
////                                        withdrawal.getTopUsername(),
////                                        BigDecimalUtil.div(withdrawal.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT),
////                                        BallLoggerWithdrawal.getTypeString(withdrawal.getType()),
////                                        BigDecimalUtil.div(withdrawal.getUsdtMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT)
////                                );
////                                apiService.tgNotice(message);
////                            }
////                        }
//                        //????????????????????????playerName like topname
////                        if(finalPlayer.getAccountType()==1){
////                            return;
////                        }
////                        BallLoggerWithdrawal withdrawaldb = loggerWithdrawalService.findById(withdrawal.getId());
////                        if(StringUtils.isBlank(withdrawaldb.getTopUsername())){
////                            String message = MessageFormat.format("?????? {0}  ???????????? ??? ???????????? {1} ???????????? {2} ???????????? {3}",
////                                    withdrawaldb.getPlayerName(),
////                                    ""+BigDecimalUtil.div(withdrawaldb.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT),
////                                    BallLoggerWithdrawal.getTypeString(withdrawaldb.getType()),
////                                    ""+BigDecimalUtil.div(withdrawaldb.getUsdtMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT)
////                            );
////                            apiService.tgNotice(message);
////                        }else{
////                            List<BallAdmin> byPlayername = adminService.findByPlayername(withdrawaldb.getTopUsername());
////                            if(!byPlayername.isEmpty()){
////                                for(BallAdmin item:byPlayername){
////                                    if(names.contains(item.getTgName())){
////                                        continue;
////                                    }
////                                    if(StringUtils.isBlank(item.getTgName())){
////                                        // ????????????TGname??????
////                                        continue;
////                                    }
////                                    names.add(item.getTgName());
////                                    String message = MessageFormat.format("{0} ?????? {1}  ???????????? {2} ???????????? {3} ???????????? {4} ???????????? {5}",
////                                            StringUtils.isBlank(item.getTgName())?"":"@"+item.getTgName(),
////                                            withdrawaldb.getPlayerName(),
////                                            withdrawaldb.getTopUsername(),
////                                            ""+BigDecimalUtil.div(withdrawaldb.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT),
////                                            BallLoggerWithdrawal.getTypeString(withdrawaldb.getType()),
////                                            ""+BigDecimalUtil.div(withdrawaldb.getUsdtMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT)
////                                    );
////                                    apiService.tgNotice(message);
////                                }
////                            }else{
////                                String message = MessageFormat.format("?????? {0}  ???????????? {1} ???????????? {2} ???????????? {3} ???????????? {4}",
////                                        withdrawaldb.getPlayerName(),
////                                        withdrawaldb.getTopUsername(),
////                                        ""+BigDecimalUtil.div(withdrawaldb.getMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT),
////                                        BallLoggerWithdrawal.getTypeString(withdrawaldb.getType()),
////                                        ""+BigDecimalUtil.div(withdrawaldb.getUsdtMoney(),BigDecimalUtil.PLAYER_MONEY_UNIT)
////                                );
////                                apiService.tgNotice(message);
////                            }
//                        }
//                    }
//                });
                break;
            }else{
                player = basePlayerService.findById(player.getId());
            }
        }
    }

    @Override
    public synchronized BaseResponse pay(Long id, Long wid, BallAdmin admin) {
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findById(wid);
        if(withdrawal.getStatus()==5){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e30"));
        }
        BallPayBehalf ballPayBehalf = findById(id);
        if(ballPayBehalf.getPayType2()==3){
            //??????????????????
            Double rate = Double.valueOf(ballPayBehalf.getRate());
            //??????=????????????-?????????
            long sysMoney = withdrawal.getMoney()-withdrawal.getCommission();
            if(rate==null){
                rate=1d;
            }
            long money = Double.valueOf(BigDecimalUtil.div(sysMoney, rate)).longValue();
            //??????????????????????????????????????????
            loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                    .id(withdrawal.getId())
                    //????????????
                    .status(4)
                    .usdtMoney(money)
                    .usdtRate(Double.valueOf(rate*100).toString())
                    .updatedAt(System.currentTimeMillis())
                    .oker(admin.getUsername())
                    .behalfId(ballPayBehalf.getId())
                    .behalfTime(System.currentTimeMillis())
                    .build());
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService,loggerWithdrawalService,bankCardService);
            return BaseResponse.successWithMsg("");
        }

        if(ballPayBehalf.getStatus()!=1){
            return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e63"));
        }
        BallBankCard bankCard = bankCardService.findByPlayerId(withdrawal.getPlayerId());
        Double rate = Double.valueOf(ballPayBehalf.getRate());
        //??????=????????????-?????????
        long sysMoney = withdrawal.getMoney()-withdrawal.getCommission();
        if(rate==null){
            rate=1d;
        }
        long money = Double.valueOf(BigDecimalUtil.div(sysMoney, rate)).longValue();
        withdrawal.setUsdtRate(Double.valueOf(rate*100).toString());
        withdrawal.setUsdtMoney(money);
        //TODO ?????????1
        if(ballPayBehalf.getPayType2()==1){
            //??????pay
            return requestPayUrl(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==2){
            //??????
            return requestPayUrlCha(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==4){
            //??????fastpay
            return requestPayUrlFast(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==5){
            //??????UPI
            return requestPayUrlIn3(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==6){
            //??????wow
            return requestPayUrlWow(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==7){
            //??????allPay
            return requestPayUrlAllPay(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==8){
            //??????allPay
            return requestPayUrlTnz(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==9){
            //????????????meta
            return requestPayUrlMeta(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==10){
            //??????meta
            return requestPayUrlMetaGG(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==11){
            //??????-webpay
            return requestPayUrlWeb(ballPayBehalf,withdrawal,bankCard);
        }else if(ballPayBehalf.getPayType2()==12){
            //??????-xdpay
            return requestPayUrlXd(ballPayBehalf,withdrawal,bankCard);
        }
        else if(ballPayBehalf.getPayType2()==13){
            //??????-mppay
            return requestPayUrlMp(ballPayBehalf,withdrawal,bankCard);
        }
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e70"));
    }

    private BaseResponse requestPayUrlMp(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            Long areaId = ballPayBehalf.getAreaId();
            List<BallBank> banks = ballBankService.findByName(areaId,bankCard.getBankName());
            if(banks.isEmpty()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e31"));
            }
            BallBank bank = banks.get(0);
            String date = TimeUtil.longToStringYmd(System.currentTimeMillis(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            MultiValueMap<String,Object> payMap = new LinkedMultiValueMap<>();
            String div = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.add("mer_no", ballPayBehalf.getMerchantNo());
            payMap.add("settle_id", withdrawal.getOrderNo().toString());
            payMap.add("currency", ballPayBehalf.getDocumentNo());
            payMap.add("settle_amount", div);
            payMap.add("bankCode", bank.getBankCode());
            payMap.add("accountName", bankCard.getCardName());
            payMap.add("accountNo",bankCard.getCardNumber() );
            payMap.add("ifsc",bankCard.getBackEncoding() );
            payMap.add("settle_date",date);
            payMap.add("notifyUrl", ballPayBehalf.getLocalCallback());
            String signStr = "mer_no={0}&settle_id={1}&currency={2}" +
                    "&settle_amount={3}&bankCode={4}&accountName={5}&accountNo={6}" +
                    "&ifsc={7}&settle_date={8}&notifyUrl={9}&key={10}";
            String format = MessageFormat.format(signStr,
                    ballPayBehalf.getMerchantNo(),
                    withdrawal.getOrderNo().toString(),
                    ballPayBehalf.getDocumentNo(),
                    div,
                    bank.getBankCode(),
                    bankCard.getCardName(),
                    bankCard.getCardNumber(),
                    bankCard.getBackEncoding(),
                    date,
                    ballPayBehalf.getLocalCallback(),
                    ballPayBehalf.getPaymentKey());
            payMap.add("sign", PasswordUtil.genMd5(format).toLowerCase());
            apiLog.info("MP?????????????????????{}",payMap);
            apiLog.info("MP?????????????????????{}",format);
            String response = restHttpsUtil.doPost(url,payMap,MediaType.APPLICATION_FORM_URLENCODED);

            apiLog.info("MP????????????:{}",response);
            MpBehalfResponse payCallBackDto = JsonUtil.fromJson(response, MpBehalfResponse.class);
            if ("SUCCESS".equals(payCallBackDto.getCode())) {
                String id = payCallBackDto.getMer_no();
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(id)
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMessage())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error["+failMessage+"]");
    }

    private BaseResponse requestPayUrlXd(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            Map<String, Object> payMap = new HashMap<>();
            String div = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.put("amount", div);
            payMap.put("bankAccount",bankCard.getCardNumber() );
            payMap.put("customName", bankCard.getCardName());
            payMap.put("merchant", ballPayBehalf.getMerchantNo());
            payMap.put("notifyUrl", ballPayBehalf.getLocalCallback());
            payMap.put("orderId", withdrawal.getOrderNo().toString());
            payMap.put("payCode", ballPayBehalf.getDocumentType());
            payMap.put("remark", bankCard.getBackEncoding());
            String signStr = "amount={0}&bankAccount={1}&customName={2}" +
                    "&merchant={3}&notifyUrl={4}&orderId={5}&payCode={6}" +
                    "&remark={7}&key={8}";
            String format = MessageFormat.format(signStr,
                    div,
                    bankCard.getCardNumber(),
                    bankCard.getCardName(),
                    ballPayBehalf.getMerchantNo(),
                    ballPayBehalf.getLocalCallback(),
                    withdrawal.getOrderNo().toString(),
                    ballPayBehalf.getDocumentType(),
                    bankCard.getBackEncoding(),
                    ballPayBehalf.getPaymentKey());
            payMap.put("sign", PasswordUtil.genMd5(format).toLowerCase());
            apiLog.info("XD?????????????????????{}",payMap);
            String response = HttpUtil.doPost(url, null, JsonUtil.toJson(payMap));
            apiLog.info("XD????????????:{}",response);
            WebPayResponse payCallBackDto = JsonUtil.fromJson(response, WebPayResponse.class);
            if (payCallBackDto.getSuccess()&&payCallBackDto.getCode()==200) {
                String id = payCallBackDto.getData().get("platOrderId");
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(id)
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMsg())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error["+failMessage+"]");
    }

    private BaseResponse requestPayUrlWeb(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            Map<String, Object> payMap = new HashMap<>();
            String div = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.put("account",bankCard.getCardNumber() );
            payMap.put("amount", div);
            payMap.put("ifsc", bankCard.getBackEncoding());
            payMap.put("mchId", ballPayBehalf.getMerchantNo());
            payMap.put("notifyUrl", ballPayBehalf.getLocalCallback());
            payMap.put("orderNo", withdrawal.getOrderNo().toString());
            payMap.put("passageId", ballPayBehalf.getDocumentType());
            payMap.put("userName", bankCard.getCardName());
            String signStr = "account={0}&amount={1}&ifsc={2}" +
                    "&mchId={3}&notifyUrl={4}&orderNo={5}&passageId={6}" +
                    "&userName={7}&key={8}";
            String format = MessageFormat.format(signStr,
                    bankCard.getCardNumber(),
                    div,
                    bankCard.getBackEncoding(),
                    ballPayBehalf.getMerchantNo(),
                    ballPayBehalf.getLocalCallback(),
                    withdrawal.getOrderNo().toString(),
                    ballPayBehalf.getDocumentType(),
                    bankCard.getCardName(),
                    ballPayBehalf.getPaymentKey());
            payMap.put("sign", PasswordUtil.genMd5(format).toLowerCase());
            apiLog.info("WEBPAY?????????????????????{}",payMap);
            String response = HttpUtil.doPost(url, null, JsonUtil.toJson(payMap));
            apiLog.info("WEBPAY????????????:{}",response);
            WebPayResponse payCallBackDto = JsonUtil.fromJson(response, WebPayResponse.class);
            if (payCallBackDto.getSuccess()&&payCallBackDto.getCode()==200) {
                String id = payCallBackDto.getData().get("id");
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(id)
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMsg())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error["+failMessage+"]");
    }

    private BaseResponse requestPayUrlMetaGG(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("appid",ballPayBehalf.getMerchantNo());
            headerMap.put("appkey",ballPayBehalf.getPaymentKey());

            Map<String, Object> payMap = new HashMap<>();
            String div = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.put("orderId", withdrawal.getOrderNo().toString());
            payMap.put("orderMoney", div);
            payMap.put("notifyUrl", ballPayBehalf.getLocalCallback());
            payMap.put("customName", bankCard.getCardName());
            payMap.put("customMobile",bankCard.getCardNumber() );
            payMap.put("currencyType", ballPayBehalf.getDocumentType());

            apiLog.info("META_GG?????????????????????{}",payMap);
            String response = HttpUtil.doPost(url, headerMap, JsonUtil.toJson(payMap));
            apiLog.info("META_GG????????????:{}",response);
            PayResponseDtoMeta payCallBackDto = JsonUtil.fromJson(response, PayResponseDtoMeta.class);
            if (payCallBackDto.getCode()==200) {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo("")
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMsg())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error["+failMessage+"]");
    }

    private BaseResponse requestPayUrlMeta(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("appid",ballPayBehalf.getMerchantNo());
            headerMap.put("appkey",ballPayBehalf.getPaymentKey());

            Map<String, Object> payMap = new HashMap<>();
            String div = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.put("orderId", withdrawal.getOrderNo().toString());
            payMap.put("orderMoney", div);
            payMap.put("notifyUrl", ballPayBehalf.getLocalCallback());
            payMap.put("customName", bankCard.getCardName());
            payMap.put("customMobile",bankCard.getCardNumber() );
            payMap.put("currencyType", ballPayBehalf.getDocumentType());

            apiLog.info("META?????????????????????{}",payMap);
            String response = HttpUtil.doPost(url, headerMap, JsonUtil.toJson(payMap));
            apiLog.info("META????????????:{}",response);
            PayResponseDtoMeta payCallBackDto = JsonUtil.fromJson(response, PayResponseDtoMeta.class);
            if (payCallBackDto.getCode()==200) {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo("")
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMsg())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error:["+failMessage+"]");
    }

    private BaseResponse requestPayUrlTnz(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            String nowTime = TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            Map<String, Object> payMap = new HashMap<>();
            String div = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.put("accountNo",bankCard.getCardNumber() );
            payMap.put("actName", bankCard.getCardName());
            payMap.put("appId", ballPayBehalf.getDocumentType());
            payMap.put("channel", ballPayBehalf.getDocumentNo());
            payMap.put("mchId", ballPayBehalf.getMerchantNo());
            payMap.put("notifyUrl", ballPayBehalf.getLocalCallback());
            payMap.put("outTradeNo", withdrawal.getOrderNo().toString());
            payMap.put("requestTime", nowTime);
            payMap.put("signType", "MD5");
            payMap.put("subject",ballPayBehalf.getAccountAttach());
            payMap.put("transAmt", div);

            String signStr = "accountNo={0}&actName={1}&appId={2}&channel={3}&mchId={4}&notifyUrl={5}&outTradeNo={6}&requestTime={7}&signType=MD5&subject={8}&transAmt={9}&key={10}";
            String format = MessageFormat.format(signStr,
                    bankCard.getCardNumber(),
                    bankCard.getCardName(),
                    ballPayBehalf.getDocumentType(),
                    ballPayBehalf.getDocumentNo(),
                    ballPayBehalf.getMerchantNo(),
                    ballPayBehalf.getLocalCallback(),
                    withdrawal.getOrderNo().toString(),
                    nowTime,
                    ballPayBehalf.getAccountAttach(),
                    div,
                    ballPayBehalf.getPaymentKey());
            payMap.put("sign", PasswordUtil.genMd5(format).toUpperCase());
            apiLog.info("TNZ?????????????????????{}",payMap);
            String response = HttpUtil.doPost(url, null, JsonUtil.toJson(payMap));
            apiLog.info("TNZ????????????:{}",format, response);
            PayParamBackDtoTnz payCallBackDto = JsonUtil.fromJson(response, PayParamBackDtoTnz.class);
            if (payCallBackDto.getSuccess()) {
                Map<String, Object> result = payCallBackDto.getResult();
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(result.get("transNo").toString())
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMessage())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error:["+failMessage+"]");
    }

    private BaseResponse requestPayUrlAllPay(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
//            BallPlayer player = basePlayerService.findOne(withdrawal.getPlayerId());
            Long areaId = ballPayBehalf.getAreaId();
            List<BallBank> banks = ballBankService.findByName(areaId,bankCard.getBankName());
            if(banks.isEmpty()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e31"));
            }
            BallBank bank = banks.get(0);
            String timestamp = TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
            data.add("mch_id",ballPayBehalf.getMerchantNo());
            data.add("mch_transferId",withdrawal.getOrderNo());
            double div = BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT);
            data.add("transfer_amount",String.valueOf(div));
            data.add("apply_date",timestamp);
            data.add("bank_code",bank.getBankCode());
            data.add("receive_name",bankCard.getCardName());
            data.add("receive_account",bankCard.getCardNumber());
            data.add("back_url",ballPayBehalf.getLocalCallback());
            data.add("remark",bankCard.getBackEncoding());
//            data.add("receiver_telephone",bankCard.getPhone());
//            data.add("remark","11223344556677");
//            data.add("receiver_telephone","546778997");

            StringBuilder sb = new StringBuilder();
            sb.append("apply_date=");
            sb.append(timestamp);
            sb.append("&back_url=");
            sb.append(ballPayBehalf.getLocalCallback());
            sb.append("&bank_code=");
            sb.append(bank.getBankCode());
            sb.append("&mch_id=");
            sb.append(ballPayBehalf.getMerchantNo());
            sb.append("&mch_transferId=");
            sb.append(withdrawal.getOrderNo());
            sb.append("&receive_account=");
            sb.append(bankCard.getCardNumber());
            sb.append("&receive_name=");
            sb.append(bankCard.getCardName());
//            sb.append("&receiver_telephone=");
//            sb.append(bankCard.getPhone());
            sb.append("&remark=");
            sb.append(bankCard.getBackEncoding());
//            sb.append("11223344556677");
            sb.append("&transfer_amount=");
            sb.append(String.valueOf(div));
            sb.append("&key=");
            sb.append(ballPayBehalf.getPaymentKey());
            apiLog.info("ALL_PAY???????????????????????????{}",sb);
            String sign = PasswordUtil.genMd5(sb.toString()).toLowerCase();
            data.add("sign",sign);
            data.add("sign_type","MD5");
            apiLog.info("ALL_PAY?????????????????????{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.APPLICATION_FORM_URLENCODED);
            apiLog.info("ALL_PAY??????????????????:{}",response);
            PayBehalfCallBackDtoCHA payCallBackDto = JsonUtil.fromJson(response, PayBehalfCallBackDtoCHA.class);
            if("SUCCESS".equals(payCallBackDto.getRespCode())){
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(payCallBackDto.getTradeNo())
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            }else{
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getErrorMsg())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getErrorMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error["+failMessage+"]");
    }

    private BaseResponse requestPayUrlWow(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
//            BallPlayer player = basePlayerService.findOne(withdrawal.getPlayerId());
            Long areaId = ballPayBehalf.getAreaId();
            List<BallBank> banks = ballBankService.findByName(areaId,bankCard.getBankName());
            if(banks.isEmpty()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e31"));
            }
            BallBank bank = banks.get(0);
            String timestamp = TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
            data.add("mch_id",ballPayBehalf.getMerchantNo());
            data.add("mch_transferId",withdrawal.getOrderNo());
            double div = BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT);
            data.add("transfer_amount",String.valueOf(div));
            data.add("apply_date",timestamp);
            data.add("bank_code",bank.getBankCode());
            data.add("receive_name",bankCard.getCardName());
            data.add("receive_account",bankCard.getCardNumber());
            data.add("back_url",ballPayBehalf.getLocalCallback());
            data.add("remark",bankCard.getBackEncoding());
//            data.add("receiver_telephone",bankCard.getPhone());
//            data.add("remark","11223344556677");
//            data.add("receiver_telephone","546778997");

            StringBuilder sb = new StringBuilder();
            sb.append("apply_date=");
            sb.append(timestamp);
            sb.append("&back_url=");
            sb.append(ballPayBehalf.getLocalCallback());
            sb.append("&bank_code=");
            sb.append(bank.getBankCode());
            sb.append("&mch_id=");
            sb.append(ballPayBehalf.getMerchantNo());
            sb.append("&mch_transferId=");
            sb.append(withdrawal.getOrderNo());
            sb.append("&receive_account=");
            sb.append(bankCard.getCardNumber());
            sb.append("&receive_name=");
            sb.append(bankCard.getCardName());
//            sb.append("&receiver_telephone=");
//            sb.append(bankCard.getPhone());
            sb.append("&remark=");
            sb.append(bankCard.getBackEncoding());
//            sb.append("11223344556677");
            sb.append("&transfer_amount=");
            sb.append(String.valueOf(div));
            sb.append("&key=");
            sb.append(ballPayBehalf.getPaymentKey());
            apiLog.info("WOW???????????????????????????{}",sb);
            String sign = PasswordUtil.genMd5(sb.toString()).toLowerCase();
            data.add("sign",sign);
            data.add("sign_type","MD5");
            apiLog.info("WOW?????????????????????{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.APPLICATION_FORM_URLENCODED);
            apiLog.info("WOW??????????????????:{}",response);
            PayBehalfCallBackDtoCHA payCallBackDto = JsonUtil.fromJson(response, PayBehalfCallBackDtoCHA.class);
            if("SUCCESS".equals(payCallBackDto.getRespCode())){
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(payCallBackDto.getTradeNo())
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            }else{
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getErrorMsg())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getErrorMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error["+failMessage+"]");
    }

    private BaseResponse requestPayUrlIn3(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            MultiValueMap<String,Object> payMap = new LinkedMultiValueMap<>();
            String money = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.add("amount", money);
            payMap.add("bankAccount", bankCard.getCardNumber());
            String bankCode = null;
//            ifsc???ifsccode???
//            upi????????????upi
            if("ifsccode".equals(ballPayBehalf.getDocumentType())){
                bankCode = "ifsccode";
                payMap.add("bankCode", bankCode);
            }else if("upi".equals(ballPayBehalf.getDocumentType())){
                bankCode = "upi";
                payMap.add("bankCode", bankCode);
            }else{
                bankCode = bankCard.getBackEncoding();
                payMap.add("bankCode", bankCard.getBackEncoding());
            }
            payMap.add("extra", "extra");
            payMap.add("merchantNo", ballPayBehalf.getMerchantNo());
            payMap.add("name", bankCard.getCardName());
            payMap.add("notifyUrl", ballPayBehalf.getLocalCallback());
            payMap.add("outTradeNo", withdrawal.getOrderNo());
            payMap.add("type", ballPayBehalf.getDocumentType());
            String signStr = "amount={0}&bankAccount={1}&bankCode={2}&extra={3}&merchantNo={4}&name={5}&notifyUrl={6}&outTradeNo={7}&type={8}&signKey={9}";
            String format = MessageFormat.format(signStr,
                    money,
                    bankCard.getCardNumber(),
                    bankCode,
                    "extra",
                    ballPayBehalf.getMerchantNo(),
                    bankCard.getCardName(),
                    ballPayBehalf.getLocalCallback(),
                    withdrawal.getOrderNo().toString(),
                    ballPayBehalf.getDocumentType(),
                    ballPayBehalf.getPaymentKey());
            payMap.add("sign", PasswordUtil.genMd5(format).toUpperCase());
            String response = restHttpsUtil.doPost(url,payMap,MediaType.MULTIPART_FORM_DATA);
            apiLog.info("IN3????????????:{}:",format, response);
            PayBehalfCallBackDto3 payCallBackDto = JsonUtil.fromJson(response, PayBehalfCallBackDto3.class);
            if (payCallBackDto.getStatus()==0) {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(payCallBackDto.getData().getTradeId())
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail("error code: "+ payCallBackDto.getCode())
                        .build());
                return BaseResponse.successWithMsg("error code: "+ payCallBackDto.getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error:["+failMessage+"]");
    }

    private BaseResponse requestPayUrlFast(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            //account=UpiAccount&amount=100&
            // ext=x&merchantNo=1178632860589223937&
            // name=UpiName &notifyUrl=http://localhost/okexadmin/merchant/simulatormerchantorder/orderNotifyV2
            // &orderNo=1399329000311230465 &type=8
            // &version=2.0.0&key=9656c0292f081a08a16a4b7475a8a5e5
            Map<String, Object> payMap = new HashMap<>();
            String signStr = "";
            String div = String.valueOf(BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT));
            payMap.put("amount", div);
            payMap.put("ext", "ext");
            payMap.put("merchantNo", ballPayBehalf.getMerchantNo());
            payMap.put("notifyUrl", ballPayBehalf.getLocalCallback());
            payMap.put("orderNo", withdrawal.getOrderNo().toString());
            payMap.put("type", ballPayBehalf.getDocumentType());
            payMap.put("name", bankCard.getCardName());
            if("1".equals(ballPayBehalf.getDocumentType())){
                payMap.put("ifscCode", bankCard.getBackEncoding());
                signStr = "account={0}&amount={1}&ext=ext&ifscCode={2}&merchantNo={3}&name={4}&notifyUrl={5}&orderNo={6}&type={7}&version=2.0.0&key={8}";
            }else{
                signStr = "account={0}&amount={1}&ext=ext&merchantNo={3}&name={4}&notifyUrl={5}&orderNo={6}&type={7}&version=2.0.0&key={8}";
            }
            payMap.put("account", bankCard.getCardNumber());
            payMap.put("version", "2.0.0");
            String format = MessageFormat.format(signStr,
                    bankCard.getCardNumber(),
                    div,
                    bankCard.getBackEncoding(),
                    ballPayBehalf.getMerchantNo(),
                    bankCard.getCardName(),
                    ballPayBehalf.getLocalCallback(),
                    withdrawal.getOrderNo().toString(),
                    ballPayBehalf.getDocumentType(),
                    ballPayBehalf.getPaymentKey());
            payMap.put("sign", PasswordUtil.genMd5(format).toUpperCase());
            String response = HttpUtil.doPost(url, null, JsonUtil.toJson(payMap));
            apiLog.info("FAST????????????:{}:",format, response);
            PayParamBackDtoFast payCallBackDto = JsonUtil.fromJson(response, PayParamBackDtoFast.class);
            if ("0".equals(payCallBackDto.getCode())) {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(payCallBackDto.getPlatformOrderNo())
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            } else {
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMessage())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error:["+failMessage+"]");
    }
    @Override
    public void payCallBackFast(PayCallBackDtoFast payNotice) {
        apiLog.info("FAST????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOrderNo());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("FAST??????,??????IP??????:{}",payNotice.getSign());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOrderNo();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("1".equals(payNotice.getStatus())){
            //????????????
            edit.setStatus(4);
        }else if("3".equals(payNotice.getStatus())){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail(payNotice.getMsg());
        }else{
            return;
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackIn3(PayBehalfNoticeDto3 payNotice) {
        apiLog.info("IN3????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOutTradeNo());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("IN3??????,??????IP??????:{}",payNotice.getSign());
            return;
        }
        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOutTradeNo();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if(payNotice.getStatus()==1){
            //????????????
            edit.setStatus(4);
        }else{
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail(payNotice.getStatus().toString());
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackWow(PayBehalfNoticeDtoCHA payNotice) {
        apiLog.info("WOW????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getMerTransferId());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("WOW??????,??????IP??????:{}",payNotice.getSign());
            return;
        }
        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getMerTransferId();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("1".equals(payNotice.getTradeResult())){
            //????????????
            edit.setStatus(4);
        }else if("2".equals(payNotice.getTradeResult())){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail(payNotice.getRespCode());
        }

        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackAllPay(PayBehalfNoticeDtoCHA payNotice) {
        apiLog.info("All_PAY????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getMerTransferId());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("All_PAY??????,??????IP??????:{}",payNotice.getSign());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getMerTransferId();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("1".equals(payNotice.getTradeResult())){
            //????????????
            edit.setStatus(4);
        }else if("2".equals(payNotice.getTradeResult())){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail(payNotice.getRespCode());
        }

        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackTNZ(PayCallBackDtoTnz payNotice) {
        apiLog.info("TNZ????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOutTradeNo());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getExtInfo())){
            apiLog.info("TNZ??????,??????IP??????:{}",payNotice.getExtInfo());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOutTradeNo();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("SUCCESS".equals(payNotice.getTransStatus())){
            //????????????
            edit.setStatus(4);
        }else if("FAIL".equals(payNotice.getTransStatus())){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail(payNotice.getFailReason());
        }else{
            return;
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackMETA(PayCallBackDtoMeta payNotice) {
        apiLog.info("META????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOrderId());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getExtInfo())){
            apiLog.info("META??????,??????IP??????:{}",payNotice.getExtInfo());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOrderId();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("2".equals(payNotice.getStatus())){
            //????????????
            edit.setStatus(4);
        }else{
            return;
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackMETAGG(PayCallBackDtoMeta payNotice) {
        apiLog.info("META_GG????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOrderId());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getExtInfo())){
            apiLog.info("META_GG??????,??????IP??????:{}",payNotice.getExtInfo());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOrderId();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("2".equals(payNotice.getStatus())){
            //????????????
            edit.setStatus(4);
        }else{
            return;
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public List<BallPayBehalf> findByAreaCode(Long id) {
        BallPlayer byId1 = basePlayerService.findById(id);
        List<BallCountry> countries = countryService.findByCode(byId1.getAreaCode());

        List<Long> cids = new ArrayList<>();
        for(BallCountry item:countries){
            cids.add(item.getId());
        }
        List<BallPayBehalf> list = findByCountryIds(cids);
        //?????????????????????
        BallBankCard bankCard = bankCardService.findByPlayerId(id);
        List<BallPayBehalf> listb = new ArrayList<>();
        for(BallPayBehalf item:list){
            if(item.getAreaId()==null){
                listb.add(item);
            }else{
                if(bankCard==null){
                    continue;
                }
                //
                List<BallBank> byName = ballBankService.findByName(item.getAreaId(), bankCard.getBankName());
                if(byName!=null&&!byName.isEmpty()&&byName.get(0)!=null){
                    listb.add(item);
                }
            }
        }
        return listb;
    }

    @Override
    public void payCallBackWeb(WebPayCallback payNotice) {
        apiLog.info("WEBPAY????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOrderNo());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("WEBPAY??????,??????IP??????:{}",payNotice.getSign());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOrderNo();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if(payNotice.getPayStatus()==1){
            //????????????
            edit.setStatus(4);
        }else if(payNotice.getPayStatus()==2){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail("");
        }else{
            return;
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackXD(XdPayCallBack payNotice) {
        apiLog.info("XD????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOrderId());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("XD??????,??????IP??????:{}",payNotice.getSign());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOrderId();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if(payNotice.getStatus()==1){
            //????????????
            edit.setStatus(4);
        }else if(payNotice.getStatus()==2){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail("");
        }else{
            return;
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public void payCallBackMp(MpPayCallBack payNotice) {
        apiLog.info("MP????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getOrderNo());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("MP??????,??????IP??????:{}",payNotice.getSign());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getOrderNo();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("1".equals(payNotice.getPayResult())){
            //????????????
            edit.setStatus(4);
        }else if("2".equals(payNotice.getPayResult())){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail("");
        }else{
            return;
        }
        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    private List<BallPayBehalf> findByCountryIds(List<Long> cids) {
        QueryWrapper<BallPayBehalf> queryWrapper = new QueryWrapper();
        if(cids.isEmpty()){
            queryWrapper.eq("pay_type",3);
        }else{
            queryWrapper.and(QueryWrapper -> QueryWrapper.in("country_id",cids)
                    .or().eq("pay_type",3));
        }
        queryWrapper.eq("status",1);
        return list(queryWrapper);
    }

    private BaseResponse requestPayUrlCha(BallPayBehalf ballPayBehalf, BallLoggerWithdrawal withdrawal, BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
//            BallPlayer player = basePlayerService.findOne(withdrawal.getPlayerId());
            List<BallBank> banks = ballBankService.findByName(bankCard.getBankName());
            if(banks.isEmpty()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e31"));
            }
            BallBank bank = banks.get(0);
            String timestamp = TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
            data.add("mch_id",ballPayBehalf.getMerchantNo());
            data.add("mch_transferId",withdrawal.getOrderNo());
            double div = BigDecimalUtil.div(withdrawal.getUsdtMoney(), BigDecimalUtil.PLAYER_MONEY_UNIT);
            data.add("transfer_amount",String.valueOf(div));
            data.add("apply_date",timestamp);
            data.add("bank_code",bank.getBankCode());
            data.add("receive_name",bankCard.getCardName());
            data.add("receive_account",bankCard.getCardNumber());
            data.add("back_url",ballPayBehalf.getLocalCallback());
            data.add("remark",bankCard.getIdentityCard());
            data.add("receiver_telephone",bankCard.getPhone());
//            data.add("remark","11223344556677");
//            data.add("receiver_telephone","546778997");

            StringBuilder sb = new StringBuilder();
            sb.append("apply_date=");
            sb.append(timestamp);
            sb.append("&back_url=");
            sb.append(ballPayBehalf.getLocalCallback());
            sb.append("&bank_code=");
            sb.append(bank.getBankCode());
            sb.append("&mch_id=");
            sb.append(ballPayBehalf.getMerchantNo());
            sb.append("&mch_transferId=");
            sb.append(withdrawal.getOrderNo());
            sb.append("&receive_account=");
            sb.append(bankCard.getCardNumber());
            sb.append("&receive_name=");
            sb.append(bankCard.getCardName());
            sb.append("&receiver_telephone=");
//            sb.append("546778997");
            sb.append(bankCard.getPhone());
            sb.append("&remark=");
            sb.append(bankCard.getIdentityCard());
//            sb.append("11223344556677");
            sb.append("&transfer_amount=");
            sb.append(String.valueOf(div));
            sb.append("&key=");
            sb.append(ballPayBehalf.getPaymentKey());
            apiLog.info("CHA???????????????????????????{}",sb);
            String sign = PasswordUtil.genMd5(sb.toString()).toLowerCase();
            data.add("sign",sign);
            data.add("sign_type","MD5");
            apiLog.info("CHA?????????????????????{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.APPLICATION_FORM_URLENCODED);
            apiLog.info("CHA??????????????????:{}",response);
            PayBehalfCallBackDtoCHA payCallBackDto = JsonUtil.fromJson(response, PayBehalfCallBackDtoCHA.class);
            if("SUCCESS".equals(payCallBackDto.getRespCode())){
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(payCallBackDto.getTradeNo())
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            }else{
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getErrorMsg())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getErrorMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error["+failMessage+"]");
    }

    @Override
    public void payCallBackCha(PayBehalfNoticeDtoCHA payNotice) {
        apiLog.info("CHA????????????:{}",payNotice);
        //?????????????????????,????????????
        BallLoggerWithdrawal withdrawal = loggerWithdrawalService.findByOrderNo(payNotice.getMerTransferId());
        if(withdrawal==null){
            return;
        }
        BallPayBehalf byId = payBehalfService.findById(withdrawal.getBehalfId());
        if(byId!=null&&!StringUtils.isBlank(byId.getWhiteIp())
                &&!byId.getWhiteIp().contains(payNotice.getSign())){
            apiLog.info("CHA??????,??????IP??????:{}",payNotice.getSign());
            return;
        }

        //????????????
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payNotice.getMerTransferId();
        Object o = redisUtil.get(key);
        if(o!=null){
            return ;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        if(incr>1){
            return ;
        }

        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(withdrawal.getId())
                .build();
        edit.setOker("sys");
        edit.setUpdatedAt(System.currentTimeMillis());
        if("1".equals(payNotice.getTradeResult())){
            //????????????
            edit.setStatus(4);
        }else if("2".equals(payNotice.getTradeResult())){
            //????????????
            edit.setStatus(6);
            edit.setRemarkFail(payNotice.getRespCode());
        }

        Boolean res = loggerWithdrawalService.edit(edit);
        if(res && edit.getStatus()==4) {
            BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
            onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, loggerWithdrawalService,bankCardService);
        }
    }

    @Override
    public List<BallPayBehalf> findByBankCode(String bankCode, Integer type) {
        // type =1 ???????????????
        // type=2 USDT??????
        // type=3 sim??????

        List<BallBank> byCode = ballBankService.findByName(bankCode);
        Set<Long> areaIds = new HashSet<>();
        for(BallBank item:byCode){
            areaIds.add(item.getAreaId());
        }
        QueryWrapper<BallPayBehalf> query = new QueryWrapper();
        if(!areaIds.isEmpty()){
            query.and(QueryWrapper -> QueryWrapper.in("area_id",areaIds)
                    .or().eq("pay_type",3));
        }else{
            query.eq("pay_type",3);
        }
        query.eq("status",1);
        List<BallPayBehalf> list = list(query);
        Set<BallPayBehalf> set = new HashSet<>(list);
        if(type!=null&&type==1){
            //??????,
            List<BallPayBehalf> byType = findByType(5);
            set.addAll(byType);
            //????????????tnz?????????
            byType = findByType(8);
            set.addAll(byType);
        } else if(type!=null&&type==3){
            List<BallPayBehalf> byType = findByType(9);
            set.addAll(byType);
        }
        return new ArrayList<>(set);
    }

    private BaseResponse requestPayUrl(BallPayBehalf ballPayBehalf,BallLoggerWithdrawal withdrawal,BallBankCard bankCard) {
        String url = ballPayBehalf.getServerUrl();
        String failMessage=null;
        try {
            String bankCode = "10170";
            Long timestamp = TimeUtil.getNowTimeSec();
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
            data.add("account_name",bankCard.getCardName());
            data.add("account_no",bankCard.getCardNumber());
            //TODO ?????????????????? 10143
            data.add("bank_code",bankCode);
            data.add("bank_ifsc",bankCard.getBackEncoding());
            data.add("merchant_no",ballPayBehalf.getMerchantNo());
            data.add("notice_url",ballPayBehalf.getLocalCallback());
            data.add("order_amount",withdrawal.getUsdtMoney());
            data.add("order_no",withdrawal.getOrderNo());
            data.add("timestamp",timestamp);
//            data.add("attach","");
            StringBuilder sb = new StringBuilder();
            sb.append("account_name=");
            sb.append(bankCard.getCardName());
            sb.append("&account_no=");
            sb.append(bankCard.getCardNumber());
            sb.append("&bank_code=");
            sb.append(bankCode);
            sb.append("&bank_ifsc=");
            sb.append(bankCard.getBackEncoding());
            sb.append("&merchant_no=");
            sb.append(ballPayBehalf.getMerchantNo());
            sb.append("&notice_url=");
            sb.append(ballPayBehalf.getLocalCallback());
            sb.append("&order_amount=");
            sb.append(withdrawal.getUsdtMoney());
            sb.append("&order_no=");
            sb.append(withdrawal.getOrderNo());
            sb.append("&timestamp=");
            sb.append(timestamp);
            sb.append("&key=");
            sb.append(ballPayBehalf.getPaymentKey());
            apiLog.info("IN???????????????????????????{}",sb);
            String sign = PasswordUtil.genMd5(sb.toString()).toLowerCase();
            data.add("sign",sign);
            apiLog.info("IN?????????????????????{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.APPLICATION_FORM_URLENCODED);
            apiLog.info("IN??????????????????:{}",response);
            PayBehalfCallBackDtoIN payCallBackDto = JsonUtil.fromJson(response, PayBehalfCallBackDtoIN.class);
            if(payCallBackDto.getCode()==0){
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .status(5)
                        .behalfNo(payCallBackDto.getData().getTradeNo())
                        .usdtMoney(withdrawal.getUsdtMoney())
                        .usdtRate(withdrawal.getUsdtRate())
                        .remarkFail("")
                        .behalfId(ballPayBehalf.getId())
                        .behalfTime(System.currentTimeMillis())
                        .build());
                return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e64"));
            }else{
                loggerWithdrawalService.edit(BallLoggerWithdrawal.builder()
                        .id(withdrawal.getId())
                        .remarkFail(payCallBackDto.getMessage())
                        .build());
                return BaseResponse.successWithMsg(payCallBackDto.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            failMessage = e.getMessage();
        }
        return BaseResponse.failedWithMsg("error:["+failMessage+"]");
    }

//    private void uploadFile(BallPayBehalf payBehalf, MultipartFile file) {
//        String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
//        if(file!=null && !file.isEmpty()){
//            String webpath = "payImg/";
//            String fileRootPath = rootPath+webpath;
//            File fileRoot = new File(fileRootPath);
//            if(!fileRoot.exists()){
//                fileRoot.mkdirs();
//            }
//            String originalFilename = file.getOriginalFilename();
//            //??????
//            String subfex = originalFilename.substring(originalFilename.lastIndexOf("."));
//            String saveName = UUIDUtil.getUUID()+subfex;
//            try {
//                InputStream inputStream = file.getInputStream();
//                String savePath = fileRootPath+saveName;
//                FileOutputStream fos = new FileOutputStream(savePath);
//                byte[] b = new byte[128];
//                int len;
//                while((len = inputStream.read(b))!=-1){
//                    fos.write(b,0,len);
//                }
//                fos.flush();
//                fos.close();
//                inputStream.close();
//                payBehalf.setImg(webpath+saveName);
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
//        }
//    }
}
