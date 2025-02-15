package com.github.herdeny.healthaiv2.service.impl;

import com.github.herdeny.healthaiv2.service.M2STGAT_Service;
import com.github.herdeny.healthaiv2.utils.SseClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class M2STGAT_ServiceImpl implements M2STGAT_Service {
    @Value("${PYTHON_PATH}")
    private String PYTHON_PATH;
    @Value("${DATA_PATH}")
    private String DATA_PATH;
    @Value("${SELECT_GENE_PATH}")
    private String SELECT_GENE_PATH;
    @Value("${GENERATE_ADJ_MATRIX_PATH}")
    private String GENERATE_ADJ_MATRIX_PATH;
    @Value("${MEGENA_PATH}")
    private String MEGENA_PATH;

    @Autowired
    private SseClient sseClient;

    @Override
    public JSONObject selectGene(String fileName, String uid) {
        JSONObject result = new JSONObject();
        boolean flag = true;
        String[] args = new String[]{PYTHON_PATH, SELECT_GENE_PATH, DATA_PATH, fileName};
        System.out.println("Start Select Gene...");
        sseClient.sendMessage(uid, uid + "-start-select-gene", "Start select gene...");
        try {
            Process process = Runtime.getRuntime().exec(args);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // Read the output
            String actionStr;
            while ((actionStr = in.readLine()) != null) {
                System.out.println(actionStr);
                String messageID = uid + "-" + UUID.randomUUID();
                sseClient.sendMessage(uid, messageID, actionStr);
            }

            String errorStr;
            while ((errorStr = err.readLine()) != null) {
                if (errorStr.contains("Error")) {
                    if (flag) flag = false;
                    String regex = "\\[(Errno|WinError)\\s+(\\d+)]";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(errorStr);
                    if (matcher.find()) {
                        result.put("code", matcher.group(2));
                    }
                    result.put("data", errorStr);
                    sseClient.sendMessage(uid, uid + "-error-select-gene", "select gene error");
                }
                System.err.println(errorStr);
            }

            in.close();
            err.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        result.put("success", flag);
        if (flag) {
            sseClient.sendMessage(uid, uid + "-end-select-gene", "Complete select gene...");
            result.put("code", 0);
        }
        return result;
    }

    @Override
    public JSONObject generateAdjMatrix(String fileName, String uid) {
        JSONObject result = new JSONObject();
        AtomicBoolean flag = new AtomicBoolean(true);
        String[] args = new String[]{PYTHON_PATH, GENERATE_ADJ_MATRIX_PATH, DATA_PATH, fileName};
        System.out.println("Start generate matrix...");
        sseClient.sendMessage(uid, uid + "-start-generate-matrix", "Start generate matrix...");
        try {
            Process process = Runtime.getRuntime().exec(args);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // 创建线程分别处理标准输出流和错误输出流
            Thread outputThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                        // 将每一行的输出发送给 SSE
                        String messageID = uid + "-" + UUID.randomUUID();
                        sseClient.sendMessage(uid, messageID, line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try {
                    String errorLine;
                    while ((errorLine = err.readLine()) != null) {
                        System.err.println(errorLine);
                        // 如果错误信息包含“Error”，则记录错误
                        if (errorLine.contains("Error")) {
                            if (flag.get()) flag.set(false);
                            String regex = "\\[(Errno|WinError)\\s+(\\d+)]";
                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher(errorLine);
                            if (matcher.find()) {
                                result.put("code", matcher.group(2));
                            }
                            result.put("data", errorLine);
                            sseClient.sendMessage(uid, uid + "-error-generate-matrix", "generate matrix error");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 启动这两个线程
            outputThread.start();
            errorThread.start();

            // 等待线程执行完成
            outputThread.join();
            errorThread.join();


            in.close();
            err.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        result.put("success", flag.get());
        if (flag.get()) {
            sseClient.sendMessage(uid, uid + "-end-generate-matrix", "Complete generate matrix...");
            result.put("code", 0);
        }
        return result;
    }

    @Override
    public JSONObject generateGeneMap(String fileName, String uid) {
        JSONObject result = new JSONObject();
        boolean flag = true;
        String[] args = new String[]{
                "Rscript", MEGENA_PATH, DATA_PATH, fileName
        };
        System.out.println("Start Generate GeneMap...");
        sseClient.sendMessage(uid, uid + "-start-create-geneMap", "Start Generate GeneMap...");
        try {
            Process process = Runtime.getRuntime().exec(args);

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // Read the output
            String actionStr;
            while ((actionStr = in.readLine()) != null) {
                System.out.println(actionStr);
                String messageID = uid + "-" + UUID.randomUUID();
                sseClient.sendMessage(uid, messageID, actionStr);
            }

            String errorStr;
            while ((errorStr = err.readLine()) != null) {
                if (errorStr.contains("error:")) {
                    if (flag) flag = false;
                    String regex = "\\[(Errno|WinError)\\s+(\\d+)]";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(errorStr);
                    if (matcher.find()) {
                        result.put("code", matcher.group(2));
                    }
                    result.put("data", errorStr.substring(errorStr.indexOf("error:") + 7));
                    sseClient.sendMessage(uid, uid + "-error-create-geneMap", "create geneMap error");
                }
                System.err.println(errorStr);
            }
            in.close();
            err.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        result.put("success", flag);
        if (flag) {
            System.out.println("Complete Generate GeneMap");
            sseClient.sendMessage(uid, uid + "-end-create-geneMap", "Complete Generate GeneMap");
            result.put("code", 0);
        }
        return result;
    }
}
