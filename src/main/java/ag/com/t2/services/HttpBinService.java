package ag.com.t2.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


@Slf4j
@Service()
public class HttpBinService implements InitializingBean {

    @Value("${app.restUrl}")
    private String restUrl ;
    private final RestTemplate restTemplate;

    public HttpBinService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public void afterPropertiesSet() {
        log.info("!! afterPropertiesSet !");

    };


    @PostConstruct
    public void init(){
        log.info("!! Init !");
    }

    public String getJson(){
        log.info("restUrl:"+restUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>("",headers);

        return restTemplate.exchange(
                restUrl, HttpMethod.GET, entity, String.class).getBody();
    }
}
