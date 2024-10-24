package ml.spring.boot.breaker.service;

public interface RemoteService {
    // 假定一个远程调用接口
    String remoteAPI();

    void setId(int id);
}
