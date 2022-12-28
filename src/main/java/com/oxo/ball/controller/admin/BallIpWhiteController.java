package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallIpWhite;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallIpWhiteService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 系统公告 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/merchant/white")
public class BallIpWhiteController {
    @Resource
    IBallIpWhiteService ipWhiteService;
    @PostMapping
    public Object index(BallIpWhite query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallIpWhite> search = ipWhiteService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallIpWhite sysUserRequest){
        BallIpWhite insert = new BallIpWhite();
        try {
            insert = ipWhiteService.insert(sysUserRequest);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return insert.getId()!=null?BaseResponse.successWithData(insert):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallIpWhite ballPlayer){
        Boolean aBoolean = ipWhiteService.edit(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallIpWhite ballPlayer){
        Boolean aBoolean = ipWhiteService.status(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = ipWhiteService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
