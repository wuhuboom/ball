package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallSlideshow;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallSlideshowMapper;
import com.oxo.ball.service.admin.IBallSlideshowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.utils.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * <p>
 * 轮播图 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallSlideshowServiceImpl extends ServiceImpl<BallSlideshowMapper, BallSlideshow> implements IBallSlideshowService {

    @Value("${static.file}")
    private String staticFile;

    @Override
    public SearchResponse<BallSlideshow> search(BallSlideshow queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallSlideshow> response = new SearchResponse<>();
        Page<BallSlideshow> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallSlideshow> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(!StringUtils.isBlank(queryParam.getLanguage())){
            query.eq("language",queryParam.getLanguage());
        }
        IPage<BallSlideshow> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallSlideshow insert(BallSlideshow slideshow, MultipartFile file) {
        uploadFile(slideshow,file);
        save(slideshow);
        return slideshow;
    }

    @Override
    public Boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    public Boolean edit(BallSlideshow slideshow,MultipartFile file) {
        uploadFile(slideshow,file);
        return updateById(slideshow);
    }

    @Override
    public Boolean status(BallSlideshow slideshow) {
        BallSlideshow edit = BallSlideshow.builder()
                .status(slideshow.getStatus())
                .build();
        edit.setId(slideshow.getId());
        return updateById(edit);
    }

    private void uploadFile(BallSlideshow slideshow, MultipartFile file) {
        String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
        if(file!=null && !file.isEmpty()){
            String webpath = "activity/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            //后缀
            String subfex = originalFilename.substring(originalFilename.lastIndexOf("."));
            String saveName = UUIDUtil.getUUID()+subfex;
            try {
                InputStream inputStream = file.getInputStream();
                String savePath = fileRootPath+saveName;
                FileOutputStream fos = new FileOutputStream(savePath);
                byte[] b = new byte[128];
                int len;
                while((len = inputStream.read(b))!=-1){
                    fos.write(b,0,len);
                }
                fos.flush();
                fos.close();
                inputStream.close();
                slideshow.setLocalPath(webpath+saveName);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
