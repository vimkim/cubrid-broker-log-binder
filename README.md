# cubrid-broker-log-binder

## How to use:

```sh
javac BrokerLogBinder.java && java BrokerLogBinder <input.log>
```

### Expected Input Format:

CUBRID broker log file

```txt
03/21 18:51:36.467 (0) CLIENT IP 127.0.0.1
03/21 18:51:36.473 (0) connect db cgkdb user public url jdbc:cubrid:localhost:53300:cgkdb:public::?

03/21 18:51:36.476 (1) prepare 0 select * from foo where id = ?
03/21 18:51:36.477 (1) prepare srv_h_id 1
03/21 18:51:36.491 (1) execute srv_h_id 1 select * from foo where id = ?
03/21 18:51:36.491 (1) bind 1 : INT 1
03/21 18:51:36.529 (1) execute 0 tuple 1 time 0.055
03/21 18:51:36.529 (0) auto_commit
03/21 18:51:36.529 (0) auto_commit 0
03/21 18:51:36.529 (0) *** elapsed time 0.052
```

Make an example file named 'example.log' with the contents above.

### Result:

```txt
$ javac BrokerLogBinder.class && java BrokerLogBinder example.log
select * from foo where id = 1
```

---

### Reference

- Related Jira issue: http://jira.cubrid.com/browse/RND-2175

- Original source: [cubrid manager source code](https://github.com/CUBRID/cubrid-manager/blob/develop/com.cubrid.common.ui/src/com/cubrid/common/ui/spi/util/CommonUITool.java#L1247)
