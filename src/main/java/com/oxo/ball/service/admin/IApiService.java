package com.oxo.ball.service.admin;

public interface IApiService {

    //每小时一次
    void refreshFixtures();
    //每天一次
    void refreshFixturesAll(boolean isToday);
    void refreshFixturesAllTest(boolean isToday);
    //刷新赔率,仅一次
    void refreshOdds();
    void refreshOddsTest(Long gameId,String json);

    void tgNotice(Long gameId,String message);

    void tgNotice(String message);
}
