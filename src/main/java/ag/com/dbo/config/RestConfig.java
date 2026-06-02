package ag.com.dbo.config;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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

    @Bean
    public ObjectMapper jsonCustomizer() {
        return JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
    }
}
