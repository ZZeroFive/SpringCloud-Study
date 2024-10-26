package ml.spring.boot.breaker.service;

import java.util.concurrent.ExecutionException;

public interface LocalService {

    String callRemote(int id) throws Exception;
}
