package com.oxo.ball.contant;

public class RedisKeyContant {
    //玩家相关
    public static final String PLAYER_ACTIVITY = "ball_player_activity:";
    public static final String PLAYER_ONLINE = "ball_player_online:";
    public static final String PLAYER_PHONE_CODE = "ball_player_phone_code:";
    public static final String PLAYER_PHONE_CODE_LIST = "ball_player_phone_code_List:";
    public static final String PLAYER_PHONE_SMS_COUNT = "player_phone_sms_count:";
    public static final String PLAYER_WITHDRAWAL_LIMIT = "player_withdrawal_limit:";
    public static final String PLAYER_SIM_LIMIT = "player_sim_limit:";

    public static final String PLAYER_CHAT_MESSAGE = "player_chat_message";


    //    verifyKey
    public static final String VERIFY_KEY = "ball_player_regist_verify_key:";
    public static final String PLAYER_LOGIN_FAIL_COUNT = "ball_player_login_fail_count:";
    public static final String BET_ORDER_NO = "ball_bet_order_no";
    public static final String RECHARGE_ORDER_NO = "ball_recharge_order_no";
    public static final String WITHDRAWAL_ORDER_NO = "ball_withdrawal_order_no";


    //赛事接口缓存数据
    public static final String LEAGUES_LIST = "ball_leagues_list";
    public static final String LEAGUES_HAS_QUERY = "ball_leagues_list_has_query";
    public static final String GAME_NEED_QUERY_ODDS = "ball_game_need_query_odds";
    public static final String GAME_BET_TOTAL = "game_bet_total:";


    //队列
    public static final String LOGGER_QUEUE = "ball_ball_logger_queue";

    //充值
    public static final String PLAYER_PAY_ORDER = "player_pay_order:";
    public static final String PLAYER_PAY_MINMAX = "player_pay_minmax_1:";
    public static final String PLAYER_PAY_CALLBACK = "player_pay_callback:";


    //活跃天数
    public static final String PLAYER_BET_ACTIVITY_DAYS = "player_bet_activity_days:";
    public static final String GAME_STATUS_EXT = "game_status_ext:";
}
