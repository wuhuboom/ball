package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallPlayer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * <p>
 * 玩家账号 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallPlayerMapper extends BaseMapper<BallPlayer> {

    @Update("update ball_player set group_size=group_size+${t} where id in (${ids})")
    void updateTreeGroupNum(@Param("ids") String treeIds,@Param("t") int quantity);

    /**
     * 字段名= replace(字段名, '查找内容', '替换成内容') ;
      */
    @Update("update ball_player set vip_rank=vip_rank+${rank}" +
            ",super_tree=replace(super_tree,'${oldSuperTree}','${newSuperTree}')" +
            " where super_tree like '${oldSuperTree}%'")
    int updateSuperTree(@Param("oldSuperTree") String oldSuperTree, @Param("newSuperTree") String newSuperTree, @Param("rank") int rank);

    IPage<BallPlayer> listPage(@Param("page") Page<BallPlayer> page, @Param("query") Map<String,Object> query);

    IPage<BallPlayer> listPageBet(@Param("page")Page<BallPlayer> page,  @Param("query") Map<String, Object> queryParam);
    IPage<BallPlayer> listPageBetAndRecharge(@Param("page")Page<BallPlayer> page,  @Param("query") Map<String, Object> queryParam);

    int standard(@Param("query") ReportStandardRequest reportStandardRequest);
    int standardGrouop(@Param("query") ReportStandardRequest reportStandardRequest);
}
