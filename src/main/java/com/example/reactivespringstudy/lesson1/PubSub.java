package com.example.reactivespringstudy.lesson1;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;

public class PubSub {

    public static void main(String[] args) throws InterruptedException {
        // Publisher <-- Observable
        // Subscriber <- Observer
        List<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Publisher publisher = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = itr.iterator();

                subscriber.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {

                        executorService.execute(() -> {
                            int i = 0;
                                try {
                                    System.out.println("requested");
                                    while (i++ < n) {
                                        if (it.hasNext()) {
                                            subscriber.onNext(it.next());
                                        } else {
                                            subscriber.onComplete();
                                            break;
                                        }
                                    }
                                } catch (Exception exception) {
                                    subscriber.onError(exception);
                                }
                            }
                        );

                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("onSubscribe");
                this.subscription = subscription;
                subscription.request(2);
            }

            int bufferSize = 2;

            @Override
            public void onNext(Integer item) {
                System.out.println("onNext" + item);
                System.out.println(" processing");
                if (--bufferSize <= 0) {
                    bufferSize = 2;
                    subscription.request(2);
                }

            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        publisher.subscribe(subscriber);
        executorService.awaitTermination(10, TimeUnit.HOURS);
        executorService.shutdown();
    }
}
