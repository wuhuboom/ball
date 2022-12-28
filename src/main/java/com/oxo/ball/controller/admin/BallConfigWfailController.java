package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallConfigWfail;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallConfigWfailService;
import com.oxo.ball.service.admin.IBallConfigWfailService;
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
@RequestMapping("/ball/merchant/wfail")
public class BallConfigWfailController {

    @Resource
    IBallConfigWfailService configWfailService;
    @PostMapping
    public Object index(BallConfigWfail query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallConfigWfail> search = configWfailService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object getAll(){
        List<BallConfigWfail> byAll = configWfailService.findByAll();
        return BaseResponse.successWithData(byAll);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallConfigWfail configWfail){
        BallConfigWfail insert = new BallConfigWfail();
        try {
            insert = configWfailService.insert(configWfail);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return insert.getId()!=null?BaseResponse.successWithData(insert):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallConfigWfail configWfail){
        Boolean aBoolean = configWfailService.edit(configWfail);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallConfigWfail configWfail){
        Boolean aBoolean = configWfailService.status(configWfail);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = configWfailService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
