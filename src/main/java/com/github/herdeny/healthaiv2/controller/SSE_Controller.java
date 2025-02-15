package com.github.herdeny.healthaiv2.controller;

import com.github.herdeny.healthaiv2.utils.SseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 模块
 * 用于SSE的连接、测试与结束
 */
@Controller
@RequestMapping("/sse")
public class SSE_Controller {
    @Autowired
    private SseClient sseClient;

    /**
     * 创建连接
     * @param uid 用于指定SSE发送端口
     * @return SseEmitter
     */
    @CrossOrigin
    @GetMapping("/createSse")
    public SseEmitter createConnect(@RequestParam String uid) {
        return sseClient.createSse(uid);
    }


    /**
     * 测试连接
     * 通过SSE发送测试语句
     * @param uid 用于指定SSE发送端口
     * @return ok
     */
    @CrossOrigin
    @GetMapping("/sendMsg")
    @ResponseBody
    public String sseChat(@RequestParam String uid) {
        for (int i = 0; i < 10; i++) {
            sseClient.sendMessage(uid, "no"+i, "Hello World");
        }
        return "ok";
    }

    /**
     * 关闭连接
     * @param uid 用于指定SSE发送端口
     */
    @CrossOrigin
    @GetMapping("/closeSse")
    public void closeConnect(@RequestParam String uid){
        sseClient.closeSse(uid);
    }
}