package ag.com.dbo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;



@Configuration
public class RestConfig {


    @Bean
    public RestClient restClient(@Value("${queue.url}") String queueBasePath ){
        return RestClient.builder()
                .baseUrl(queueBasePath)
                .build();
    }

}
