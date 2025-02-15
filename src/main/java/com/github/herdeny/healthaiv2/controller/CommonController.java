package com.github.herdeny.healthaiv2.controller;

import com.github.herdeny.healthaiv2.pojo.Result;
import com.github.herdeny.healthaiv2.utils.StringTools;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * 通用 模块
 * 包含上传文件、读取文件等通用操作
 *
 * @Author Joel
 * @Create 2024/10/15 16:42
 * @Version 1.0
 */

@RestController
public class CommonController {

    @Value("${DATA_PATH}")
    private String DATA_PATH;

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);


    /**
     * 上传文件
     * 上传文件都保存在 DATA_PATH 目录下
     * @param file 用户上传文件
     * @return 文件上传结果+路径
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file){
        //检查文件是否为空
        if(file.isEmpty()){
            return Result.error("文件为空");
        }
        //检查目标文件夹是否存在，如果目标文件夹不存在，则创建
        System.out.println(DATA_PATH);
        File directory = new File(DATA_PATH);
        if(!directory.exists()){
            directory.mkdirs();
        }
        //保存文件
        try {
            File targetFile = new File(directory, Objects.requireNonNull(file.getOriginalFilename()));
            file.transferTo(targetFile);
            return Result.success("文件上传成功："+ targetFile.getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("上传文件时出现问题：" + e.getMessage());
        }
    }

    /**
     * 将指定文件的内容读取并写入到HTTP响应中
     *
     * @param response
     * @param filePath
     */
    protected void readFile(HttpServletResponse response, String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }
        OutputStream out = null;
        // 用于读取文件
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            // TODO: 读取文件
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            // 获取 HttpServletResponse 的输出流 out，用于将文件内容写入响应
            out = response.getOutputStream();
            int len = 0;
            // while循环读取文件内容，每次读取 1024 字节
            while ((len = in.read(byteData)) != -1) {
                // 将读取的数据写入输出流 out
                out.write(byteData, 0, len);
            }
            // 通过 out.flush() 刷新输出流，确保数据被发送
            out.flush();
        } catch (Exception e) {
            logger.error("读取文件异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
        }
    }
}
