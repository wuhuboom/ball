package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dao.BallVip;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.admin.IBallVipService;
import com.oxo.ball.utils.MapUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@RestController
@RequestMapping("/ball/merchant/vip")
public class BallVipController {

    @Resource
    IBallVipService vipService;
    @Resource
    IBallSystemConfigService systemConfigService;
    @PostMapping
    public Object index(BallVip query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallVip> search = vipService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object getConfig(){
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        Map<String,Object> data = new HashMap<>();
        data.put("checkTime",systemConfig.getCheckLevelTime());
        data.put("rewardTime",systemConfig.getVipRewardTime());
        return BaseResponse.successWithData(data);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallVip ballVip){
        BallVip insert = new BallVip();
        try {
            insert = vipService.insert(ballVip);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return insert.getId()!=null?BaseResponse.successWithData(insert):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallVip ballVip){
        BaseResponse res = vipService.edit(ballVip);
        return  res;
    }
    @GetMapping("edit")
    public Object editSaveConfig(String time,String reward){
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        systemConfigService.edit(BallSystemConfig.builder()
                .id(systemConfig.getId())
                .checkLevelTime(time)
                .vipRewardTime(reward)
                .build());
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e58"));
    }
    @PostMapping("status")
    public Object status(@RequestBody BallVip ballVip){
        Boolean aBoolean = vipService.status(ballVip);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = vipService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
