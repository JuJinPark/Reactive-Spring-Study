package com.example.reactivespringstudy.lesson4;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@SpringBootApplication
@Slf4j
@EnableAsync
public class ReactiveSpringStudyApplication2 {

    @RestController
    public static class MyController {

        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

        @GetMapping("/async")
        public String async() throws InterruptedException {
            Thread.sleep(2000);
            return "hello";
        }


        @GetMapping("/callable")
        public Callable<String> callable() throws InterruptedException {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }

        @GetMapping("/dr")
        public DeferredResult<String> dr() throws InterruptedException {
            log.info("dr");
            DeferredResult<String> dr = new DeferredResult<>(600000L);
            results.add(dr);
            return dr;
        }

        @GetMapping("/dr/count")
        public String drCount() throws InterruptedException {
          return String.valueOf(results.size());
        }

        @GetMapping("/dr/event")
        public String drevent(String message) throws InterruptedException {
            for (DeferredResult<String> dr : results) {
                dr.setResult("hello " + message);
                results.remove(dr);
            }
            return "OK";
        }

        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() throws IOException {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            Executors.newSingleThreadExecutor().submit(() -> {
                for (int i = 0; i <50; i++) {

                    try {
                        emitter.send("<p>Stream"+i+"</p>");
                        Thread.sleep(100);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            });
            return emitter;
        }

    }


    public static void main(String[] args) {
        SpringApplication.run(ReactiveSpringStudyApplication2.class, args);
    }




}
