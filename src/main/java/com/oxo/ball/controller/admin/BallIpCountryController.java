package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallIpCountry;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallIpCountryService;
import com.oxo.ball.service.admin.IBallIpCountryService;
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
@RequestMapping("/ball/merchant/ip_country")
public class BallIpCountryController {
    @Resource
    IBallIpCountryService ipCountryService;
    @PostMapping
    public Object index(BallIpCountry query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallIpCountry> search = ipCountryService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallIpCountry sysUserRequest){
        BallIpCountry insert = new BallIpCountry();
        try {
            insert = ipCountryService.insert(sysUserRequest);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return insert.getId()!=null?BaseResponse.successWithData(insert):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallIpCountry ballPlayer){
        Boolean aBoolean = ipCountryService.edit(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallIpCountry ballPlayer){
        Boolean aBoolean = ipCountryService.status(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = ipCountryService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
