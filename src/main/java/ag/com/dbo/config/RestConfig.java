package ag.com.dbo.config;

import ag.com.dbo.repositories.management.NodeRepository;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;




@Configuration
public class RestConfig {

    private final NodeRepository nodeRepository;

    public RestConfig(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }


    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("etlCache");
    }

    @Bean(name = "scriptRestClient")
    public RestClient scriptRestClient(@Value("${script.url}") String queueBasePath, @Value("${server.port}") String port ){
        return RestClient.builder()
                .baseUrl(queueBasePath+port)
                .build();
    }

    @Bean
    public ObjectMapper jsonCustomizer() {
        return JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
    }

    @Bean(name = "scheduleRestClient")
    public RestClient scheduleRestClient(@Value("${schedule.url}") String queueBasePath, @Value("${server.port}") String port ){
        return RestClient.builder()
                .baseUrl(queueBasePath+port)
                .build();
    }

}
