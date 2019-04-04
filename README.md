# 分布式柔性事务可靠消息最终一致性和TCC

实现分布式事务中柔性事务的可靠消息最终一致性和TCC，用模拟方法测试demo，校验最终一致性。

## 框架简略图
![框架图](http://www.jacksonpuppy.com/img/框架图.png)


## 项目运行环境
1. 自行部署环境
- Mysql 5.6+
- RabbitMQ 3.2+
- Redis 3.2+
- Zookeeper 3.2+
- Maven/IDE include Maven 3

2. Docker环境（推荐）
- Maven 3
- docker-compose 3.6+

>debug的时候可以采用混合环境，即中间件采用Docker部署，confirm、order、bill和holdings服务在IDE调试，请自行修改docker-compose.yml，注释上面提及的服务和改中间件的映射端口。
---

## 业务介绍

业务模型是基金分销商申购确认，类似支付机构通知平台支付成功，平台确认支付成功的操作。

### 模块介绍：

demo里面有4个服务分别是confirm、order、bill和holdings。

confirm是简单模拟基金公司确认数据，插入原始数据并推送到order。

order是用户下单模块，更新确认后的状态。

bill是对账模块，表结构bill.order_number与order.order_number是一对一关系。

holdings是用户资产模块，表结构holdings.account_number与order.order_number是多对一关系。

### 字段简单介绍：

- order_number：订单号
- agency_fee：基金分销商获取的费用
- account_number：支付用户的帐号
- unit：基金申购确认的份额
- freeze_unit：处于事务中冻结的份额
- order.status: 订单确认状态Paid(1),Receiving(2),Received(3)
- bill.status：对账确认状态Pending(1),Confirm(2)
- holdings_resource.status：用户资产单个订单确认状态Pending(1),Confirm(2)
- TCC_TRANSACTION_xxx.status：处于事务中资源状态Trying(1), Confirming(2), Cancelling(3);

### 业务流程：

confirm收到数据推送到order，order、bill和holdings在一个分布式事务中存储对应的数据。

这里通过模拟数据请求到confirm模块，查看order、bill和holdings的数据弱一致性结果。

- 模拟并发1000个订单确认，1个用户有10个订单，即100个不同的用户。
- 每一笔订单100unit，1unit产生1agency_fee。
- confirm通过RabbitMQ可靠消息把数据推送到order。
- 在order、bill和holdings事务中，均有机率抛出异常进行模拟。
- 事务进行中，TCC_TRANSACTION_xxx中均有数据产生，事务结束则删除数据。
- status逻辑状态do try->insert status=trying->do confirm/cancel->update status=Confirming/Cancelling->delete row。

#### 最终结果
- bill总数1000agency_fee，单笔记录status均为Confirm(2)。
- holdings每一用户资产会有1000unit，holdings_resource上status均为Confirm(2)。
- order的status均为Received(3)。
- TCC_TRANSACTION_xxx均无数据。
- RabbitMq队列上无数据。

## 快速运行测速

### 运行服务
1. 自行部署环境
- 安装项目所需要的项目运行环境并启动，建议在linux的环境下。
- 运行demo-dubbo路径下的import_sql.sh导入mysql所需要的数据。
- 分别启动confirm、bill、holdings、order这四个模块。

2. Docker环境
- 在项目根路径下执行mvn package 或者 mvn install。
- 在demo-dubbo路径下的docker-compose up。

### 运行测试
1. 自行部署环境

http Get请求到[http://127.0.0.1:9001/mock/sendConfirm](http://127.0.0.1:9001/mock/sendConfirm)，过程大约90s。

测试完毕可以http Get请求到[http://127.0.0.1:9001/mock/rollback](http://127.0.0.1:9001/mock/rollback)恢复初始数据。
>*Note:Mysql的port是3306，RabbitMq是5672和15672，Redis是6379，Zookeeper分别是2181-2183

2. Docker环境

http Get请求到[http://127.0.0.1:49001/mock/sendConfirm](http://127.0.0.1:49001/mock/sendConfirm)，过程大约90s。

测试完毕可以http Get请求到[http://127.0.0.1:49001/mock/rollback](http://127.0.0.1:49001/mock/rollback)恢复初始数据。
>*Note:Mysql的port是43306，RabbitMq是45672和55672，Redis是46379，Zookeeper分别是42181-42183，详情请查看docker-compose.yml

#### 通过以下sql查看TCC资源管理RM的数据
```sql
select count(*),DOMAIN, status from ORDER.TCC_TRANSACTION_ORDER where status = 1 group by DOMAIN 
union select count(*),DOMAIN, status from ORDER.TCC_TRANSACTION_ORDER where status = 2 group by DOMAIN 
union select count(*),DOMAIN, status from ORDER.TCC_TRANSACTION_ORDER where status = 3 group by DOMAIN 
union select count(*),DOMAIN, status from BILL.TCC_TRANSACTION_BILL where status = 1 group by DOMAIN 
union select count(*),DOMAIN, status from BILL.TCC_TRANSACTION_BILL where status = 2 group by DOMAIN 
union select count(*),DOMAIN, status from BILL.TCC_TRANSACTION_BILL where status = 3 group by DOMAIN
union select count(*),DOMAIN, status from HOLDINGS.TCC_TRANSACTION_HOLDINGS where status = 1 group by DOMAIN
union select count(*),DOMAIN, status from HOLDINGS.TCC_TRANSACTION_HOLDINGS where status = 2 group by DOMAIN
union select count(*),DOMAIN, status from HOLDINGS.TCC_TRANSACTION_HOLDINGS where status = 3 group by DOMAIN;
```

#### 通过以下sql查看参与TCC事务应用AP的数据
```sql
select * from 
(select count(*) received_order_count from `ORDER`.`order` where status = 3 ) order_count,
(select count(*) added_bill_count from BILL.bill where status = 2 ) bill_count,
(select count(*) added_holdings_count from HOLDINGS.holdings_resource where status = 2 ) holdings_resource_count,
(select sum( unit ) sum_holdings_unit, sum( freeze_unit ) sum_freeze_unit from HOLDINGS.holdings ) holdings;
```

#### 通过以下shell命令(依赖httpie、jq和column命令)或者RabbitMq管理后台页面查看队列数据
1. 自行部署环境
```shell
http http://127.0.0.1:15672/api/queues -a kevin:kevin -b | jq -r '[\"NAME\",\"READY\",\"UNACK\",\"TOTAL\"],(.[]|[.name,.messages_ready,.messages_unacknowledged,.messages])|@tsv' | column -t
```
2. Docker环境
```shell
http http://127.0.0.1:55672/api/queues -a kevin:kevin -b | jq -r '[\"NAME\",\"READY\",\"UNACK\",\"TOTAL\"],(.[]|[.name,.messages_ready,.messages_unacknowledged,.messages])|@tsv' | column -t
```

#### Docker环境下执行过程shell演示（Linux）
- 依赖watch、httpie、jq和column命令。
- 依赖mycli（python模块）的Mysql命令行客户端。
[![asciicast](https://asciinema.org/a/238746.svg)](https://asciinema.org/a/238746)

## 分析

### 可靠性消息

普通的MQ消息为什么不可靠？

- TCP在传输层上是可靠的，通过不断retry或者reset保证可靠，但应用层却不可能保持这种规则，导致应用层面不可靠。
- TCP是流操作，在应用层需要从流解析包，这导致解析过程或者处理相关业务宕机/异常均会造成TCP认为发送成功，但包却没有正确处理。

#### 如何做到消息的可靠？

- 发送高可靠是通过应用层面上做日志记录和重试保证，demo里的日志是Mysql。RabbitMq通过Advanced Message Queuing Protocol(AMQP)回复发送端ack即认为消息正确传达到RabbitMQ，更新日志避免重新发送，否则按照规定规则重新发送日志信息。
- RabbitMQ高可靠是通过持久化和镜像队列保证（这里为了方便没做镜像队列配置），因RabbitMQ转发过程可能会宕机或者异常。若开启了持久化则会在回复发送端ack之前就会把消息持久化，RabbitMQ从异常状态重启后仍然不丢失已经ack过的消息。
- 接受端有可能会收到相同的消息，要保证幂等防止重复消费，且完成正确完成消费后要通过AMQP回复RabbitMQ ack信息。


#### 具体业务做法如下：

##### 发送端

1. 在一个事务当作持久化业务相关字段和中间态信息，在事务成功后发送信息到MQ；
2. 收到MQ的confirm ack后则把持久化的信息从中间态改成完成状态；
3. 定时查询持久化的信息是否在中间态，如是则重发。

##### 接收端

1. 发送端定时可能会重发中间态的信息，接收端必然要做幂等操作；
2. 当接收端对应的业务完成后，ack RabbitMQ，否则nack让信息返回到RabbitMQ；
3. 若接受端业务失败或者异常后把信息发送到RabbitMQ延时，延时利用死信队列和转发队列实现；
4. 延时重试仍然失败或者异常多次则需要人工介入。

>高可靠和高性能是不可得兼，选择了高可靠必然会降低性能，这里可以把日志Mysql换成Redis等Nosql提高吞吐量，虽然可靠性会降低，但RabbitMQ和Nosql同时出现异常概率是非常小的。

>若对高吞吐量有要求，建议把RabbitMq换成Kafka，缺点是需要自行设计延时队列等功能。

#### 可靠消息泳道图如下：

![可靠消息](http://www.jacksonpuppy.com/img/可靠消息.png)

### TCC

TCC和JTA都是是2PC分布式事务处理，而JTA是针对资源（数据源），TCC是针对服务。按现在微服务发展势态，JTA应用场景是很小的。

TCC由Try、Confirm和Cancel业务构成。

- Try：锁定业务资源；
- Confirm：事务完成则确认提交；
- Cancel：事务失败则回滚；

这里TCC是由Dubbo和Spring共同构成，内有一个定时补偿系统。主要逻辑如下：

#### Try

- 主事务处理事务前会持久化事务上下文，设置事务上下文状态为Trying；
- 主事务内调用从事务前会更新到事务上下文；
- 远程调用从事务。

#### Confirm

- 依次根据事务上下文调用主事务以及远程调用从事务的Confirm方法；
- 更新事务上下文状态为Confirming；
- 删除事务上下文。

#### Cancel

- 依次根据事务上下文调用主事务以及远程调用从事务的Cancel方法。
- 更新事务上下文状态为Canceling；
- 删除事务上下文。

#### 定时补偿

- 一旦在Try阶段成功/失败，必然会执行Confirm/Cancel方法；
- Try阶段过程中失败了，会有超时机制最终把Trying变为Canceling，调用事务上下文的Cancel方法。


#### TCC框架的时序图如下：

![Tcc时序图](http://www.jacksonpuppy.com/img/tcc.png)

#### 业务操作

1. Confirm和Cancle方法一定要做幂等，有两个原因：
    1. 定时补偿会有可能重试；
    2. 假设主事务状态在Confirming，多个从业务里面，若一个失败了，则会利用定时补偿重试所有从业务Confirm方法。
2. 每一次Try都是需要锁定资源的，需要唯一标识号传达到Confirm或Cancel方法，如UUID或者时间戳（demo里面是时间戳，集群可能会存在时间偏移）。因TCC的事务是2PC，所以会存在并发竞争调用Confirm和Cancel方法问题。

>*Note：若没有锁住Try，当主事务回滚，准备调用Cancel方法。此时业务重复调用，在主事务Try成功后，准备调用Confirm，这样事务的Confirm和Cancel就会存在竞争关系导致数据不一致。

在demo里面的Holdings模块里面，多个order.order_number对应一个holdings.account_number，这里运用了数据库的乐观锁，需要把事务级别降到READ_COMMITTED(RC)以下，否则乐观锁失效。

>运用乐观锁的原因是为了提高悲观锁的并发，但依然无法支撑高并发。高并发下不建议使用任何TCC模型。

### 幂等

查询是天然幂等的，删除对于单row作用一样，所以也是幂等，这里只讨论添加，更新。

#### Try

- order上是利用Redis分布式锁防止并发，修改状态。业务处理完成后解锁。
- holding和bill上则是通过添加order_number唯一索引做幂等并锁住资源。

#### Confirm和Cancel
- 更新Try业务锁定的状态和时间，因只和Try阶段有关，故是幂等。（状态机幂等）

>总结：TCC分布式编写业务非常复杂，坑非常多，若实时性要求不高不建议使用TCC，而使用可靠消息，缺点就是实时性差。若采用TCC则需要对参与事务的所有Try锁住唯一资源，并且在Confirm和Cancel做好幂等。


## 参考项目

- [tcc-transaction](https://github.com/changmingxie/tcc-transaction): tcc-transaction是TCC型事务java实现

## 授权许可

本项目采用 MIT 开源授权许可证。
