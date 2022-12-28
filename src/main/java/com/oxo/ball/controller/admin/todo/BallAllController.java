package com.oxo.ball.controller.admin.todo;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerBack;
import com.oxo.ball.bean.dao.BallLoggerRebate;
import com.oxo.ball.bean.dao.BallTodo;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallLoggerBackService;
import com.oxo.ball.service.admin.IBallLoggerRebateService;
import com.oxo.ball.service.admin.IBallTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 奖金策略 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/todo/all")
public class BallAllController {
    @Autowired
    BallAdminService adminService;
    @Resource
    IBallLoggerRebateService loggerRebateService;
    @Resource
    IBallLoggerBackService loggerBackService;
    @Resource
    IBallTodoService todoService;
    @PostMapping
    public Object index(BallTodo query,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        Map<String,Object> dataMap = new HashMap<>();
        //充值优惠
        List<BallLoggerRebate> search = loggerRebateService.searchRecharge2(BallLoggerRebate.builder()
                .playerName(query.getPlayerName())
                .build(),currentUser);
        dataMap.put("list1",search);
        //存款返佣
        List<BallLoggerRebate> list2 = loggerRebateService.search(BallLoggerRebate.builder()
                .playerName(query.getPlayerName())
                .build(),currentUser);
        dataMap.put("list2",list2);
        //六级返佣
        List<BallLoggerBack> list3 = loggerBackService.searchUnSettlement(BallLoggerBack.builder()
                .playerName(query.getPlayerName())
                .build(),currentUser);
        dataMap.put("list3",list3);
        //奖金
        List<BallTodo> list4 = todoService.search(BallTodo.builder()
                .playerName(query.getPlayerName())
                .build(), currentUser);
        dataMap.put("list4",list4);
        return BaseResponse.successWithData(dataMap);
    }

}
