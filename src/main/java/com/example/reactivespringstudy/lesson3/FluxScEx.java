package com.example.reactivespringstudy.lesson3;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class FluxScEx {

    public static void main(String[] args) throws InterruptedException {
        Flux.interval(Duration.ofMillis(500))
            .subscribe(s -> log.debug("onNext:{}",s));
        System.out.println("exit");
        TimeUnit.SECONDS.sleep(5);
    }

}
