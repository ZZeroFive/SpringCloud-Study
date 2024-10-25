package ml.spring.boot.breaker.service.impl;

import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.service.LocalService;
import ml.spring.boot.breaker.service.RemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DirectCallImpl implements LocalService {

    @Autowired
    private RemoteService remoteService;

    /**
     * 直接调用
     * @return
     */
    public String callRemote(int id) {
        remoteService.setId(id);

        return remoteService.remoteAPI();
    }

}
