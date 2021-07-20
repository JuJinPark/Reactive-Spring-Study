package com.example.reactivespringstudy.lesson2;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class PubSub {

    public static void main(String[] args) {
        mapOperator();
        mapOperator2();
        mixedOperator();
        sumOperator();
        reduceOperator();
        reduceOperator2();
    }

    private static void mapOperator() {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10)
            .collect(Collectors.toList()));
        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
        mapPub.subscribe(logSub());
    }

    private static void mapOperator2() {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10)
            .collect(Collectors.toList()));
        Publisher<String> mapPub = mapPub(pub, s -> "[" + s + "]");
        mapPub.subscribe(logSub());
    }

    private static void mixedOperator() {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10)
            .collect(Collectors.toList()));
        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
        Publisher<String> mapPub2 = mapPub(mapPub, s -> "[" + s + "]");
        mapPub2.subscribe(logSub());
    }

    private static void sumOperator() {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10)
            .collect(Collectors.toList()));
        Publisher<Integer> sumPub = sumPub(pub);
        sumPub.subscribe(logSub());
    }

    private static void reduceOperator() {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10)
            .collect(Collectors.toList()));
        Publisher<String> reducePub = reducePub(pub, "", (a, b) -> a + "-" + b);
        reducePub.subscribe(logSub());
    }

    private static void reduceOperator2() {
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10)
            .collect(Collectors.toList()));
        Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder(),
            (a, b) -> a.append(b + ","));
        reducePub.subscribe(logSub());
    }

    private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> s) {
                pub.subscribe(new DelegateSub<T, R>(s) {
                    R result = init;

                    @Override
                    public void onNext(T integer) {
                        result = f.apply(result, integer);

                    }

                    @Override
                    public void onComplete() {
                        s.onNext(result);
                        s.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                pub.subscribe(new DelegateSub(s) {
                    int sum = 0;


                    public void onNext(Integer integer) {
                        sum += integer;
                    }

                    @Override
                    public void onComplete() {
                        s.onNext(sum);
                        s.onComplete();
                    }
                });
            }
        };
    }

    private static <T, R> Publisher<R> mapPub(Publisher<T> pub,
        Function<T, R> f) {

        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T, R>(sub) {
                    @Override
                    public void onNext(T integer) {
                        sub.onNext(f.apply(integer));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T integer) {
                System.out.println("onNext:{}" + integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(Iterable<Integer> iter) {
        return new Publisher<>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try {
                            iter.forEach(s -> sub.onNext(s));
                            sub.onComplete();
                        } catch (Throwable t) {
                            sub.onError(t);
                        }

                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}
