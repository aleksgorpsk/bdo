package ag.com.dbo;

import java.util.concurrent.*;
public class Test {

    public static void main(String[] args) {
        int poolSize = 5; // Example fixed size

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolSize,                  // corePoolSize
                poolSize,                  // maximumPoolSize
                0L, TimeUnit.MILLISECONDS, // keepAliveTime (0 for fixed pool)
                new SynchronousQueue<Runnable>(), // Queue that holds 0 tasks
                new ThreadPoolExecutor.AbortPolicy() // Reject if more tasks arrive
        );

        // Usage
        try {
            for (int i=0;i<8;i++) {
                executor.execute(() -> {
                    try {
                        System.err.println("sleep:" );
                        Thread.sleep(3000);
                        System.err.println("wake up");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (RejectedExecutionException e) {
            System.err.println("Task rejected: All " + poolSize + " threads are busy.");
        }
        while (executor.getActiveCount()>0){
            try {
                System.err.println("bzzz");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.err.println("shootdown");
        executor.shutdown();
    }
}
