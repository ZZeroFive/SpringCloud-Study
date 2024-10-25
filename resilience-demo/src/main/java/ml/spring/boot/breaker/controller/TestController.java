package ml.spring.boot.breaker.controller;



import lombok.extern.slf4j.Slf4j;
import ml.spring.boot.breaker.service.LocalService;
import ml.spring.boot.breaker.service.RemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
@Slf4j
public class TestController {


    @Autowired
    @Qualifier("circuitBreakImpl")
    private LocalService circuitBreakImpl;

    @Autowired
    @Qualifier("directCallImpl")
    private LocalService directCallImpl;


    @GetMapping("/direct/call/{id}")
    public String directCall(@PathVariable("id") int id) {
        // http://localhost:8080/direct/call/4  10 14
        return directCallImpl.callRemote(id);
    }

    @GetMapping("/circuit/call/{id}")
    public String onlyCircuitCall(@PathVariable("id") int id) {
        // http://localhost:8080/direct/call/4  10 14
        return circuitBreakImpl.callRemote(id);
    }


}
