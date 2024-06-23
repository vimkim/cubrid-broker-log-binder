# cubrid-broker-log-binder

![image](https://github.com/vimkim/cubrid-broker-log-binder/assets/18080546/952e71be-e8a5-4598-9620-a4679804670a)

## How to use

```sh
java BrokerLogBinder [--rm-comments] <input.log>
```

Requirements: JRE 8 or above

### Options

```text

--rm-comments
    removes csql comments while processing the input.log.

```

### 1. Prepare a cubrid broker log file as an input file

example.log:

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

### 2. Observe the binding results

```sh
java BrokerLogBinder --rm-comments example.log > output.sql
```

output.sql:

```txt
select * from foo where id = 1
```

### 3. Format the output file using the provided formatter binary (Experimental)

You have three experimental options.

#### Option 1. Sleek

```sh
cat output.sql | ./sleek-binary > formatted.sql
```

#### Option 2. sql-format

```sh
./sql-formatter-executable output.sql > formatted.sql
```

formatted.sql:

```txt
select
  *
from
  foo
where
  id = 1;
```

#### Option 3. CUBRID fsqlf

Use a CUBRID csql-flavored formatter from <https://github.com/CUBRID/fsqlf>.

---

## How to build

```sh
javac BrokerLogBinder.java
```

---

### Reference

- Related Jira issue: <http://jira.cubrid.com/browse/RND-2175>

- Original source: [cubrid manager source code](https://github.com/CUBRID/cubrid-manager/blob/develop/com.cubrid.common.ui/src/com/cubrid/common/ui/spi/util/CommonUITool.java#L1247)
