package ag.com.dbo.config;

import ag.com.dbo.models.management.Node;
import ag.com.dbo.repositories.management.NodeRepository;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Configuration
public class RestConfig {

    private final NodeRepository nodeRepository;

    public RestConfig(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Bean("NodeClients")
    public Map<Integer, RestClient> nodeClients(){
        List<Node> nodes= nodeRepository.findAll().stream().filter(Node::getActive).toList();
        Map <Integer, RestClient> result = new HashMap<>(nodes.size());
        for(Node node: nodes){
            RestClient client = RestClient.builder()
                    .baseUrl(node.getHost())
                    .build();
            result.put(node.getId(), client);
        }
        return result;

    }

    @Bean("queueRestClient")
    public RestClient queueRestClient(@Value("${queue.url}") String queueBasePath ){
        return RestClient.builder()
                .baseUrl(queueBasePath)
                .build();
    }

    @Bean(name = "scriptRestClient")
    public RestClient scriptRestClient(@Value("${script.url}") String queueBasePath ){
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
