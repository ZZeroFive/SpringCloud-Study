
# Spring Cloud微服务框架学习

## 1. 服务治理
### 1.1 服务注册中心
（1）搭建eureka服务注册中心



## 2. Resilience4j服务容错处理
由于Hystrix已经停止更新，只维护BUG，并且Netflix官网推荐使用Resilience4j作为容错的解决方案。Resilience4j支持重试、降级、熔断、限流、隔离、缓存等功能。
是一个完备的容错框架。
### 2.1 基于SpringBoot的Resilience4J使用
（1）熔断
使用默认配置的断路器进行远程服务调用。
可以发现：异常时返回降级结果；不易确认断路器的状态

使用自定义配置的断路器进行远程服务调用：可以确认断路器的工作方式

（2）异常降级
（3）熔断
（4）超时降级
（5）限流
