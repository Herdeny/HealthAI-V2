package com.github.herdeny.healthaiv2.controller;

import com.github.herdeny.healthaiv2.pojo.Result;
import com.github.herdeny.healthaiv2.service.M2STGAT_Service;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
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
     * 调用用户上传的 csv 文件进行基因挑选，生成的文件名为 **`{fileName}_{num}.csv`**
     * **`{fileName}`**、**`{num}`** 与请求的参数值相同，如示例参数会生成`PPMI-data_M6_600.csv`
     *
     * @param fileName 上传的文件名
     * @param uid      用于指定SSE发送端口
     * @return
     */
    @RequestMapping("/selectGene")
    public Result<Map<String, Object>> selectGene(@RequestParam String fileName, String uid, String num) {
        JSONObject result;
        if (num == null) {
            num = "1000";
        }
        result = m2stgatService.selectGene(fileName, uid, num);
        return Result.success(result.toMap());
    }

    /**
     * 生成基因图谱
     * 利用挑选过后的基因文件生成基因图谱
     *
     * @param fileName 用于生成基因图谱的文件名，通常为 `{fileName}_{num}.csv`
     * @return
     */
    @RequestMapping("/createGeneMap")
    public Result<Map<String, Object>> generateGeneMap(@RequestParam String fileName, String uid) {
        JSONObject result;
        result = m2stgatService.generateGeneMap(fileName, uid);
        return Result.success(result.toMap());
    }

    /**
     * 模块聚类
     * 将PFN文件进行聚类，此接口必须在调用`/createGeneMap`接口后才能使用
     * 生成的文件名为`{fileName}_{num}_PFN_modules.csv`
     * 利用获取文件接口获取该文件进行绘图，第四行`module`列为模块编号
     *
     * @param fileName PFN文件名，通常为 `{fileName}_{num}_PFN.csv`
     * @return
     */
    @RequestMapping("/moduleCluster")
    public Result<Map<String, Object>> moduleCluster(@RequestParam String fileName, String uid) {
        JSONObject result;
        result = m2stgatService.ModuleCluster(fileName, uid);
        return Result.success(result.toMap());
    }

    /**
     * 邻接矩阵转化
     * 需要在参数内声明是什么时期（M12、M24、M36），以便于后续预测
     * @param fileName 用于生成邻接矩阵的文件名，通常为 `{fileName}_{num}.csv`
     * @param type 声明是什么时期（M12、M24、M36），以便于后续预测
     * @return
     */
    @RequestMapping("/createAdjMatrix")
    public Result<Map<String, Object>> adjacencyMatrixConversion(@RequestParam String fileName,@RequestParam String type,String uid) {
        JSONObject result;
        result = m2stgatService.generateAdjMatrix(fileName,type,uid);
        return Result.success(result.toMap());
    }

    /**
     * 调用模型预测
     * 调用模型进行预测，需要M12、M24、M36三个时期的基因节点数据（挑选后）和邻接矩阵数据
     * 这意味着必须分别对M12、M24、M36基因文件调用`/createGeneMap`和`/createAdjMatrix`接口后才能使用
     *
     * @param M12GeneFileName      M12基因文件名
     * @param M24GeneFileName      M24基因文件名
     * @param M36GeneFileName      M36基因文件名
     * @param M12AdjMatrixFileName M12邻接矩阵文件名
     * @param M24AdjMatrixFileName M24邻接矩阵文件名
     * @param M36AdjMatrixFileName M36邻接矩阵文件名
     * @return
     */
    @RequestMapping("/predict")
    public Result<Map<String, Object>> adjacencyMatrixConversion(@RequestParam String M12GeneFileName,
                                                                 @RequestParam String M24GeneFileName,
                                                                 @RequestParam String M36GeneFileName,
                                                                 @RequestParam String M12AdjMatrixFileName,
                                                                 @RequestParam String M24AdjMatrixFileName,
                                                                 @RequestParam String M36AdjMatrixFileName,
                                                                 String uid) {
        JSONObject result;
        result = m2stgatService.predict(M12GeneFileName, M12AdjMatrixFileName,
                M24GeneFileName, M24AdjMatrixFileName, M36GeneFileName, M36AdjMatrixFileName, uid);
        return Result.success(result.toMap());
    }

    /**
     * 获取文件
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
