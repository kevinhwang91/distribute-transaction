### 此项目主要实现分布式柔性事务可靠消息最终一致性和TCC。
- - -

### 框架简略图
![框架图](http://www.jacksonpuppy.com/img/框架图.png)  


### 项目运行环境
- mysql 5.6+
- rabbitmq
- redis
- zookeeper

- - -

### 业务介绍
业务模型是基金分销商申购确认，类似支付机构通知调用平台支付成功。  
* 字段简单介绍：  
order_number：订单号，agency_fee：基金分销商获取的费用，unit：基金申购确认的份额
  
* 模块介绍：  
demo里面有4个模块分别是confirm、order、bill和holdings。  
confirm是简单模拟基金公司确认数据，插入原始数据并推送到order。  
order是用户下单模块，更新确认后的状态。  
bill是对账模块，表结构与order是一对一关系。  
holdings是用户资产模块，表结构与order是一对多。  

* 业务流程：  
confirm收到数据推送到order，order、bill和holdings在一个事务中存储对应的数据。

- - -

### 快速运行
- 安装项目所需要的项目运行环境并启动，建议在linux的环境下
- 导入项目sql目录的sql
- 更改所有ip地址127.0.0.1为运行环境的ip地址
- 分别启动confirm、order、bill、holdings这四个模块
- 默认开启了isTestedTcc，在tcc每个环节都会有概率抛出异常，运行order模块的rollBack和sendConfirm
- 运行一段时间后用以下sql查看结果
```sql
select * from 
(select count(*) received_order_count from `order`.`order` where status = 3 ) order_count,
(select count(*) added_bill_count from bill.bill where status = 2 ) bill_count,
(select count(*) added_holdings_count from holdings.holdings_resource where status = 2 ) holdings_resource_count,
(select sum( unit ) sum_holdings_unit, sum( freeze_unit ) sum_freeze_unit from holdings.holdings ) holdings;
```
![sql结果](http://www.jacksonpuppy.com/img/sql结果.png)  
看到上图代表成功  

- - -

### 详细请看
[我的个人笔记](http://www.jacksonpuppy.com/2018-09-07/distributed-transaction.html) 