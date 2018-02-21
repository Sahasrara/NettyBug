package com.sahasrara.frown;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simulate the load we see.  ~800 threads futzing about.
 */
public class BackgroundNoise {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(800, new ThreadFactoryBuilder()
            .setNameFormat("Noisy-Worker-%d")
            .setDaemon(true)
            .build());

    public static void makeSomeNoise() {
        EXECUTOR_SERVICE.execute(new NoiseConductor());
    }

    private static class NoiseConductor implements Runnable {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5);
                    EXECUTOR_SERVICE.execute(new NoiseMaker());
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static class NoiseMaker implements Runnable {
        private static final Random RANDOM = new Random();
        public void run() {
            for (int i = 0; i < 100000; i++) {
                RANDOM.nextDouble();
            }
        }
    }
}
