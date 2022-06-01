package com.qk.reggie.controller;

import com.qk.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    //定义一个文件上传的路径，路径文件在配置文件中读取
    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件的上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> update(MultipartFile file) {       //file参数名已经和前端关联，此处文件名必须是file
        log.info(file.toString());
        //获取上传文件的原始文件名
        String originalFilename = file.getOriginalFilename();
        //suffix 为文件的后缀名
         String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新生成文件名，防止文件名重复造成文件覆盖
        String fileName = UUID.randomUUID().toString()+suffix;         //32位文件名

        //创建一个目录对象
        File dir =new File(basePath);
        //判断目录是否存在
        if (!dir.exists()){
            //不存在目录，则需要创建
            dir.mkdirs();
        }
        try {
            //文件转存到
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //文件上传完毕后返回文件名称，将文件存入数据库
        return R.success(fileName);
    }
}
