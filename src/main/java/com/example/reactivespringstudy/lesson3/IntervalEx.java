package com.example.reactivespringstudy.lesson3;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
public class IntervalEx {

    public static void main(String[] args) {
        Publisher<Integer> pub = normalPub();
        Publisher<Integer> takePub = takePub(pub);
        takePub.subscribe(subscriber());
    }

    private static Subscriber<Integer> subscriber() {
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

    private static Publisher<Integer> normalPub () {
       return sub -> sub.onSubscribe(new Subscription() {
            int no = 0;
            boolean cancel = false;

            @Override
            public void request(long n) {
                ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                exec.scheduleAtFixedRate(() -> {
                    if (cancel) {
                        exec.shutdown();
                        return;
                    }
                    sub.onNext(no++);
                }, 0, 300, TimeUnit.MILLISECONDS);
            }

            @Override
            public void cancel() {
                cancel = true;
            }
        });
    }

    private static Publisher<Integer> takePub (Publisher<Integer> pub) {
       return sub -> pub.subscribe(new Subscriber<>() {
           int counter = 0;
           Subscription subscription;

           @Override
           public void onSubscribe(Subscription s) {
               subscription = s;
               sub.onSubscribe(s);
           }

           @Override
           public void onNext(Integer integer) {
               sub.onNext(integer);
               if (++counter >= 10) {
                   subscription.cancel();
               }
           }

           @Override
           public void onError(Throwable t) {
               sub.onError(t);
           }

           @Override
           public void onComplete() {
               sub.onComplete();
           }
       });
    }

}
