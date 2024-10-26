package ml.spring.boot.breaker.service.impl;

import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.service.RemoteService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RemoteServiceImpl implements RemoteService {

    private int id;


    /**
     * 模拟远端服务
     * id < 10  抛出运行时异常
     * == 10 将当前线程停止5秒
     * 其他返回线程name + id
     * @return
     */
    @Override
    public String remoteAPI() {
        String tName = Thread.currentThread().getName();
        log.info("{}: 请求到达远端服务... ..", tName);
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
        return tName+ "--->" + id;
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }
}
