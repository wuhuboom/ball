package com.oxo.ball.service;

import com.oxo.ball.bean.dao.BallPlayer;

public interface IBasePlayerService {
    BallPlayer findById(Long id);
    BallPlayer findByIdNoCache(Long id);
    BallPlayer findByUsername(String username);
    BallPlayer findByPhone(String areaCode, String phone);
    BallPlayer findByUserId(Long userId);
    BallPlayer findByInvitationCode(String invitationCode);
    Long createUserId();
    boolean editAndClearCache(BallPlayer edit,BallPlayer db);

    void editMultGroupNum(String treeIds,int quantity);

    void clearFrozen();

}
