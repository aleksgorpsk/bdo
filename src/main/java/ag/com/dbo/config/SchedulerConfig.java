package ag.com.dbo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Define maximum concurrent threads allowed
        scheduler.setPoolSize(20);

        // Prefix for thread names to identify them in logs
        scheduler.setThreadNamePrefix("MyScheduler-Thread-");

        // Ensure graceful shutdown of remaining tasks on stop
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);

        // Silently skip tasks that are canceled after they are submitted
        scheduler.setRemoveOnCancelPolicy(true);

        return scheduler;
    }
}
