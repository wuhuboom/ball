package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallBankCardService;
import com.oxo.ball.service.admin.IBallBankService;
import com.oxo.ball.service.admin.IBallLoggerBindCardService;
import com.oxo.ball.service.impl.admin.BallBankCardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 银行卡 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/finance/bind")
public class BallBankCardController {
    @Autowired
    IBallBankCardService bankCardService;
    @Autowired
    BallAdminService adminService;
    @Autowired
    IBallBankService bankService;
    @Autowired
    IBallLoggerBindCardService loggerBindCardService;
    @PostMapping
    public Object index(BallBankCard query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        if(query.getQueryType()==0){
            SearchResponse<BallBankCard> search = bankCardService.search(query, pageNo, pageSize);
            return BaseResponse.successWithData(search);
        }else{
            SearchResponse<BallLoggerBindCard> search = loggerBindCardService.search(query, pageNo, pageSize);
            return BaseResponse.successWithData(search);
        }
    }
    @GetMapping
    public Object getBanks(){
        List<BallBankArea> enabled = bankService.findEnabled();
//        List<BallBank> search = bankService.findAll(enabled.get(0).getCode(),enabled);
        List<BallBank> search = bankService.findAll();
        return BaseResponse.successWithData(search);
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallBankCard ballBankCard,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        ballBankCard.setOperUser(currentUser.getUsername());
        BaseResponse aBoolean = bankCardService.edit(ballBankCard);
        return  aBoolean;
    }
    @PostMapping("status")
    public Object status(@RequestBody BallBankCard ballBankCard,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        ballBankCard.setOperUser(currentUser.getUsername());
        Boolean aBoolean = bankCardService.status(ballBankCard);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("status")
    public Object delete(Long id){
        Boolean aBoolean = bankCardService.delete(id);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("delete error");
    }
    @PutMapping("status")
    public Object check(@RequestBody BallBankCard ballBankCard,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        ballBankCard.setOperUser(currentUser.getUsername());
        Boolean aBoolean = bankCardService.check(ballBankCard);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
}
