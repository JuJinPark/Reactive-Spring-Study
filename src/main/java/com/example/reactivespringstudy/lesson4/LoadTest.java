package com.example.reactivespringstudy.lesson4;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class LoadTest {

   static AtomicInteger counter =  new AtomicInteger ();

    public static void main(String[] args) throws InterruptedException {
        //server.tomcat.max-threads=1 값을 바꿔가면 응답속도를 측정 또는 visual vm으로 실제 쓰레드 라이프사이클 확인
        loadTest("http://localhost:8080/async");
        loadTest("http://localhost:8080/callable");
        loadTest("http://localhost:8080/emitter");
        loadTest("http://localhost:8080/dr");
    }

    private static void loadTest(String url) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();

        StopWatch main = new StopWatch();
        main.start();

        for (int i = 0; i < 100; i++) {
            es.execute(() -> {
                int idx = counter.addAndGet(1);
                log.info("Thread "+idx);

                StopWatch sw = new StopWatch();
                sw.start();

                rt.getForObject(url,String.class);

                sw.stop();
                log.info("Elaspsed: {} {}", idx , sw.getTotalTimeSeconds() );
            });
        }
        es.shutdown();
        es.awaitTermination(100,  TimeUnit.SECONDS);
        main.stop();

        log.info("Total: {}",main.getTotalTimeSeconds());
    }

}
