package com.simbest.jms;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用途： 
 * 作者: lishuyi 
 * 时间: 2018/1/23  23:52 
 */
@Configuration
public class OkHttpClientConfig {

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }
}
