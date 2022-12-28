package com.oxo.ball.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.oxo.ball.OxoMainApplication;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.api.*;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.controller.player.PlayerAuthController;
import com.oxo.ball.service.pay.IPlayerPayService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.admin.ApiServiceImpl;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerGameService;
import com.oxo.ball.utils.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@RestController()
@RequestMapping("test")
public class TestController {

    @Resource
    BallMenuService ballMenuService;

    @Resource
    IPlayerBetService betService;
    @Autowired
    IBallBetService ballBetService;

    @Autowired
    IBallGameLossPerCentService lossPerCentService;

    @Autowired
    IBallGameService gameService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    IBallBankService bankService;
    @Autowired
    ApiServiceImpl apiService;
    @Autowired
    IPlayerPayService playerPayService;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    IBallProxyLoggerService proxyLoggerService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    RestHttpsUtil restHttpsUtil;
    @Autowired
    IBallGameReportService gameReportService;
    @Autowired
    IPlayerGameService playerGameService;
    @Autowired
    IBallTimezoneService timezoneService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    IBallPlayerService ballPlayerService;
    @Autowired
    private IBallSystemConfigService systemConfigService;

    @GetMapping("v")
    public Object setR() {
        return BaseResponse.successWithMsg("version:" + OxoMainApplication.global_version
                + ",timezone:" + TimeUtil.TIME_ZONE.getID()
                + ",now:" + TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS)
                + ",md5:" + PasswordUtil.genMd5("abcdefg")
                + ",chatset:" + Charset.defaultCharset().name()
        );
    }

    @GetMapping("vv")
    public Object getR() {
        return BaseResponse.successWithData(MapUtil.newMap("v", OxoMainApplication.html_version));
    }

    @GetMapping("t")
    public Object getTimeZone() {
        BallTimezone byStatusOn = timezoneService.findByStatusOn();
        byStatusOn.setId(System.currentTimeMillis());
        return BaseResponse.successWithData(byStatusOn);
    }

    @GetMapping("nnoott")
    public Object notOpen() {
//        List<BallBet> byGameId = ballBetService.findByGameId(926862L, 0);
        return BaseResponse.successWithData("");
    }

    @GetMapping("autotest")
    public Object autotest() {
//        BallGame game = playerGameService.findOne(836426L);
//        BallGame edit = BallGame.builder()
//                .id(836426L)
////                .id(game.getId())
////                .homeHalf(1)
////                .guestHalf(1)
////                .homeFull(1)
////                .guestFull(1)
////                .homeOvertime(1)
////                .guestOvertime(1)
////                .homePenalty(1)
////                .guestPenalty(1)
//                .build();
//        ballBetService.betOpen(edit,false);
//        ballPlayerService.dayReward();
//        List<BallBank> banks = bankService.findByName(1L,"State Bank of India");
//        createOdds();
        String key = RedisKeyContant.PLAYER_PAY_CALLBACK + 110;
        Object o = redisUtil.get(key);
        if(o!=null){
            System.out.println(o);
            return BaseResponse.SUCCESS;
        }
        long incr = redisUtil.incr(key, 1);
        redisUtil.expire(key,5);
        System.out.println(incr);
        if(incr>1){
            return BaseResponse.SUCCESS;
        }
        return BaseResponse.successWithData("ok~");
    }

    private void createOdds() {
        try {
            FileReader fr = new FileReader("D:\\tgfile\\games.txt");
            BufferedReader br = new BufferedReader(fr);
            StringBuilder jsonString = new StringBuilder();
            String temp = null;
            while ((temp = br.readLine()) != null) {
                jsonString.append(temp);
            }
            br.close();
            br.close();
            BallSystemConfig systemConfig = systemConfigService.getSystemConfig();

            List<BallGame> ballGames = new ArrayList<>();
            ApiFixtures apiFixtures = JsonUtil.fromJson(jsonString.toString(), ApiFixtures.class);
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
                        .finishTime(fixture.getTimestamp() * 1000 + TimeUtil.TIME_ONE_MIN * systemConfig.getGameFinishMin())
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
                if (System.currentTimeMillis() + TimeUtil.TIME_ONE_HOUR > game.getStartTime()) {
                    continue;
                }
                //初始保存
                try {
                    redisUtil.leftSet(RedisKeyContant.GAME_NEED_QUERY_ODDS, game);
                    ballGames.add(game);
                    count++;
                } catch (Exception ex) {
                }
            }
            System.out.println("API读取到[" + count + "]场可用赛事");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @GetMapping("r1")
//    public Object setR(){
//        redisUtil.set("tt","1",10);
//        return BaseResponse.successWithMsg(""+redisUtil.getExpire("tt"));
//    }
//    @GetMapping("r2")
//    public Object setR(Long gameId,String json){
//        apiService.refreshOddsTest(gameId,json);
//        return BaseResponse.successWithMsg("ok");
//    }
//    @GetMapping("r4")
//    public Object setR4(){
//        proxyLoggerService.statisEveryDay();
//        return BaseResponse.successWithMsg("ok");
//    }
//    @GetMapping("r5")
//    public Object setR5(HttpServletRequest request){
//        String ipAddr = GeoLiteUtil.getIpAddr(IpUtil.getIpAddress(request));
//        return BaseResponse.successWithMsg(ipAddr+TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS)+loggerRechargeService.getDayOrderNo());
//    }
//    @RequestMapping("r6")
//    public Object setR6(HttpServletRequest request){
//        String s = restHttpsUtil.doGet("https://www.baidu.com/",null);
//        return BaseResponse.successWithMsg(s);
//    }
//    @RequestMapping("r7")
//    public Object setR6(PayRequestDtoIN payRequest,HttpServletRequest request){
//        return BaseResponse.successWithData(payRequest);
//    }
    @RequestMapping("r8")
    public Object setR8(HttpServletRequest request) {
//        gameReportService.dayStatis();
//        for(int i=0;i<10;i++){
//            String forObject = restTemplate.getForObject("http://localhost:10100/player/pay/callback/2", String.class);
//            System.out.println(forObject);
//        }
        return BaseResponse.successWithMsg("ok");
    }
//    @GetMapping("r3")
//    public Object testPay(){
////        String response = playerPayService.requestPayUrl(PayParamDto.builder()
////                .platformOrder(String.valueOf(System.currentTimeMillis()))
////                .username("player1")
////                .rechargeType(PayParamDto.PAY_TYPE_USDT)
////                .accountOrders(100F)
////                .backUrl(someConfig.getPayCallbackUrl())
////                .build());
//        return BaseResponse.successWithMsg("");
//    }


    //    @GetMapping("orderno")
//    public Object orderNo(){
//        return betService.getDayOrderNo();
//    }
//    @GetMapping("bank")
//    public Object bank() throws IOException {
//        String json = "[{\"bankCname\":\"Access Bank (Diamond)\",\"bankCode\":\"ACCBANKD\",\"bankId\":1000002},{\"bankCname\":\"ALAT by WEMA\",\"bankCode\":\"ALATBW\",\"bankId\":1000003},{\"bankCname\":\"ASO Savings and Loans\",\"bankCode\":\"ASOSAL\",\"bankId\":1000004},{\"bankCname\":\"CEMCS Microfinance Bank\",\"bankCode\":\"CEMCSB\",\"bankId\":1000006},{\"bankCname\":\"Citibank Nigeria\",\"bankCode\":\"CITIBANK\",\"bankId\":1000007},{\"bankCname\":\"Coronation Merchant Bank\",\"bankCode\":\"CMBANK\",\"bankId\":1000008},{\"bankCname\":\"Ecobank Nigeria\",\"bankCode\":\"ECOBANK\",\"bankId\":1000009},{\"bankCname\":\"Ekondo Microfinance Bank\",\"bankCode\":\"EkondoMB\",\"bankId\":1000010},{\"bankCname\":\"Eyowo\",\"bankCode\":\"EYOWO\",\"bankId\":1000011},{\"bankCname\":\"Fidelity Bank\",\"bankCode\":\"FIDELITYB\",\"bankId\":1000012},{\"bankCname\":\"First Bank of Nigeria\",\"bankCode\":\"FIRSTBANK\",\"bankId\":1000013},{\"bankCname\":\"First City Monument Bank\",\"bankCode\":\"FIRSTCMB\",\"bankId\":1000014},{\"bankCname\":\"FSDH Merchant Bank Limited\",\"bankCode\":\"FSDHMBL\",\"bankId\":1000015},{\"bankCname\":\"Globus Bank\",\"bankCode\":\"GLOBUSB\",\"bankId\":1000016},{\"bankCname\":\"Guaranty Trust Bank\",\"bankCode\":\"GUARATYTB\",\"bankId\":1000017},{\"bankCname\":\"Hackman Microfinance Bank\",\"bankCode\":\"HACKMANMB\",\"bankId\":1000018},{\"bankCname\":\"Hasal Microfinance Bank\",\"bankCode\":\"HASALMB\",\"bankId\":1000019},{\"bankCname\":\"Heritage Bank\",\"bankCode\":\"HERITAGEB\",\"bankId\":1000020},{\"bankCname\":\"Ibile Microfinance Bank\",\"bankCode\":\"IBILEMB\",\"bankId\":1000021},{\"bankCname\":\"Infinity MFB\",\"bankCode\":\"INFMFB\",\"bankId\":1000022},{\"bankCname\":\"Jaiz Bank\",\"bankCode\":\"JAIZB\",\"bankId\":1000023},{\"bankCname\":\"Keystone Bank\",\"bankCode\":\"KEYBANK\",\"bankId\":1000024},{\"bankCname\":\"Kuda Bank\",\"bankCode\":\"KUDABANK\",\"bankId\":1000025},{\"bankCname\":\"Mayfair MFB\",\"bankCode\":\"MAYMFB\",\"bankId\":1000026},{\"bankCname\":\"One Finance\",\"bankCode\":\"ONEFINANCE\",\"bankId\":1000027},{\"bankCname\":\"Parallex Bank\",\"bankCode\":\"PARABANK\",\"bankId\":1000028},{\"bankCname\":\"Parkway - ReadyCash\",\"bankCode\":\"PKARC\",\"bankId\":1000029},{\"bankCname\":\"Petra Mircofinance Bank Plc\",\"bankCode\":\"PETRAMBP\",\"bankId\":1000030},{\"bankCname\":\"Polaris Bank\",\"bankCode\":\"POLARISB\",\"bankId\":1000031},{\"bankCname\":\"Providus Bank\",\"bankCode\":\"PROVIDUSB\",\"bankId\":1000032},{\"bankCname\":\"Rand Merchant Bank\",\"bankCode\":\"RANDMB\",\"bankId\":1000033},{\"bankCname\":\"Rubies MFB\",\"bankCode\":\"RUBIESMFB\",\"bankId\":1000034},{\"bankCname\":\"Sparkle Microfinance Bank\",\"bankCode\":\"SPARKBANK\",\"bankId\":1000035},{\"bankCname\":\"Stanbic IBTC Bank\",\"bankCode\":\"STANBICB\",\"bankId\":1000036},{\"bankCname\":\"Standard Chartered Bank\",\"bankCode\":\"STANDARDCB\",\"bankId\":1000037},{\"bankCname\":\"Sterling Bank\",\"bankCode\":\"STERLINGB\",\"bankId\":1000038},{\"bankCname\":\"Suntrust Bank\",\"bankCode\":\"SUNTRUST\",\"bankId\":1000039},{\"bankCname\":\"TAJ Bank\",\"bankCode\":\"TAJBANK\",\"bankId\":1000040},{\"bankCname\":\"TCF MFB\",\"bankCode\":\"TCFMFB\",\"bankId\":1000041},{\"bankCname\":\"Titan Bank\",\"bankCode\":\"TITANB\",\"bankId\":1000042},{\"bankCname\":\"Union Bank of Nigeria\",\"bankCode\":\"UNIONBON\",\"bankId\":1000043},{\"bankCname\":\"United Bank For Africa\",\"bankCode\":\"UNITEDBFA\",\"bankId\":1000044},{\"bankCname\":\"Unity Bank\",\"bankCode\":\"UNITYB\",\"bankId\":1000045},{\"bankCname\":\"VFD Microfinance Bank Limited\",\"bankCode\":\"VFDMBL\",\"bankId\":1000046},{\"bankCname\":\"Wema Bank\",\"bankCode\":\"WEMABANK\",\"bankId\":1000047},{\"bankCname\":\"Zenith Bank\",\"bankCode\":\"ZENITHB\",\"bankId\":1000048},{\"bankCname\":\"FINATRUST MICROFINANCE\",\"bankCode\":\"FINATRUST MICROFINANCE\",\"bankId\":1000113},{\"bankCname\":\"Rubies Microfinance\",\"bankCode\":\"Rubies Microfinance\",\"bankId\":1000114},{\"bankCname\":\"Rand merchant Bank\",\"bankCode\":\"Rand merchant Bank\",\"bankId\":1000115},{\"bankCname\":\"Paga\",\"bankCode\":\"Paga\",\"bankId\":1000116},{\"bankCname\":\"GoMoney\",\"bankCode\":\"GoMoney\",\"bankId\":1000117},{\"bankCname\":\"AMJU Unique Microfinance Bank\",\"bankCode\":\"AMJU Unique Microfinance Bank\",\"bankId\":1000118},{\"bankCname\":\"BRIDGEWAY MICROFINANCE BANK\",\"bankCode\":\"BRIDGEWAY MICROFINANCE BANK\",\"bankId\":1000119},{\"bankCname\":\"Mint-Finex MICROFINANCE BANK\",\"bankCode\":\"Mint-Finex MICROFINANCE BANK\",\"bankId\":1000120},{\"bankCname\":\"Covenant Microfinance Bank\",\"bankCode\":\"Covenant Microfinance Bank\",\"bankId\":1000121},{\"bankCname\":\"PatrickGold Microfinance Bank\",\"bankCode\":\"PatrickGold Microfinance Bank\",\"bankId\":1000122},{\"bankCname\":\"NPF MicroFinance Bank\",\"bankCode\":\"NPF MicroFinance Bank\",\"bankId\":1000123},{\"bankCname\":\"PayAttitude Online\",\"bankCode\":\"PayAttitude Online\",\"bankId\":1000124},{\"bankCname\":\"Intellifin\",\"bankCode\":\"Intellifin\",\"bankId\":1000125},{\"bankCname\":\"Contec Global Infotech Limited (NowNow)\",\"bankCode\":\"Contec Global Infotech Limited (NowNow)\",\"bankId\":1000126},{\"bankCname\":\"FCMB Easy Account\",\"bankCode\":\"FCMB Easy Account\",\"bankId\":1000127},{\"bankCname\":\"EcoMobile\",\"bankCode\":\"EcoMobile\",\"bankId\":1000128},{\"bankCname\":\"Innovectives Kesh\",\"bankCode\":\"Innovectives Kesh\",\"bankId\":1000129},{\"bankCname\":\"Zinternet Nigera Limited\",\"bankCode\":\"Zinternet Nigera Limited\",\"bankId\":1000130},{\"bankCname\":\"HeritageTagPay\",\"bankCode\":\"HeritageTagPay\",\"bankId\":1000131},{\"bankCname\":\"Eartholeum\",\"bankCode\":\"Eartholeum\",\"bankId\":1000132},{\"bankCname\":\"MoneyBox\",\"bankCode\":\"MoneyBox\",\"bankId\":1000133},{\"bankCname\":\"Fidelity Mobile\",\"bankCode\":\"Fidelity Mobile\",\"bankId\":1000134},{\"bankCname\":\"Enterprise Bank\",\"bankCode\":\"Enterprise Bank\",\"bankId\":1000135},{\"bankCname\":\"FBNQUEST Merchant Bank\",\"bankCode\":\"FBNQUEST Merchant Bank\",\"bankId\":1000136},{\"bankCname\":\"Nova Merchant Bank\",\"bankCode\":\"Nova Merchant Bank\",\"bankId\":1000137},{\"bankCname\":\"Omoluabi savings and loans\",\"bankCode\":\"Omoluabi savings and loans\",\"bankId\":1000138},{\"bankCname\":\"Trustbond Mortgage Bank\",\"bankCode\":\"Trustbond Mortgage Bank\",\"bankId\":1000139},{\"bankCname\":\"SafeTrust\",\"bankCode\":\"SafeTrust\",\"bankId\":1000140},{\"bankCname\":\"FBN Mortgages Limited\",\"bankCode\":\"FBN Mortgages Limited\",\"bankId\":1000141},{\"bankCname\":\"Imperial Homes Mortgage Bank\",\"bankCode\":\"Imperial Homes Mortgage Bank\",\"bankId\":1000142},{\"bankCname\":\"AG Mortgage Bank\",\"bankCode\":\"AG Mortgage Bank\",\"bankId\":1000143},{\"bankCname\":\"Gateway Mortgage Bank\",\"bankCode\":\"Gateway Mortgage Bank\",\"bankId\":1000144},{\"bankCname\":\"Refuge Mortgage Bank\",\"bankCode\":\"Refuge Mortgage Bank\",\"bankId\":1000145},{\"bankCname\":\"Platinum Mortgage Bank\",\"bankCode\":\"Platinum Mortgage Bank\",\"bankId\":1000146},{\"bankCname\":\"First Generation Mortgage Bank\",\"bankCode\":\"First Generation Mortgage Bank\",\"bankId\":1000147},{\"bankCname\":\"Brent Mortgage Bank\",\"bankCode\":\"Brent Mortgage Bank\",\"bankId\":1000148},{\"bankCname\":\"Infinity Trust Mortgage Bank\",\"bankCode\":\"Infinity Trust Mortgage Bank\",\"bankId\":1000149},{\"bankCname\":\"Jubilee-Life Mortgage Bank\",\"bankCode\":\"Jubilee-Life Mortgage Bank\",\"bankId\":1000150},{\"bankCname\":\"Haggai Mortgage Bank Limited\",\"bankCode\":\"Haggai Mortgage Bank Limited\",\"bankId\":1000151},{\"bankCname\":\"New Prudential Bank\",\"bankCode\":\"New Prudential Bank\",\"bankId\":1000152},{\"bankCname\":\"Fortis Microfinance Bank\",\"bankCode\":\"Fortis Microfinance Bank\",\"bankId\":1000153},{\"bankCname\":\"Page Financials\",\"bankCode\":\"Page Financials\",\"bankId\":1000154},{\"bankCname\":\"Parralex Microfinance bank\",\"bankCode\":\"Parralex Microfinance bank\",\"bankId\":1000155},{\"bankCname\":\"Seed Capital Microfinance Bank\",\"bankCode\":\"Seed Capital Microfinance Bank\",\"bankId\":1000156},{\"bankCname\":\"Empire trust MFB\",\"bankCode\":\"Empire trust MFB\",\"bankId\":1000157},{\"bankCname\":\"AMML MFB\",\"bankCode\":\"AMML MFB\",\"bankId\":1000158},{\"bankCname\":\"Boctrust Microfinance Bank\",\"bankCode\":\"Boctrust Microfinance Bank\",\"bankId\":1000159},{\"bankCname\":\"Ohafia Microfinance Bank\",\"bankCode\":\"Ohafia Microfinance Bank\",\"bankId\":1000160},{\"bankCname\":\"Wetland Microfinance Bank\",\"bankCode\":\"Wetland Microfinance Bank\",\"bankId\":1000161},{\"bankCname\":\"Gowans Microfinance Bank\",\"bankCode\":\"Gowans Microfinance Bank\",\"bankId\":1000162},{\"bankCname\":\"Verite Microfinance Bank\",\"bankCode\":\"Verite Microfinance Bank\",\"bankId\":1000163},{\"bankCname\":\"Xslnce Microfinance Bank\",\"bankCode\":\"Xslnce Microfinance Bank\",\"bankId\":1000164},{\"bankCname\":\"Regent Microfinance Bank\",\"bankCode\":\"Regent Microfinance Bank\",\"bankId\":1000165},{\"bankCname\":\"Fidfund Microfinance Bank\",\"bankCode\":\"Fidfund Microfinance Bank\",\"bankId\":1000166},{\"bankCname\":\"BC Kash Microfinance Bank\",\"bankCode\":\"BC Kash Microfinance Bank\",\"bankId\":1000167},{\"bankCname\":\"Ndiorah Microfinance Bank\",\"bankCode\":\"Ndiorah Microfinance Bank\",\"bankId\":1000168},{\"bankCname\":\"Money Trust Microfinance Bank\",\"bankCode\":\"Money Trust Microfinance Bank\",\"bankId\":1000169},{\"bankCname\":\"Consumer Microfinance Bank\",\"bankCode\":\"Consumer Microfinance Bank\",\"bankId\":1000170},{\"bankCname\":\"Allworkers Microfinance Bank\",\"bankCode\":\"Allworkers Microfinance Bank\",\"bankId\":1000171},{\"bankCname\":\"Richway Microfinance Bank\",\"bankCode\":\"Richway Microfinance Bank\",\"bankId\":1000172},{\"bankCname\":\"AL-Barakah Microfinance Bank\",\"bankCode\":\"AL-Barakah Microfinance Bank\",\"bankId\":1000173},{\"bankCname\":\"Accion Microfinance Bank\",\"bankCode\":\"Accion Microfinance Bank\",\"bankId\":1000174},{\"bankCname\":\"Personal Trust Microfinance Bank\",\"bankCode\":\"Personal Trust Microfinance Bank\",\"bankId\":1000175},{\"bankCname\":\"Baobab Microfinance Bank\",\"bankCode\":\"Baobab Microfinance Bank\",\"bankId\":1000176},{\"bankCname\":\"PecanTrust Microfinance Bank\",\"bankCode\":\"PecanTrust Microfinance Bank\",\"bankId\":1000177},{\"bankCname\":\"Royal Exchange Microfinance Bank\",\"bankCode\":\"Royal Exchange Microfinance Bank\",\"bankId\":1000178},{\"bankCname\":\"Visa Microfinance Bank\",\"bankCode\":\"Visa Microfinance Bank\",\"bankId\":1000179},{\"bankCname\":\"Sagamu Microfinance Bank\",\"bankCode\":\"Sagamu Microfinance Bank\",\"bankId\":1000180},{\"bankCname\":\"Chikum Microfinance Bank\",\"bankCode\":\"Chikum Microfinance Bank\",\"bankId\":1000181},{\"bankCname\":\"Yes Microfinance Bank\",\"bankCode\":\"Yes Microfinance Bank\",\"bankId\":1000182},{\"bankCname\":\"Apeks Microfinance Bank\",\"bankCode\":\"Apeks Microfinance Bank\",\"bankId\":1000183},{\"bankCname\":\"CIT Microfinance Bank\",\"bankCode\":\"CIT Microfinance Bank\",\"bankId\":1000184},{\"bankCname\":\"Fullrange Microfinance Bank\",\"bankCode\":\"Fullrange Microfinance Bank\",\"bankId\":1000185},{\"bankCname\":\"Trident Microfinance Bank\",\"bankCode\":\"Trident Microfinance Bank\",\"bankId\":1000186},{\"bankCname\":\"IRL Microfinance Bank\",\"bankCode\":\"IRL Microfinance Bank\",\"bankId\":1000187},{\"bankCname\":\"Virtue Microfinance Bank\",\"bankCode\":\"Virtue Microfinance Bank\",\"bankId\":1000188},{\"bankCname\":\"Mutual Trust Microfinance Bank\",\"bankCode\":\"Mutual Trust Microfinance Bank\",\"bankId\":1000189},{\"bankCname\":\"Nagarta Microfinance Bank\",\"bankCode\":\"Nagarta Microfinance Bank\",\"bankId\":1000190},{\"bankCname\":\"FFS Microfinance Bank\",\"bankCode\":\"FFS Microfinance Bank\",\"bankId\":1000191},{\"bankCname\":\"La Fayette Microfinance Bank\",\"bankCode\":\"La Fayette Microfinance Bank\",\"bankId\":1000192},{\"bankCname\":\"e-Barcs Microfinance Bank\",\"bankCode\":\"e-Barcs Microfinance Bank\",\"bankId\":1000193},{\"bankCname\":\"Futo Microfinance Bank\",\"bankCode\":\"Futo Microfinance Bank\",\"bankId\":1000194},{\"bankCname\":\"Credit Afrique Microfinance Bank\",\"bankCode\":\"Credit Afrique Microfinance Bank\",\"bankId\":1000195},{\"bankCname\":\"Addosser Microfinance Bank\",\"bankCode\":\"Addosser Microfinance Bank\",\"bankId\":1000196},{\"bankCname\":\"Okpoga Microfinance Bank\",\"bankCode\":\"Okpoga Microfinance Bank\",\"bankId\":1000197},{\"bankCname\":\"Stanford Microfinance Bak\",\"bankCode\":\"Stanford Microfinance Bak\",\"bankId\":1000198},{\"bankCname\":\"First Royal Microfinance Bank\",\"bankCode\":\"First Royal Microfinance Bank\",\"bankId\":1000199},{\"bankCname\":\"Eso-E Microfinance Bank\",\"bankCode\":\"Eso-E Microfinance Bank\",\"bankId\":1000200},{\"bankCname\":\"Daylight Microfinance Bank\",\"bankCode\":\"Daylight Microfinance Bank\",\"bankId\":1000201},{\"bankCname\":\"Gashua Microfinance Bank\",\"bankCode\":\"Gashua Microfinance Bank\",\"bankId\":1000202},{\"bankCname\":\"Alpha Kapital Microfinance Bank\",\"bankCode\":\"Alpha Kapital Microfinance Bank\",\"bankId\":1000203},{\"bankCname\":\"Mainstreet Microfinance Bank\",\"bankCode\":\"Mainstreet Microfinance Bank\",\"bankId\":1000204},{\"bankCname\":\"Astrapolaris Microfinance Bank\",\"bankCode\":\"Astrapolaris Microfinance Bank\",\"bankId\":1000205},{\"bankCname\":\"Reliance Microfinance Bank\",\"bankCode\":\"Reliance Microfinance Bank\",\"bankId\":1000206},{\"bankCname\":\"Malachy Microfinance Bank\",\"bankCode\":\"Malachy Microfinance Bank\",\"bankId\":1000207},{\"bankCname\":\"Bosak Microfinance Bank\",\"bankCode\":\"Bosak Microfinance Bank\",\"bankId\":1000208},{\"bankCname\":\"Lapo Microfinance Bank\",\"bankCode\":\"Lapo Microfinance Bank\",\"bankId\":1000209},{\"bankCname\":\"GreenBank Microfinance Bank\",\"bankCode\":\"GreenBank Microfinance Bank\",\"bankId\":1000210},{\"bankCname\":\"FAST Microfinance Bank\",\"bankCode\":\"FAST Microfinance Bank\",\"bankId\":1000211},{\"bankCname\":\"Baines Credit Microfinance Bank\",\"bankCode\":\"Baines Credit Microfinance Bank\",\"bankId\":1000212},{\"bankCname\":\"Esan Microfinance Bank\",\"bankCode\":\"Esan Microfinance Bank\",\"bankId\":1000213},{\"bankCname\":\"Mutual Benefits Microfinance Bank\",\"bankCode\":\"Mutual Benefits Microfinance Bank\",\"bankId\":1000214},{\"bankCname\":\"KCMB Microfinance Bank\",\"bankCode\":\"KCMB Microfinance Bank\",\"bankId\":1000215},{\"bankCname\":\"Midland Microfinance Bank\",\"bankCode\":\"Midland Microfinance Bank\",\"bankId\":1000216},{\"bankCname\":\"Unical Microfinance Bank\",\"bankCode\":\"Unical Microfinance Bank\",\"bankId\":1000217},{\"bankCname\":\"NIRSAL Microfinance Bank\",\"bankCode\":\"NIRSAL Microfinance Bank\",\"bankId\":1000218},{\"bankCname\":\"Grooming Microfinance Bank\",\"bankCode\":\"Grooming Microfinance Bank\",\"bankId\":1000219},{\"bankCname\":\"Pennywise Microfinance Bank\",\"bankCode\":\"Pennywise Microfinance Bank\",\"bankId\":1000220},{\"bankCname\":\"ABU Microfinance Bank\",\"bankCode\":\"ABU Microfinance Bank\",\"bankId\":1000221},{\"bankCname\":\"RenMoney Microfinance Bank\",\"bankCode\":\"RenMoney Microfinance Bank\",\"bankId\":1000222},{\"bankCname\":\"New Dawn Microfinance Bank\",\"bankCode\":\"New Dawn Microfinance Bank\",\"bankId\":1000223},{\"bankCname\":\"UNN MFB\",\"bankCode\":\"UNN MFB\",\"bankId\":1000224},{\"bankCname\":\"Imo State Microfinance Bank\",\"bankCode\":\"Imo State Microfinance Bank\",\"bankId\":1000225},{\"bankCname\":\"Alekun Microfinance Bank\",\"bankCode\":\"Alekun Microfinance Bank\",\"bankId\":1000226},{\"bankCname\":\"Above Only Microfinance Bank\",\"bankCode\":\"Above Only Microfinance Bank\",\"bankId\":1000227},{\"bankCname\":\"Quickfund Microfinance Bank\",\"bankCode\":\"Quickfund Microfinance Bank\",\"bankId\":1000228},{\"bankCname\":\"Stellas Microfinance Bank\",\"bankCode\":\"Stellas Microfinance Bank\",\"bankId\":1000229},{\"bankCname\":\"Navy Microfinance Bank\",\"bankCode\":\"Navy Microfinance Bank\",\"bankId\":1000230},{\"bankCname\":\"Auchi Microfinance Bank\",\"bankCode\":\"Auchi Microfinance Bank\",\"bankId\":1000231},{\"bankCname\":\"Lovonus Microfinance Bank\",\"bankCode\":\"Lovonus Microfinance Bank\",\"bankId\":1000232},{\"bankCname\":\"Uniben Microfinance Bank\",\"bankCode\":\"Uniben Microfinance Bank\",\"bankId\":1000233},{\"bankCname\":\"Adeyemi College Staff Microfinance Bank\",\"bankCode\":\"Adeyemi College Staff Microfinance Bank\",\"bankId\":1000234},{\"bankCname\":\"Greenville Microfinance Bank\",\"bankCode\":\"Greenville Microfinance Bank\",\"bankId\":1000235},{\"bankCname\":\"AB Microfinance Bank\",\"bankCode\":\"AB Microfinance Bank\",\"bankId\":1000236},{\"bankCname\":\"Lavender Microfinance Bank\",\"bankCode\":\"Lavender Microfinance Bank\",\"bankId\":1000237},{\"bankCname\":\"Olabisi Onabanjo University Microfinance Bank\",\"bankCode\":\"Olabisi Onabanjo University Microfinance Bank\",\"bankId\":1000238},{\"bankCname\":\"Emeralds Microfinance Bank\",\"bankCode\":\"Emeralds Microfinance Bank\",\"bankId\":1000239},{\"bankCname\":\"Trustfund Microfinance Bank\",\"bankCode\":\"Trustfund Microfinance Bank\",\"bankId\":1000240},{\"bankCname\":\"Al-Hayat Microfinance Bank\",\"bankCode\":\"Al-Hayat Microfinance Bank\",\"bankId\":1000241},{\"bankCname\":\"FET\",\"bankCode\":\"FET\",\"bankId\":1000242},{\"bankCname\":\"Cellulant\",\"bankCode\":\"Cellulant\",\"bankId\":1000243},{\"bankCname\":\"eTranzact\",\"bankCode\":\"eTranzact\",\"bankId\":1000244},{\"bankCname\":\"Stanbic IBTC @ease wallet\",\"bankCode\":\"Stanbic IBTC @ease wallet\",\"bankId\":1000245},{\"bankCname\":\"Ecobank Xpress Account\",\"bankCode\":\"Ecobank Xpress Account\",\"bankId\":1000246},{\"bankCname\":\"GTMobile\",\"bankCode\":\"GTMobile\",\"bankId\":1000247},{\"bankCname\":\"TeasyMobile\",\"bankCode\":\"TeasyMobile\",\"bankId\":1000248},{\"bankCname\":\"Mkudi\",\"bankCode\":\"Mkudi\",\"bankId\":1000249},{\"bankCname\":\"VTNetworks\",\"bankCode\":\"VTNetworks\",\"bankId\":1000250},{\"bankCname\":\"AccessMobile\",\"bankCode\":\"AccessMobile\",\"bankId\":1000251},{\"bankCname\":\"FBNMobile\",\"bankCode\":\"FBNMobile\",\"bankId\":1000252},{\"bankCname\":\"Kegow\",\"bankCode\":\"Kegow\",\"bankId\":1000253},{\"bankCname\":\"FortisMobile\",\"bankCode\":\"FortisMobile\",\"bankId\":1000254},{\"bankCname\":\"Hedonmark\",\"bankCode\":\"Hedonmark\",\"bankId\":1000255},{\"bankCname\":\"ZenithMobile\",\"bankCode\":\"ZenithMobile\",\"bankId\":1000256},{\"bankCname\":\"Flutterwave Technology Solutions Limited\",\"bankCode\":\"Flutterwave Technology Solutions Limited\",\"bankId\":1000257},{\"bankCname\":\"NIP Virtual Bank\",\"bankCode\":\"NIP Virtual Bank\",\"bankId\":1000258},{\"bankCname\":\"ChamsMobile\",\"bankCode\":\"ChamsMobile\",\"bankId\":1000259},{\"bankCname\":\"MAUTECH Microfinance Bank\",\"bankCode\":\"MAUTECH Microfinance Bank\",\"bankId\":1000260},{\"bankCname\":\"Greenwich Merchant Bank\",\"bankCode\":\"Greenwich Merchant Bank\",\"bankId\":1000261},{\"bankCname\":\"Firmus MFB\",\"bankCode\":\"Firmus MFB\",\"bankId\":1000262},{\"bankCname\":\"Manny Microfinance bank\",\"bankCode\":\"Manny Microfinance bank\",\"bankId\":1000263},{\"bankCname\":\"Letshego MFB\",\"bankCode\":\"Letshego MFB\",\"bankId\":1000264},{\"bankCname\":\"M36\",\"bankCode\":\"M36\",\"bankId\":1000265},{\"bankCname\":\"Safe Haven MFB\",\"bankCode\":\"Safe Haven MFB\",\"bankId\":1000266},{\"bankCname\":\"9 Payment Service Bank\",\"bankCode\":\"9 Payment Service Bank\",\"bankId\":1000267},{\"bankCname\":\"Tangerine Bank\",\"bankCode\":\"Tangerine Bank\",\"bankId\":1000268},{\"bankCname\":\"Access Bank\",\"bankCode\":\"Access Bank\",\"bankId\":1000269},{\"bankCname\":\"ACCESS BANK PLC(DIAMOND)\",\"bankCode\":\"ACCESS BANK PLC(DIAMOND)\",\"bankId\":1000270},{\"bankCname\":\"AB Microfinance Bank\",\"bankCode\":\"AB Microfinance Bank\",\"bankId\":1000271},{\"bankCname\":\"Above Only Microfinance Bank\",\"bankCode\":\"Above Only Microfinance Bank\",\"bankId\":1000272},{\"bankCname\":\"ABU Microfinance Bank\",\"bankCode\":\"ABU Microfinance Bank\",\"bankId\":1000273},{\"bankCname\":\"ABUCOOP Microfinance Bank\",\"bankCode\":\"ABUCOOP Microfinance Bank\",\"bankId\":1000274},{\"bankCname\":\"Accion Microfinance Bank\",\"bankCode\":\"Accion Microfinance Bank\",\"bankId\":1000275},{\"bankCname\":\"Addosser Microfinance Bank\",\"bankCode\":\"Addosser Microfinance Bank\",\"bankId\":1000276},{\"bankCname\":\"Adeyemi College Staff Microfinance Bank\",\"bankCode\":\"Adeyemi College Staff Microfinance Bank\",\"bankId\":1000277},{\"bankCname\":\"Afekhafe Microfinance Bank\",\"bankCode\":\"Afekhafe Microfinance Bank\",\"bankId\":1000278},{\"bankCname\":\"Agosasa Microfinance Bank\",\"bankCode\":\"Agosasa Microfinance Bank\",\"bankId\":1000279},{\"bankCname\":\"AL-Barakah Microfinance Bank\",\"bankCode\":\"AL-Barakah Microfinance Bank\",\"bankId\":1000280},{\"bankCname\":\"AL-HAYAT Microfinance Bank\",\"bankCode\":\"AL-HAYAT Microfinance Bank\",\"bankId\":1000281},{\"bankCname\":\"Alekun Microfinance Bank\",\"bankCode\":\"Alekun Microfinance Bank\",\"bankId\":1000282},{\"bankCname\":\"Alert Microfinance Bank\",\"bankCode\":\"Alert Microfinance Bank\",\"bankId\":1000283},{\"bankCname\":\"Allworkers Microfinance Bank\",\"bankCode\":\"Allworkers Microfinance Bank\",\"bankId\":1000284},{\"bankCname\":\"Alpha Kapital Microfinance Bank\",\"bankCode\":\"Alpha Kapital Microfinance Bank\",\"bankId\":1000285},{\"bankCname\":\"Amac Microfinance Bank\",\"bankCode\":\"Amac Microfinance Bank\",\"bankId\":1000286},{\"bankCname\":\"Amju Unique Micro Finance Ban\",\"bankCode\":\"Amju Unique Micro Finance Ban\",\"bankId\":1000287},{\"bankCname\":\"AMML Microfinance Bank\",\"bankCode\":\"AMML Microfinance Bank\",\"bankId\":1000288},{\"bankCname\":\"Apeks Microfinance Bank\",\"bankCode\":\"Apeks Microfinance Bank\",\"bankId\":1000289},{\"bankCname\":\"APPLE Microfinance Bank\",\"bankCode\":\"APPLE Microfinance Bank\",\"bankId\":1000290},{\"bankCname\":\"Arise Microfinance Bank\",\"bankCode\":\"Arise Microfinance Bank\",\"bankId\":1000291},{\"bankCname\":\"ASO Savings & Loans\",\"bankCode\":\"ASO Savings & Loans\",\"bankId\":1000292},{\"bankCname\":\"Asset Matrix Microfinance Bank\",\"bankCode\":\"Asset Matrix Microfinance Bank\",\"bankId\":1000293},{\"bankCname\":\"Astrapolaris Microfinance Bank\",\"bankCode\":\"Astrapolaris Microfinance Bank\",\"bankId\":1000294},{\"bankCname\":\"Auchi Microfinance Bank\",\"bankCode\":\"Auchi Microfinance Bank\",\"bankId\":1000295},{\"bankCname\":\"Access Yello\",\"bankCode\":\"Access Yello\",\"bankId\":1000296},{\"bankCname\":\"Access Money\",\"bankCode\":\"Access Money\",\"bankId\":1000297},{\"bankCname\":\"Abbey Mortgage Bank\",\"bankCode\":\"Abbey Mortgage Bank\",\"bankId\":1000298},{\"bankCname\":\"AG Mortgage Bank Plc\",\"bankCode\":\"AG Mortgage Bank Plc\",\"bankId\":1000299},{\"bankCname\":\"Ada Microfinance Bank\",\"bankCode\":\"Ada Microfinance Bank\",\"bankId\":1000300},{\"bankCname\":\"Assets Microfinance Bank\",\"bankCode\":\"Assets Microfinance Bank\",\"bankId\":1000301},{\"bankCname\":\"Avuenegbe Microfinance Bank\",\"bankCode\":\"Avuenegbe Microfinance Bank\",\"bankId\":1000302},{\"bankCname\":\"Anchorage Microfinance Bank\",\"bankCode\":\"Anchorage Microfinance Bank\",\"bankId\":1000303},{\"bankCname\":\"Baines Credit MicrofinanceBank\",\"bankCode\":\"Baines Credit MicrofinanceBank\",\"bankId\":1000304},{\"bankCname\":\"Balogun Fulani Microfinance Bank\",\"bankCode\":\"Balogun Fulani Microfinance Bank\",\"bankId\":1000305},{\"bankCname\":\"Banex Microfinance Bank\",\"bankCode\":\"Banex Microfinance Bank\",\"bankId\":1000306},{\"bankCname\":\"BAOBAB Microfinance Bank\",\"bankCode\":\"BAOBAB Microfinance Bank\",\"bankId\":1000307},{\"bankCname\":\"Bayero Microfinance Bank\",\"bankCode\":\"Bayero Microfinance Bank\",\"bankId\":1000308},{\"bankCname\":\"BC Kash Microfinance Bank\",\"bankCode\":\"BC Kash Microfinance Bank\",\"bankId\":1000309},{\"bankCname\":\"BENYSTA Microfinance Bank\",\"bankCode\":\"BENYSTA Microfinance Bank\",\"bankId\":1000310},{\"bankCname\":\"BIPC Microfinance Bank\",\"bankCode\":\"BIPC Microfinance Bank\",\"bankId\":1000311},{\"bankCname\":\"Bluewhales Microfinance Bank\",\"bankCode\":\"Bluewhales Microfinance Bank\",\"bankId\":1000312},{\"bankCname\":\"Boctrust Microfinance Bank\",\"bankCode\":\"Boctrust Microfinance Bank\",\"bankId\":1000313},{\"bankCname\":\"Bonghe Microfinance Bank\",\"bankCode\":\"Bonghe Microfinance Bank\",\"bankId\":1000314},{\"bankCname\":\"BORGU Microfinance Bank\",\"bankCode\":\"BORGU Microfinance Bank\",\"bankId\":1000315},{\"bankCname\":\"Borstal Microfinance Bank\",\"bankCode\":\"Borstal Microfinance Bank\",\"bankId\":1000316},{\"bankCname\":\"Bosak Microfinance Bank\",\"bankCode\":\"Bosak Microfinance Bank\",\"bankId\":1000317},{\"bankCname\":\"Bowen Microfinance Bank\",\"bankCode\":\"Bowen Microfinance Bank\",\"bankId\":1000318},{\"bankCname\":\"BRETHREN Microfinance Bank\",\"bankCode\":\"BRETHREN Microfinance Bank\",\"bankId\":1000319},{\"bankCname\":\"Bridgeway Microfinance Bank\",\"bankCode\":\"Bridgeway Microfinance Bank\",\"bankId\":1000320},{\"bankCname\":\"Brightway Microfinance Bank\",\"bankCode\":\"Brightway Microfinance Bank\",\"bankId\":1000321},{\"bankCname\":\"Business Support Microfinance Bank\",\"bankCode\":\"Business Support Microfinance Bank\",\"bankId\":1000322},{\"bankCname\":\"Brent Mortgage Bank\",\"bankCode\":\"Brent Mortgage Bank\",\"bankId\":1000323},{\"bankCname\":\"Balogun Gambari Mfb\",\"bankCode\":\"Balogun Gambari Mfb\",\"bankId\":1000324},{\"bankCname\":\"Central Bank of Nigeria\",\"bankCode\":\"Central Bank of Nigeria\",\"bankId\":1000325},{\"bankCname\":\"Citi Bank\",\"bankCode\":\"Citi Bank\",\"bankId\":1000326},{\"bankCname\":\"Calabar Microfinance Bank\",\"bankCode\":\"Calabar Microfinance Bank\",\"bankId\":1000327},{\"bankCname\":\"Capstone Microfinance Bank\",\"bankCode\":\"Capstone Microfinance Bank\",\"bankId\":1000328},{\"bankCname\":\"Cashconnect Microfinance Bank\",\"bankCode\":\"Cashconnect Microfinance Bank\",\"bankId\":1000329},{\"bankCname\":\"Chibueze Microfinance Bank\",\"bankCode\":\"Chibueze Microfinance Bank\",\"bankId\":1000330},{\"bankCname\":\"CoalCamp Microfinance Bank\",\"bankCode\":\"CoalCamp Microfinance Bank\",\"bankId\":1000331},{\"bankCname\":\"Coastline Microfinance Bank\",\"bankCode\":\"Coastline Microfinance Bank\",\"bankId\":1000332},{\"bankCname\":\"Corestep Microfinance Bank\",\"bankCode\":\"Corestep Microfinance Bank\",\"bankId\":1000333},{\"bankCname\":\"Carbon\",\"bankCode\":\"Carbon\",\"bankId\":1000334},{\"bankCname\":\"Cellulant PSSP\",\"bankCode\":\"Cellulant PSSP\",\"bankId\":1000335},{\"bankCname\":\"Cyberspace Limited\",\"bankCode\":\"Cyberspace Limited\",\"bankId\":1000336},{\"bankCname\":\"Coop Mortgage Bank\",\"bankCode\":\"Coop Mortgage Bank\",\"bankId\":1000337},{\"bankCname\":\"Conpro Microfinance Bank\",\"bankCode\":\"Conpro Microfinance Bank\",\"bankId\":1000338},{\"bankCname\":\"Chams Mobile\",\"bankCode\":\"Chams Mobile\",\"bankId\":1000339},{\"bankCname\":\"Contec Global Infotech\",\"bankCode\":\"Contec Global Infotech\",\"bankId\":1000340},{\"bankCname\":\"Davodani Microfinance Bank\",\"bankCode\":\"Davodani Microfinance Bank\",\"bankId\":1000341},{\"bankCname\":\"Ecobank Bank\",\"bankCode\":\"Ecobank Bank\",\"bankId\":1000342},{\"bankCname\":\"Eagle Flight Microfinance Bank\",\"bankCode\":\"Eagle Flight Microfinance Bank\",\"bankId\":1000343},{\"bankCname\":\"Ebonyi State University Microfinance Bank\",\"bankCode\":\"Ebonyi State University Microfinance Bank\",\"bankId\":1000344},{\"bankCname\":\"Edfin Microfinance Bank\",\"bankCode\":\"Edfin Microfinance Bank\",\"bankId\":1000345},{\"bankCname\":\"EK-Reliable Microfinance Bank\",\"bankCode\":\"EK-Reliable Microfinance Bank\",\"bankId\":1000346},{\"bankCname\":\"Empire trust Microfinance Bank\",\"bankCode\":\"Empire trust Microfinance Bank\",\"bankId\":1000347},{\"bankCname\":\"Evangel Microfinance Bank\",\"bankCode\":\"Evangel Microfinance Bank\",\"bankId\":1000348},{\"bankCname\":\"Evergreen Microfinance Bank\",\"bankCode\":\"Evergreen Microfinance Bank\",\"bankId\":1000349},{\"bankCname\":\"Eyowo Microfinance Bank\",\"bankCode\":\"Eyowo Microfinance Bank\",\"bankId\":1000350},{\"bankCname\":\"FBN Merchant Bank\",\"bankCode\":\"FBN Merchant Bank\",\"bankId\":1000351},{\"bankCname\":\"FSDH Merchant Bank\",\"bankCode\":\"FSDH Merchant Bank\",\"bankId\":1000352},{\"bankCname\":\"FAME Microfinance Bank\",\"bankCode\":\"FAME Microfinance Bank\",\"bankId\":1000353},{\"bankCname\":\"FCMB BETA\",\"bankCode\":\"FCMB BETA\",\"bankId\":1000354},{\"bankCname\":\"FCT Microfinance Bank\",\"bankCode\":\"FCT Microfinance Bank\",\"bankId\":1000355},{\"bankCname\":\"FEDERAL UNIVERSITY DUTSE Microfinance Bank\",\"bankCode\":\"FEDERAL UNIVERSITY DUTSE Microfinance Bank\",\"bankId\":1000356},{\"bankCname\":\"FederalPoly NasarawaMicrofinance Bank\",\"bankCode\":\"FederalPoly NasarawaMicrofinance Bank\",\"bankId\":1000357},{\"bankCname\":\"FinaTrust Microfinance Bank\",\"bankCode\":\"FinaTrust Microfinance Bank\",\"bankId\":1000358},{\"bankCname\":\"Finca Microfinance Bank\",\"bankCode\":\"Finca Microfinance Bank\",\"bankId\":1000359},{\"bankCname\":\"Firmus Microfinance Bank\",\"bankCode\":\"Firmus Microfinance Bank\",\"bankId\":1000360},{\"bankCname\":\"First Multiple Microfinance Bank\",\"bankCode\":\"First Multiple Microfinance Bank\",\"bankId\":1000361},{\"bankCname\":\"First Option Microfinance Bank\",\"bankCode\":\"First Option Microfinance Bank\",\"bankId\":1000362},{\"bankCname\":\"Futminna Microfinance Bank\",\"bankCode\":\"Futminna Microfinance Bank\",\"bankId\":1000363},{\"bankCname\":\"FETS\",\"bankCode\":\"FETS\",\"bankId\":1000364},{\"bankCname\":\"FirstMonie Wallet\",\"bankCode\":\"FirstMonie Wallet\",\"bankId\":1000365},{\"bankCname\":\"Fortis Mobile Money\",\"bankCode\":\"Fortis Mobile Money\",\"bankId\":1000366},{\"bankCname\":\"First Mortgage Limited\",\"bankCode\":\"First Mortgage Limited\",\"bankId\":1000367},{\"bankCname\":\"Fedpoly Nasarawa Microfinance Bank\",\"bankCode\":\"Fedpoly Nasarawa Microfinance Bank\",\"bankId\":1000368},{\"bankCname\":\"FirstTrust Mortgage Bank\",\"bankCode\":\"FirstTrust Mortgage Bank\",\"bankId\":1000369},{\"bankCname\":\"FCMB Mobile\",\"bankCode\":\"FCMB Mobile\",\"bankId\":1000370},{\"bankCname\":\"Flutterwave Technology Solutions\",\"bankCode\":\"Flutterwave Technology Solutions\",\"bankId\":1000371},{\"bankCname\":\"Federal Polytechnic, Nekede Microfinance Bank\",\"bankCode\":\"Federal Polytechnic, Nekede Microfinance Bank\",\"bankId\":1000372},{\"bankCname\":\"First Apple Limited\",\"bankCode\":\"First Apple Limited\",\"bankId\":1000373},{\"bankCname\":\"Fortress Microfinance Bank\",\"bankCode\":\"Fortress Microfinance Bank\",\"bankId\":1000374},{\"bankCname\":\"GTBank\",\"bankCode\":\"GTBank\",\"bankId\":1000375},{\"bankCname\":\"Girei Microfinance Bank\",\"bankCode\":\"Girei Microfinance Bank\",\"bankId\":1000376},{\"bankCname\":\"GLORY Microfinance Bank\",\"bankCode\":\"GLORY Microfinance Bank\",\"bankId\":1000377},{\"bankCname\":\"GMB Microfinance Bank\",\"bankCode\":\"GMB Microfinance Bank\",\"bankId\":1000378},{\"bankCname\":\"Grant Microfinance Bank\",\"bankCode\":\"Grant Microfinance Bank\",\"bankId\":1000379},{\"bankCname\":\"GTI Microfinance Bank\",\"bankCode\":\"GTI Microfinance Bank\",\"bankId\":1000380},{\"bankCname\":\"Giginya Microfinance Bank\",\"bankCode\":\"Giginya Microfinance Bank\",\"bankId\":1000381},{\"bankCname\":\"Giant Stride Mfb\",\"bankCode\":\"Giant Stride Mfb\",\"bankId\":1000382},{\"bankCname\":\"Good Neighbours Mfb\",\"bankCode\":\"Good Neighbours Mfb\",\"bankId\":1000383},{\"bankCname\":\"Heritage\",\"bankCode\":\"Heritage\",\"bankId\":1000384},{\"bankCname\":\"Hala Microfinance Bank\",\"bankCode\":\"Hala Microfinance Bank\",\"bankId\":1000385},{\"bankCname\":\"Headway Microfinance Bank\",\"bankCode\":\"Headway Microfinance Bank\",\"bankId\":1000386},{\"bankCname\":\"Highland Microfinance Bank\",\"bankCode\":\"Highland Microfinance Bank\",\"bankId\":1000387},{\"bankCname\":\"Hope Payment Service Bank\",\"bankCode\":\"Hope Payment Service Bank\",\"bankId\":1000388},{\"bankCname\":\"First Heritage Microfinance Bank\",\"bankCode\":\"First Heritage Microfinance Bank\",\"bankId\":1000389},{\"bankCname\":\"IBETO Microfinance Bank\",\"bankCode\":\"IBETO Microfinance Bank\",\"bankId\":1000390},{\"bankCname\":\"Ikenne Microfinance Bank\",\"bankCode\":\"Ikenne Microfinance Bank\",\"bankId\":1000391},{\"bankCname\":\"Ikire Microfinance Bank\",\"bankCode\":\"Ikire Microfinance Bank\",\"bankId\":1000392},{\"bankCname\":\"Ilasan Microfinance Bank\",\"bankCode\":\"Ilasan Microfinance Bank\",\"bankId\":1000393},{\"bankCname\":\"Ilora Microfinance Bank\",\"bankCode\":\"Ilora Microfinance Bank\",\"bankId\":1000394},{\"bankCname\":\"Imo Microfinance Bank\",\"bankCode\":\"Imo Microfinance Bank\",\"bankId\":1000395},{\"bankCname\":\"Imowo Microfinance Bank\",\"bankCode\":\"Imowo Microfinance Bank\",\"bankId\":1000396},{\"bankCname\":\"Infinity Microfinance Bank\",\"bankCode\":\"Infinity Microfinance Bank\",\"bankId\":1000397},{\"bankCname\":\"Insight Microfinance Bank\",\"bankCode\":\"Insight Microfinance Bank\",\"bankId\":1000398},{\"bankCname\":\"Interland Microfinance Bank\",\"bankCode\":\"Interland Microfinance Bank\",\"bankId\":1000399},{\"bankCname\":\"ISALEOYO Microfinance Bank\",\"bankCode\":\"ISALEOYO Microfinance Bank\",\"bankId\":1000400},{\"bankCname\":\"Ishie Microfinance Bank\",\"bankCode\":\"Ishie Microfinance Bank\",\"bankId\":1000401},{\"bankCname\":\"Izon Microfinance Bank\",\"bankCode\":\"Izon Microfinance Bank\",\"bankId\":1000402},{\"bankCname\":\"Innovectives\",\"bankCode\":\"Innovectives\",\"bankId\":1000403},{\"bankCname\":\"Ilorin Microfinance Bank\",\"bankCode\":\"Ilorin Microfinance Bank\",\"bankId\":1000404},{\"bankCname\":\"Jubilee Life Mortgage Bank\",\"bankCode\":\"Jubilee Life Mortgage Bank\",\"bankId\":1000405},{\"bankCname\":\"Kadpoly Microfinance Bank\",\"bankCode\":\"Kadpoly Microfinance Bank\",\"bankId\":1000406},{\"bankCname\":\"Kontagora Microfinance Bank\",\"bankCode\":\"Kontagora Microfinance Bank\",\"bankId\":1000407},{\"bankCname\":\"Kuda Microfinance Bank\",\"bankCode\":\"Kuda Microfinance Bank\",\"bankId\":1000408},{\"bankCname\":\"Lafayette Microfinance Bank\",\"bankCode\":\"Lafayette Microfinance Bank\",\"bankId\":1000409},{\"bankCname\":\"Legend Microfinance Bank\",\"bankCode\":\"Legend Microfinance Bank\",\"bankId\":1000410},{\"bankCname\":\"Ltshego Microfinance Bank\",\"bankCode\":\"Ltshego Microfinance Bank\",\"bankId\":1000411},{\"bankCname\":\"Lagos Building Investment Company\",\"bankCode\":\"Lagos Building Investment Company\",\"bankId\":1000412},{\"bankCname\":\"Light Microfinance Bank\",\"bankCode\":\"Light Microfinance Bank\",\"bankId\":1000413},{\"bankCname\":\"Megapraise Microfinance Bank\",\"bankCode\":\"Megapraise Microfinance Bank\",\"bankId\":1000414},{\"bankCname\":\"Meridian Microfinance Bank\",\"bankCode\":\"Meridian Microfinance Bank\",\"bankId\":1000415},{\"bankCname\":\"Molusi Microfinance Bank\",\"bankCode\":\"Molusi Microfinance Bank\",\"bankId\":1000416},{\"bankCname\":\"MozFin Microfinance Bank\",\"bankCode\":\"MozFin Microfinance Bank\",\"bankId\":1000417},{\"bankCname\":\"MayFresh Mortgage Bank\",\"bankCode\":\"MayFresh Mortgage Bank\",\"bankId\":1000418},{\"bankCname\":\"Monarch Microfinance Bank\",\"bankCode\":\"Monarch Microfinance Bank\",\"bankId\":1000419},{\"bankCname\":\"Moyofade Microfinance Bank\",\"bankCode\":\"Moyofade Microfinance Bank\",\"bankId\":1000420},{\"bankCname\":\"NOVA Merchant Bank Ltd\",\"bankCode\":\"NOVA Merchant Bank Ltd\",\"bankId\":1000421},{\"bankCname\":\"NIGERIAN NAVY Microfinance Bank\",\"bankCode\":\"NIGERIAN NAVY Microfinance Bank\",\"bankId\":1000422},{\"bankCname\":\"Nnew women Microfinance Bank\",\"bankCode\":\"Nnew women Microfinance Bank\",\"bankId\":1000423},{\"bankCname\":\"Nuture Microfinance Bank\",\"bankCode\":\"Nuture Microfinance Bank\",\"bankId\":1000424},{\"bankCname\":\"NowNow Bank\",\"bankCode\":\"NowNow Bank\",\"bankId\":1000425},{\"bankCname\":\"Nsukka Microfinance Bank\",\"bankCode\":\"Nsukka Microfinance Bank\",\"bankId\":1000426},{\"bankCname\":\"OAKLAND Microfinance Bank\",\"bankCode\":\"OAKLAND Microfinance Bank\",\"bankId\":1000427},{\"bankCname\":\"OLUCHUKWU Microfinance Bank\",\"bankCode\":\"OLUCHUKWU Microfinance Bank\",\"bankId\":1000428},{\"bankCname\":\"Oscotech Microfinance Bank\",\"bankCode\":\"Oscotech Microfinance Bank\",\"bankId\":1000429},{\"bankCname\":\"Omoluabi Mortgage Bank Plc\",\"bankCode\":\"Omoluabi Mortgage Bank Plc\",\"bankId\":1000430},{\"bankCname\":\"Olofin Owena Microfinance Bank\",\"bankCode\":\"Olofin Owena Microfinance Bank\",\"bankId\":1000431},{\"bankCname\":\"Petra Microfinance Bank\",\"bankCode\":\"Petra Microfinance Bank\",\"bankId\":1000432},{\"bankCname\":\"Polyunwana Microfinance Bank\",\"bankCode\":\"Polyunwana Microfinance Bank\",\"bankId\":1000433},{\"bankCname\":\"Prestige Microfinance Bank\",\"bankCode\":\"Prestige Microfinance Bank\",\"bankId\":1000434},{\"bankCname\":\"Parkway-ReadyCash\",\"bankCode\":\"Parkway-ReadyCash\",\"bankId\":1000435},{\"bankCname\":\"Paystack Payments\",\"bankCode\":\"Paystack Payments\",\"bankId\":1000436},{\"bankCname\":\"Rolez Microfinance Bank\",\"bankCode\":\"Rolez Microfinance Bank\",\"bankId\":1000437},{\"bankCname\":\"RUBIES Microfinance Bank\",\"bankCode\":\"RUBIES Microfinance Bank\",\"bankId\":1000438},{\"bankCname\":\"Rehoboth Microfinance Bank\",\"bankCode\":\"Rehoboth Microfinance Bank\",\"bankId\":1000439},{\"bankCname\":\"Safetrust Mortage\",\"bankCode\":\"Safetrust Mortage\",\"bankId\":1000440},{\"bankCname\":\"Shepherd Trust Microfinance Bank\",\"bankCode\":\"Shepherd Trust Microfinance Bank\",\"bankId\":1000441},{\"bankCname\":\"Spectrum Microfinance Bank\",\"bankCode\":\"Spectrum Microfinance Bank\",\"bankId\":1000442},{\"bankCname\":\"STB MORTGAGE BANK\",\"bankCode\":\"STB MORTGAGE BANK\",\"bankId\":1000443},{\"bankCname\":\"Stockcorp Microfinance Bank\",\"bankCode\":\"Stockcorp Microfinance Bank\",\"bankId\":1000444},{\"bankCname\":\"Sunbeam Microfinance Bank\",\"bankCode\":\"Sunbeam Microfinance Bank\",\"bankId\":1000445},{\"bankCname\":\"Sterling Mobile\",\"bankCode\":\"Sterling Mobile\",\"bankId\":1000446},{\"bankCname\":\"Safegate Microfinance Bank\",\"bankCode\":\"Safegate Microfinance Bank\",\"bankId\":1000447},{\"bankCname\":\"Sulspap Microfinance Bank\",\"bankCode\":\"Sulspap Microfinance Bank\",\"bankId\":1000448},{\"bankCname\":\"TITAN Trust Bank\",\"bankCode\":\"TITAN Trust Bank\",\"bankId\":1000449},{\"bankCname\":\"TCF Microfinance Bank\",\"bankCode\":\"TCF Microfinance Bank\",\"bankId\":1000450},{\"bankCname\":\"TrustBanc J6 Microfinance Bank Limited\",\"bankCode\":\"TrustBanc J6 Microfinance Bank Limited\",\"bankId\":1000451},{\"bankCname\":\"TagPay\",\"bankCode\":\"TagPay\",\"bankId\":1000452},{\"bankCname\":\"Trustbond Mortgage Bank Plc\",\"bankCode\":\"Trustbond Mortgage Bank Plc\",\"bankId\":1000453},{\"bankCname\":\"United Bank for Africa Plc\",\"bankCode\":\"United Bank for Africa Plc\",\"bankId\":1000454},{\"bankCname\":\"U AND C Microfinance Bank\",\"bankCode\":\"U AND C Microfinance Bank\",\"bankId\":1000455},{\"bankCname\":\"Unilorin Microfinance Bank\",\"bankCode\":\"Unilorin Microfinance Bank\",\"bankId\":1000456},{\"bankCname\":\"Uzondu Microfinance Bank\",\"bankCode\":\"Uzondu Microfinance Bank\",\"bankId\":1000457},{\"bankCname\":\"Unilag Microfinance Bank\",\"bankCode\":\"Unilag Microfinance Bank\",\"bankId\":1000458},{\"bankCname\":\"UNIMAID Microfinance Bank\",\"bankCode\":\"UNIMAID Microfinance Bank\",\"bankId\":1000459},{\"bankCname\":\"Vt Network\",\"bankCode\":\"Vt Network\",\"bankId\":1000460},{\"bankCname\":\"Verdant Microfinance Bank\",\"bankCode\":\"Verdant Microfinance Bank\",\"bankId\":1000461},{\"bankCname\":\"Winview Microfinance Bank\",\"bankCode\":\"Winview Microfinance Bank\",\"bankId\":1000462},{\"bankCname\":\"YCT Microfinance Bank\",\"bankCode\":\"YCT Microfinance Bank\",\"bankId\":1000463},{\"bankCname\":\"Zenith Mobile\",\"bankCode\":\"Zenith Mobile\",\"bankId\":1000464},{\"bankCname\":\"ZWallet\",\"bankCode\":\"ZWallet\",\"bankId\":1000465},{\"bankCname\":\"ZENITH EASY WALLET\",\"bankCode\":\"ZENITH EASY WALLET\",\"bankId\":1000466},{\"bankCname\":\"Interswitch\",\"bankCode\":\"Interswitch\",\"bankId\":1000467},{\"bankCname\":\"VFD Microfinance Bank\",\"bankCode\":\"VFD Microfinance Bank\",\"bankId\":1000469},{\"bankCname\":\"Paycom\",\"bankCode\":\"Paycom\",\"bankId\":1000470},{\"bankCname\":\"PalmPay\",\"bankCode\":\"PalmPay\",\"bankId\":1000471},{\"bankCname\":\"Lagos Building Investment Company Plc\",\"bankCode\":\"Lagos Building Investment Company Plc\",\"bankId\":1000472},{\"bankCname\":\"Ibile Microfinance\",\"bankCode\":\"Ibile Microfinance\",\"bankId\":1000473},{\"bankCname\":\"SDH Merchant Bank Limited\",\"bankCode\":\"SDH Merchant Bank Limited\",\"bankId\":1000474}]";
//        List<BallBank> ballBanks = JsonUtil.fromJsonToList(json, BallBank.class);
//        for(BallBank bb:ballBanks){
//            bankService.insert(bb);
//        }
//        return BaseResponse.successWithData("");
//    }
    @GetMapping("menu")
    public Object testMenu() {
        List<BallMenu> all = ballMenuService.findAll();
        List<Menu> tree = new ArrayList<>();
        for (BallMenu ballMenu : all) {
            //一级菜单放入
            if (ballMenu.getParentId() == 0) {
                tree.add(Menu.builder()
                        .id(ballMenu.getId())
                        .label(ballMenu.getMenuName())
                        .children(new ArrayList<>())
                        .sort(ballMenu.getIsMenu())
                        .build());
            }
        }
        for (BallMenu ballMenu : all) {
            if (ballMenu.getParentId() == 0) {
                continue;
            }
            for (Menu menu : tree) {
                if (menu.getId().equals(ballMenu.getParentId())) {
                    //放入二级
                    List<Menu> children = menu.getChildren();
                    children.add(Menu.builder()
                            .id(ballMenu.getId())
                            .label(ballMenu.getMenuName())
                            .children(new ArrayList<>())
                            .build());
                }
            }
        }
        for (BallMenu ballMenu : all) {
            if (ballMenu.getParentId() == 0) {
                continue;
            }
            //
            for (Menu menu : tree) {
                //获得第一层子菜单
                List<Menu> children1 = menu.getChildren();
                for (Menu child : children1) {
                    if (child.getId().equals(ballMenu.getParentId())) {
                        //放入三级
                        List<Menu> last = child.getChildren();
                        last.add(Menu.builder()
                                .id(ballMenu.getId())
                                .label(ballMenu.getMenuName())
                                .children(new ArrayList<>())
                                .build());
                    }
                }
            }
        }
        Collections.sort(tree, Comparator.comparingInt(Menu::getSort));
        return tree;
    }

//    @PostMapping("loss")
//    public Object perloss(@RequestParam("json") String json){
//        try {
//            ApiFixtures apiFixtures = JsonUtil.fromJson(json, ApiFixtures.class);
//            List<ApiFixtures.FixtureReponse> resposneList = apiFixtures.getResponse();
//            if(resposneList==null||resposneList.isEmpty()){
//                return BaseResponse.failedWithMsg("failed");
//            }
//            for (ApiFixtures.FixtureReponse response : resposneList) {
//                ApiFixture fixture = response.getFixture();
//                ApiTeams teams = response.getTeams();
//                ApiGoals goals = response.getGoals();
//                ApiScore score = response.getScore();
//                ApiLeague league = response.getLeague();
//                //比赛是否结束,如果结束查数据库,有的话修改状态 并结算订单
//                switch (fixture.getStatus().getShortStatus()) {
//                    case ApiFixtureStatus.STATUS_TBD:
//                    case ApiFixtureStatus.STATUS_NS:
//                        //初始保存
//                        try {
//                            BallGame insert = BallGame.builder()
//                                    .id(fixture.getId())
//                                    .gameStatus(1)
//                                    .status(1)
//                                    .createdAt(TimeUtil.getNowTimeMill())
//                                    .allianceLogo(league.getLogo())
//                                    .allianceName(league.getName())
//                                    .mainLogo(teams.getHome().getLogo())
//                                    .mainName(teams.getHome().getName())
//                                    .guestLogo(teams.getAway().getLogo())
//                                    .guestName(teams.getAway().getName())
//                                    .startTime(fixture.getTimestamp() * 1000)
//                                    .build();
//                            if (gameService.insert(insert)) {
//                                //存入redis,然后去查赔率
//                                redisUtil.leftSet(RedisKeyContant.GAME_NEED_QUERY_ODDS, insert);
//                            }
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                        break;
//                    case ApiFixtureStatus.STATUS_FT:
//                    case ApiFixtureStatus.STATUS_PEN:
//                        //比赛结束
//                        gameService.edit(BallGame.builder()
//                                .id(fixture.getId())
//                                .homeHalf(score.getHalftime().getHome())
//                                .guestHalf(score.getHalftime().getAway())
//                                .homeFull(goals.getHome())
//                                .guestFull(goals.getAway())
//                                .homeOvertime(score.getExtratime().getHome())
//                                .guestOvertime(score.getExtratime().getAway())
//                                .homePenalty(score.getPenalty().getHome())
//                                .guestPenalty(score.getPenalty().getAway())
//                                .gameStatus(3)
//                                .build());
//                        ThreadPoolUtil.exec(() -> ballBetService.betOpen(fixture.getId()));
//                        break;
//                    case ApiFixtureStatus.STATUS_CANC:
//                    case ApiFixtureStatus.STATUS_ABD:
//                    case ApiFixtureStatus.STATUS_AWD:
//                        //比赛取消
//                        break;
//                    case ApiFixtureStatus.STATUS_WO:
//                        //走过场
//                        break;
//                    default:
//                        break;
//                }
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception ex){
//            ex.printStackTrace();
//        }
//        return BaseResponse.successWithMsg("ok");
//    }

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
            lossPerCents.add(add);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Menu {
        //        id: 9,
//        label: '系统管理',
//        children
        private Long id;
        private String label;
        private List<Menu> children;
        @JsonIgnore
        private Integer sort;
    }


}
