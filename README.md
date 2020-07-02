本项目基于开源项目 [kafka-eagle]("https://github.com/smartloli/kafka-eagle") 改造，个人兴趣改造

---
变化：
- springmvc切换为springboot
- 安全框架切换为spring security
- kafka资源采用apache commons pool池化
- 完成Thymeleaf前端UI改造
- 新增Zookeeper 集群模拟启动脚本start-zookeeper-cluster.sh，方便测试
- 新增Kafka集群模拟启动脚本 start-kafka-cluster.sh(默认开启JMX功能，方便监控),方便测试
- 公共资源(KafkaZkClient、AdminClient、KafkaConsumer、KafkaProducer、JMXConnector)完成模板化改造，方便资源统一管理
- 统一JMXConnector获取接口，方便JMX资源统一管理

---
期望:
- 新增Kafka主题消息实时更新
- AngularJS框架替换JQuery