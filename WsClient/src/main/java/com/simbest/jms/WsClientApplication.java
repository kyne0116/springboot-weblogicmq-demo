package com.simbest.jms;


import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

/**
 * @author OSB集成平台
 */
@SpringBootApplication
@Slf4j
public class WsClientApplication {

    public static void main(String[] args) throws IOException {
        String url = "http://localhost:7070/sendSampleTopic";
        ApplicationContext context = SpringApplication.run(WsClientApplication.class, args);
        Request request = new Request.Builder().url(url).build();
        Response response = context.getBean(OkHttpClient.class).newCall(request).execute();
        String result = response.body().string();
        log.debug("请求调用完毕!，url:'{}'，result:'{}'", url, result);
        SpringApplication.exit(context, () -> 0);
    }
}
