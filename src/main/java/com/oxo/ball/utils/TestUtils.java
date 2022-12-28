package com.oxo.ball.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import java.text.ParseException;
import java.util.*;

public class TestUtils {

    public static void main(String[] args) throws JsonProcessingException {
//        System.out.println(TimeUtil.getNowWeek());
        //        for(int i=3;i<=200;i++){
//            System.out.println("e"+i+": '',");
//        }
//        TimeZone timeZone = TimeZone.getTimeZone("Africa/Dar_es_Salaam");
//        System.out.println(timeZone);
//        System.out.println(BigDecimalUtil.div(1,2322));
//        Double moneyd = BigDecimalUtil.mul(23220000, Double.parseDouble("0.0004306632"));
//        System.out.println(moneyd);
        //        System.out.println(TimeUtil.getEndDayOfYesterday().getTime()/1000);
//        System.out.println(TimeUtil.TIME_ONE_DAY);
//        System.out.println(PasswordUtil.genPasswordMd5("123123"));
//        System.out.println(BigDecimalUtil.toString(BigDecimalUtil.mul(1000000000,100)));
//        System.out.println(TimeUtil.getNowHour());
//        System.out.println(TimeUtil.getNowMin());
//        String singStr= "goods_name=bkash2002&mch_id=997997997&mch_order_no=904202983&notify_url=http://139.180.155.239:10100/player/pay/callback/4&order_date=2022-09-04 20:28:38&pay_type=2120&trade_amount=200.0&key=f9616cdb340a4d3688ad4c6151bc83c1";
//        String singStr = "goods_name=test&mch_id=123456789&mch_order_no=2021-04-13 17:32:28&notify_url=http://www.baidu.com/notify_url.jsp&order_date=2021-04-13 17:32:25&pay_type=122&trade_amount=100&version=1.0&key=0936D7E86164C2D53C8FF8AD06ED6D09";
//        String singStr = "goods_name=test&mch_id=997997997&mch_order_no=2021-04-1317:32:29&notify_url=http://www.baidu.com/notify_url.jsp&order_date=2021-04-13 17:32:25&pay_type=2120&trade_amount=100&version=1.0&key=f9616cdb340a4d3688ad4c6151bc83c1";
//        String signStr=  "goods_name=test&mch_id=997997997&mch_order_no=2021-04-1317:32:29&notify_url=http://www.baidu.com/notify_url.jsp&order_date=2021-04-13 17:32:25&pay_type=2120&trade_amount=100&version=1.0";
//        String signStr= "apply_date=2022-09-06 17:17:52&back_url=http://139.180.155.239:10100/player/pay/callback/5&bank_code=GHSAABL&mch_id=997997997&mch_transferId=906132139aa&receive_account=111111&receive_name=111&receiver_telephone=546778997&remark=11223344556677&transfer_amount=1087.09&key=NM6DXAEG1TXHOFHRRUVKHA9M6GUSPJ7R";
//        String signStr= "apply_date=2022-10-11 10:19:26&back_url=http://207.148.117.157:10100/player/pay/callback/5&bank_code=GHSMTN&mch_id=801037888&mch_transferId=1010195800&receive_account=544813825&receive_name=Acheampong micha&receiver_telephone=5283184450&remark=GHA-726265600-2&transfer_amount=0.81&key=00SKKGHCANH6MPHULQ3MKYP5N3JHKLPW";
//        String signStr= "apply_date=2022-10-13 12:20:11&back_url=http://139.180.155.239:10100/player/pay/callback/11&bank_code=STATE BANK OF INDIA&mch_id=222887002&mch_transferId=926222739&receive_account=917382177298&receive_name=SAICHARAN&receiver_telephone=null&remark=null&transfer_amount=112.23&key=MZBG89MDIBEDWJOJQYEZVSNP8EEVMSPM";
//        String signStr= "goods_name=wowpay&mch_id=222887002&mch_order_no=1012156210&notify_url=http://139.180.155.239:10100/player/pay/callback/10&order_date=2022-10-12 15:59:17&pay_type=105&trade_amount=500.0&version=1.0&key=TZLMQ1QWJCUSFLH02LAYRZBJ1WK7IHSG";

//        System.out.println(PasswordUtil.genMd5(signStr).toLowerCase());
//        System.out.println(PasswordUtil.genMd5("abcdefg").toLowerCase());
//        System.out.println(SignAPI.sign(signStr,"f9616cdb340a4d3688ad4c6151bc83c1"));
        //        System.out.println(TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS));
//        System.out.println(111/100f);
//        System.out.println(TimeUtil.getDayEnd().getTime());
//        Double d = BigDecimalUtil.div(BigDecimalUtil.mul(111,1000),100);
//        long commission = d.longValue();
//        System.out.println(commission);
//        Set<String> set = new HashSet<>();
//        System.out.println(set.add("1"));
//        System.out.println(set.add("1"));
//        System.out.println("123".split(",")[0]);
//        Double mul = BigDecimalUtil.mul(7.2, BigDecimalUtil.PLAYER_MONEY_UNIT);
//        System.out.println(mul.longValue());
//        System.out.println(!"香港,香港,香港,香港,香港,香港,香港,香港,香港,香港,香港".contains("香港"));
//        System.out.println(BallPlayer.getTreeCount("_1_"));
//        long startTime = 1660987103000L;
//        long startTime = 1660976303000L;
//        if(startTime+90*TimeUtil.TIME_ONE_MIN>System.currentTimeMillis()){
//            System.out.println("还未到90分钟");
//        }else{
//            System.out.println("比赛超过90分钟了");
//        }
//        System.out.println("_1_".split("_").length);
//        long bingo = 14007;
//        long bet =  10003;
//        double div = BigDecimalUtil.div(bingo, bet);
//        //除法,玩家盈亏率 250%,系统亏损
//        System.out.println(BigDecimalUtil.format2(100-div*100));
//        String data = "merchant_no=6058673&notice_url=http://45.76.146.183:10100/pay/callback/2&order_amount=20000&order_no=8081202021000030&payment_code=1007&timestamp=1659960122&key=TNO6tjwMlEjiiXw7KTz6hOLCJ3tEh7gm";
//        System.out.println(PasswordUtil.genMd5(data));
//        sortList.add(null);
//        System.out.println(sortList==null);
//        System.out.println(sortList.isEmpty());
//        System.out.println(sortList.get(0)==null);
//        List<String> l = sortList.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
//        System.out.println(l.isEmpty());
        List<String> sortList = new ArrayList<>();
        sortList.add("mer_no");
        sortList.add("settle_id");
        sortList.add("currency");
        sortList.add("settle_amount");
        sortList.add("bankCode");
        sortList.add("accountName");
        sortList.add("accountNo");
        sortList.add("ifsc");
        sortList.add("settle_date");
        sortList.add("notifyUrl");
        Collections.sort(sortList);
        System.out.println(sortList);
//        StringBuilder sb = new StringBuilder();
//        int i=0;
//        for(String item:sortList){
//            sb.append(item);
//            sb.append("={"+(i++)+"}&");
//        }
//        System.out.println(sb);
//        for(String item:sortList){
//            System.out.println(item);
//        }
//        System.out.println(UUIDUtil.getServletPath("http://localhost:10010/pay/callback/1"));
//        double a = 0;
//        a+=8699.00;
//        a+=7999;
//        a+=1000.00;
//        a+=25000.00;
//        a+=10000.00;
//        a+=65000.00;
//        a+=5700.00;
//        a+=10000.00;
//        a+=600.00;
//        a+=5800.00;
//        a+=1000.00;
//        a+=9800.00;
//        a+=9800.00;
//        a+=9800.00;
//        a+=9500.00;
//        a+=344.88;
//        double b = 132298+15000;
//        System.out.println(a);
//        System.out.println(a-b);
//        System.out.println(UUIDUtil.getUUID().substring(6,12));
        //0.1%
//        Double profit = BigDecimalUtil.div(BigDecimalUtil.mul(10000, Double.valueOf("0.1")),BigDecimalUtil.PLAYER_MONEY_UNIT);
//        System.out.println(profit);
//        String encrypt = Base64Util.encrypt("203943948844");
//        System.out.println(encrypt);
//        String abc = Base64Util.decrypt(encrypt);
//        System.out.println(abc);
//        String url = "http://localhost:100010/pay/callback/1";
//
//        url = url.substring(url.indexOf(":")+1);
//        System.out.println(url);
//        url = url.substring(url.indexOf(":")+1);
//        System.out.println(url);
//        url = url.substring(url.indexOf("/"));
//        System.out.println(url);
//        System.out.println(JsonUtil.toJson(PayParamDto.builder()
//                .platformOrder("6271000011")
//                .username("user1")
//                .rechargeType(PayParamDto.PAY_TYPE_USDT)
//                .accountOrders(200F)
//                .backUrl("xx")
//                .build()));
//        System.out.println(BigDecimalUtil.div(1111,100,2));
//        System.out.println(Double.valueOf("15.15"));
//        for(int i=0;i<10;i++){
//            System.out.println(RandomUtils.nextInt(0,3));
//        }
//        Long money = 10000L;
//        Double rate = BigDecimalUtil.mul(0.1, money);
//        System.out.println(rate/100);
//        System.out.println(PasswordUtil.genPasswordMd5("123123"));
//        double div = BigDecimalUtil.div(1, 100);
//        Double disc = BigDecimalUtil.mul(11100, div);
//        System.out.println(disc.longValue());
//        System.out.println(BigDecimalUtil.antiPerCent("226"));
//        System.out.println(TimeUtil.isDouble("0.0")<=0);
//        String ptree = "_1_2_";
//        String stree = "_1_2_3_4_";
//        String substring = stree.substring(ptree.length());
//        System.out.println(Arrays.toString(substring.split("_")));
//        System.out.println(BigDecimalUtil.antiPerCent("99.96"));
//        List<String> scores = new ArrayList<>();
//        scores.add("1");
//        scores.add("10");
//        scores.add("12");
//        scores.add("13");
//        scores.add("*");
//        Collections.sort(scores, new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//                if(o1.equals("*")){
//                    return 1;
//                }
//                return Integer.parseInt(o1)-Integer.parseInt(o2);
//            }
//        });
//        System.out.println(scores);
        // de8776116c28303c8d03be3b4e0cdfc9
//        System.out.println(PasswordUtil.genPasswordMd5("1234567"));
//        System.out.println(PasswordUtil.genPasswordMd5("123456"));
//        System.out.println("_1_".substring(0));
//        Double odds = Double.valueOf("0.5");
//        double div = BigDecimalUtil.div(odds, 100);
//        long bingo = Double.valueOf(BigDecimalUtil.mul(div, 21100)).longValue();
//        Long oddsLong = Double.valueOf(Double.valueOf("5")*100).longValue();
//        Long bingo = 20000+21100/10000*oddsLong;
//        System.out.println(21100+bingo);
//        edit.setWinningAmount(bet.getBetMoney()+bet.getBetMoney()*oddsLong);

//        System.out.println("1".equals(String.valueOf(1)));
//        System.out.println(TimeUtil.TIME_ONE_DAY);
//        long curr = TimeUtil.getNowTimeMill();
//        for(int i=0;i<20;i++){
//            if(i%2==0){
//                System.out.println("INSERT INTO `ball_game` (`game_logo`, `alliance_logo`, `alliance_name`, `main_logo`, `main_name`, `guest_logo`, `guest_name`, `start_time`, `score`, `game_status`, `top`, `hot`, `even`, `status`, `created_at`, `updated_at`) VALUES ('', '', 'Salted fish', '', 'holy shit', '', 'kick ass', '"+(curr-((i%7)*TimeUtil.TIME_ONE_DAY))+"', '1-1/(0-0)', '3', '2', '1', '2', '3', '"+(curr-((i%7)*TimeUtil.TIME_ONE_DAY))+"', '1651133045779');");
//            }else{
//                System.out.println("INSERT INTO `ball_game` (`game_logo`, `alliance_logo`, `alliance_name`, `main_logo`, `main_name`, `guest_logo`, `guest_name`, `start_time`, `score`, `game_status`, `top`, `hot`, `even`, `status`, `created_at`, `updated_at`) VALUES ('', '', 'Salted fish', '', 'holy shit', '', 'kick ass', '"+(curr-((i%7)*TimeUtil.TIME_ONE_DAY))+"', '0-1/(0-0)', '3', '2', '1', '2', '3', '"+(curr-((i%7)*TimeUtil.TIME_ONE_DAY))+"', '1651133045779');");
//            }
//        }
    }
}
