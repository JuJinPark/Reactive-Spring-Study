package com.example.reactivespringstudy.lesson3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

@Slf4j
public class SchedulerEx {

    public static void main(String[] args) {
        Publisher<Integer> pub = normalPub();

        //subOnPub 예제
        Publisher<Integer> subOnPub = subOnPub(pub);
        subOnPub.subscribe(normalSubscriber());

        //punOnPub 예제
        Publisher<Integer> pubOnPub = pubOnPub(pub);
        pubOnPub.subscribe(normalSubscriber());


        //이런식으로 두개 사용가능
        Publisher<Integer> pubOnPub2 = pubOnPub(pub);
        Publisher<Integer> subOnPub2 = subOnPub(pubOnPub2);
        subOnPub2.subscribe(normalSubscriber());

        System.out.println("exit");

    }


    private static Subscriber<Integer> normalSubscriber() {
        return new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                log.debug("onNext:{}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError:{}", t);
            }

            @Override
            public void onComplete() {
                log.debug("onClomplete");
            }
        };
    }

    private static Publisher<Integer> normalPub() {
        return sub -> sub.onSubscribe(new Subscription() {
            @Override
            public void request(long n) {
                log.debug("request");
                sub.onNext(1);
                sub.onNext(2);
                sub.onNext(3);
                sub.onNext(4);
                sub.onNext(5);
                sub.onComplete();
            }

            @Override
            public void cancel() {

            }
        });
    }

    private static Publisher<Integer> pubOnPub(Publisher<Integer> pub) {
        return sub -> pub.subscribe(new Subscriber<>() {
            ExecutorService es = Executors
                .newSingleThreadExecutor(new CustomizableThreadFactory() {
                    @Override
                    public String getThreadNamePrefix() {
                        return "pubOn-";
                    }
                });

            @Override
            public void onSubscribe(Subscription s) {
                sub.onSubscribe(s);
            }

            @Override
            public void onNext(Integer integer) {
                es.execute(() -> sub.onNext(integer));
            }

            @Override
            public void onError(Throwable t) {
                es.execute(() -> sub.onError(t));
                es.shutdown();
            }

            @Override
            public void onComplete() {
                es.execute(() -> sub.onComplete());
                es.shutdown();
            }
        });
    }

    private static Publisher<Integer> subOnPub(Publisher<Integer> pub) {
        return s -> {
            ExecutorService ex = Executors.newSingleThreadExecutor(new CustomizableThreadFactory(){
                @Override
                public String getThreadNamePrefix() {
                    return "subOn-";
                }
            });
            ex.execute(() -> pub.subscribe(s));

        };
    }
}
