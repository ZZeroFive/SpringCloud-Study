package ml.spring.cloud.user.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/userController")
@Slf4j
public class UserController {

    @GetMapping("/{id}")
    public String sayHello(@PathVariable(name = "id") int id) {
        log.info("===> user input: {} <===", id);
        if (id == 0) {
            throw new RuntimeException("id 为0");
        } else if (id == 1) {
            try {
                log.info("当前线程休眠3秒");
                Thread.sleep(3*1000);
            } catch (Exception e) {
                log.error("当前线程休眠3秒.");
            }
        }
        return id+"";
    }
}
