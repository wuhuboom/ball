package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallApp;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallAppService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@RestController
@RequestMapping("/ball/merchant/app")
public class BallAppController {

    @Resource
    IBallAppService vipService;
    @PostMapping
    public Object index(BallApp query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallApp> search = vipService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object getAll(){
        List<BallApp> byAll = vipService.findByAll();
        return BaseResponse.successWithData(byAll);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallApp ballApp){
        BallApp insert = new BallApp();
        try {
            insert = vipService.insert(ballApp);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return insert.getId()!=null?BaseResponse.successWithData(insert):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallApp ballApp){
        Boolean aBoolean = vipService.edit(ballApp);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallApp ballApp){
        Boolean aBoolean = vipService.status(ballApp);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = vipService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
