package com.oxo.ball.service.admin;


import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallPlayer;

/**
 * @author flooming
 */
public interface AuthService {
    int TOKEN_INVALID = 402;
    int HAVE_NO_AUTH = 401;
    String buildToken(BallAdmin user);
    int checkAuth(String token, String path);
    void clearAuth(BallAdmin user);
    void clearCurrentUser();
    BallAdmin getUserFromToken(String token);
}
