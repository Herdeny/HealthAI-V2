package com.github.herdeny.healthaiv2.controller;

import com.github.herdeny.healthaiv2.pojo.Result;
import com.github.herdeny.healthaiv2.service.M2STGAT_Service;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Map;

/**
 * M2STGAT 模块
 * 包含挑选基因、生成基因图谱、邻接矩阵转化等操作
 */
@RestController
@RequestMapping("/M2STGAT")
public class M2STGAT_Controller extends CommonController {

    @Value("${DATA_PATH}")
    private String DATA_PATH;

    @Autowired
    private M2STGAT_Service m2stgatService;

    /**
     * 挑选基因
     * 调用用户上传的 csv 文件进行基因挑选，生成的文件名为 filename_1000.csv
     * @param fileName 上传的文件名
     * @param uid 用于指定SSE发送端口
     * @return
     */
    @RequestMapping("/selectGene")
    public Result<Map<String, Object>> selectGene(@RequestParam String fileName, String uid) {
        JSONObject result;
        result = m2stgatService.selectGene(fileName, uid);
        return Result.success(result.toMap());
    }

    /**
     * 生成基因图谱
     * @param fileName 用于生成基因图谱的文件名，通常为 上传文件名_1000.csv
     * @return
     */
    @RequestMapping("/createGeneMap")
    public Result<Map<String, Object>> generateGeneMap(@RequestParam String fileName, String uid) {
        JSONObject result;
        result = m2stgatService.generateGeneMap(fileName, uid);
        return Result.success(result.toMap());
    }

    /**
     * 邻接矩阵转化
     * @param fileName 用于生成邻接矩阵的文件名，通常为 上传文件名_1000.csv
     * @return
     */
    @RequestMapping("/createAdjMatrix")
    public Result<Map<String, Object>> adjacencyMatrixConversion(@RequestParam String fileName, String uid) {
        JSONObject result;
        result = m2stgatService.generateAdjMatrix(fileName, uid);
        return Result.success(result.toMap());
    }

    /**
     * 获取PFN邻接表
     *
     * @param response HttpServletResponse
     * @return
     */
    @GetMapping("/getFile")
    public void getFile(HttpServletResponse response, @RequestParam String fileName) {
        String filePath = DATA_PATH + fileName;
        // 判断GRN路径是否存在
        if (!new File(filePath).exists()) {
            return;
        }
        response.setContentType("text/csv");
        readFile(response, filePath);
    }
}
