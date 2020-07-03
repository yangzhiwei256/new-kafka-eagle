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

---
期望:
- 新增Kafka主题消息实时更新
- AngularJS框架替换JQuery
- 邮件告警完整支持

---
Kafka工具：
```
创建主题：kafka-topics.sh --create --bootstrap-server debian:9091,debian:9092,debian:9093 --replication-factor 1 --partitions 1 --topic SAMPLE
主题列表：kafka-topics.sh --list --bootstrap-server debian:9091,debian:9092,debian:9093
生产消息：kafka-console-producer.sh --bootstrap-server debian:9091,debian:9092,debian:9093 --topic SAMPLE
消费消息：kafka-console-consumer.sh --bootstrap-server debian:9091,debian:9092,debian:9093 --topic SAMPLE --from-beginning

生产消息压测：kafka-producer-perf-test.sh --topic SAMPLE --num-records 10000000 --record-size 100 --throughput 5000 --producer-props bootstrap.servers=debian:9091,debian:9092,debian:9093
消费消息压测：kafka-consumer-perf-test.sh --broker-list debian:9091,debian:9092,debian:9093 --topic SAMPLE --fetch-size 1048576 --messages 10000000 --threads 1
```