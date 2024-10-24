package ml.spring.boot.breaker.service.impl;

import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RemoteServiceImpl implements RemoteService {

    private int id;

    @Override
    public String remoteAPI() {
        System.out.println("i am remote service... ..");
        if (this.id < 10) {
            throw new RuntimeException("id 非法");
        }
        if (this.id == 10) {
            try {
                Thread.sleep(5*1000);
            } catch (Exception e) {
                System.out.println("线程休眠异常!");
                return "exception " + id;
            }
        }
        return "hello " + id;
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }
}
