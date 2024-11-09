package ml.spring.boot.breaker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ExternAPIService {

    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;


    public String callApi(Integer id) {
        if (id < 10) {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                log.info("线程休眠失败!");
            }
            return "sleep " + id;
        } else if (id == 10) {
             throw new RuntimeException("id非法");
        } else {
            return "normal " + id;
        }
    }
}
