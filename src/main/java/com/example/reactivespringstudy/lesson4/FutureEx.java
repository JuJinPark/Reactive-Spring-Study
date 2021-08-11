package com.example.reactivespringstudy.lesson4;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FutureEx {

    interface SuccessCallBack {

        void onSuccess(String result);
    }

    interface ExceptionCallBack {

        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {

        private SuccessCallBack sc;

        private ExceptionCallBack ec;

        public CallbackFutureTask(Callable<String> callable, SuccessCallBack sc,
            ExceptionCallBack ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            try {
                sc.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                ec.onError(e.getCause());
            }
        }

    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
        runByNormalFuture(es);
        runByFutureTaskWithoutCallback(es);
        runByFutureTaskWithCallBack(es);
        runBySuccessAndExceptionCallBack(es);

    }

    private static void runByNormalFuture(ExecutorService es)
        throws InterruptedException, ExecutionException {

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        });

        System.out.println("f.done() = " + f.isDone());
        Thread.sleep(2100);
        log.info("Exit");
        System.out.println("f.done() = " + f.isDone());
        System.out.println("f.get() = " + f.get());
        es.shutdown();
    }

    private static void runByFutureTaskWithoutCallback(ExecutorService es)
        throws InterruptedException, ExecutionException {
        FutureTask<String> f = new FutureTask<>(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        });

        es.execute(f);

        log.info(String.valueOf(f.isDone()));
        Thread.sleep(2000);
        log.info("Exit");
        log.info(String.valueOf(f.isDone()));
        log.info(f.get());
    }

    private static void runByFutureTaskWithCallBack(ExecutorService es) {
        FutureTask<String> f = new FutureTask<>(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        }) {
            @Override
            public void done() {
                try {
                    log.info(get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        es.execute(f);
        es.shutdown();
        log.info("EXIT");
    }

    private static void runBySuccessAndExceptionCallBack(ExecutorService es) {
        CallbackFutureTask f = new CallbackFutureTask(() -> {
            if (1 == 1) {
                throw new RuntimeException("Async ERROR!!");
            }
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        }, x -> System.out.println("result:" + x),
            e -> System.out.println("error" + e.getMessage()));

        es.execute(f);
        es.shutdown();
    }
}
