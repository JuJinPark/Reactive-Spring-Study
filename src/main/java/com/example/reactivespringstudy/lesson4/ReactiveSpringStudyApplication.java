package com.example.reactivespringstudy.lesson4;

import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@SpringBootApplication
@Slf4j
@EnableAsync
public class ReactiveSpringStudyApplication {

    @Component
    public static class MyService {
        @Async
        public Future<String> helloWithFuture() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(2000);
            return new AsyncResult<>("Hello") ;
        }

        @Async(value = "tp")
        public ListenableFuture<String> helloWithListenableFuture() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(2000);
            return new AsyncResult<>("Hello") ;
        }

    }
    @Bean
    ThreadPoolTaskExecutor tp() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(10);
        te.setMaxPoolSize(100);
        te.setQueueCapacity(200);
        te.setThreadNamePrefix("mythread");
        te.initialize();
        return te;
    }

    public static void main(String[] args) {
        // try with resource 블록을 이용해 빈이 다 준비된 후 종료되도록 설정
        try(ConfigurableApplicationContext c =
            SpringApplication.run(ReactiveSpringStudyApplication.class, args)){
        };
    }

    @Autowired
    MyService myService;

    //일종의 컨트롤러써 강제 실행
    @Bean
    ApplicationRunner run() {
        return args -> {
            log.info("runListenableFuture()");
            ListenableFuture<String> f = myService.helloWithListenableFuture();
            f.addCallback(s -> log.info(s), e-> log.info(e.getMessage()));
            log.info("exit ListenableFuture");
            Thread.sleep(2000);
        };
    }

    @Bean
    ApplicationRunner run2() {
        return args -> {
            log.info("runFuture()");
            Future<String> res = myService.helloWithFuture();
            log.info("exit Future: {}", res.isDone());
            log.info("result Future: {}", res.get());
        };
    }



}

