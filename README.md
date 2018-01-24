#### 基于springboot+WeblogicMQ实现JMS消息的Queue和Topic示例

#### 运行本示例需要启动Weblogic，并配置队列Queue和主题Topic，相关配置步骤参考http://blog.csdn.net/shuangyidehudie/article/details/7733254

#### 示例演示说明
1. 	分别在三个模块运行mvnw package对模块打包
2.  启动两个MQReader用于模拟应用监听JMS消息（MQReader项目通过设置ClientId的方式将Topic消息进行持久化，即只要MQWriter不宕机，MQReader无论何时启动，都将接收到完整的MQ推送消息，目前ClientID规则为：clientId_serverport，根据实际情况修改MQReader项目的application.properties文件的jms.client）
	
```
java -jar -Dserver.port=7081 MQReader\target\MQReader.jar
java -jar -Dserver.port=7082 MQReader\target\MQReader.jar
```
3.  启动MQWriter用于模拟OSB提供导入采购订单服务，并写入MQ
		
```
java -jar MQWriter\target\MQWriter.jar
```

3.  启动WsClient用于模拟写入一条消息
		
```
java -jar WsClient\target\WsClient.jar
```

#### 示例结果
执行完上述命令后，根目录将生成MQReader-7081.log和MQReader-7082.log，只要执行一次java -jar WsClient\target\WsClient.jar，两个文件便会打印一次订阅的消息。
		