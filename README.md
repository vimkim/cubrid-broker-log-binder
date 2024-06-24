# Cubrid Broker Log Utilities

큐브리드 브로커 로그를 더 쉽게 보고 분석할 수 있게 해주는 유틸리티 패키지입니다.

- Broker Log Binder
- Nth Query Printer
- Formatters

위와 같이 구성되어 있습니다.

다음과 같은 기능을 지원합니다:

- [x] 변수 바인딩
- [x] 주석 제거
- [x] 인자 번호에 해당하는 쿼리를 출력
- [x] SQL 포매팅

## Summary

사용법 요약:

```sh
# remove single-line comments
sed 's/--[^\t]*\t/\t/g' log_top.q | sed 's/\/\/[^\t]*\t/\t/g' > log_top_wo_comments.q

# remove multi-line comments and bind parameters
javac BrokerLogBinder.java && java BrokerLogBinder --rm-comments log_top_wo_comments.q > output.sql

# print n-th query from output.sql
javac PrintNthQuery.java && java PrintNthQuery output.sql 3

# using the above utility program, get the formatted output
javac PrintNthQuery.java && java PrintNthQuery output.sql 3 | ./sleek-binary
```

---

### Input

log_top.q 파일

크기 50MB, 20만줄 이상의 대용량 파일도 5초 안에 결과를 얻으실 수 있습니다.

---

### Output

```sql
SELECT
    DISTINCT A.comp_cd,
    A.id_row,
    A.empno,
    B.empno_nm,
    B.lev_ind,
    A.dept_cd,
    TO_CHAR(A.ate_day, 'YYYY-MM-DD') AS ateDay,
    WEEKDAY(A.ate_day) AS ateDayNo,
    A.ate_cd,
    D.ate_nm,
    TO_CHAR(Z.req_rest_sdt, 'YYYY-MM-DD') AS rest_sdt,
    TO_CHAR(Z.req_rest_edt, 'YYYY-MM-DD') AS rest_edt,
    (
        CASE
            WHEN HOUR(Z.req_rest_sdt) < 10 THEN '0' || TO_CHAR(HOUR(Z.req_rest_sdt))
            ELSE TO_CHAR(HOUR(Z.req_rest_sdt))
        END
    ) AS rest_shour,
...
```

---

## BrokerLogBinder

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

---

## PrintNthQuery

```sh
java PrintNthQuery <filename-generated-by-BrokerLogBinder> <query number>
```

BrokerLogBinder를 통해 생성된 파일의 이름과, 쿼리 번호를 인자로 주면, 해당 쿼리를 출력합니다. 

---

## SQL Formatter

- sleek (written in rust): https://github.com/nrempel/sleek
- sql-format (from npm): https://github.com/sql-formatter-org/sql-formatter
- CUBRID fsqlf: https://github.com/CUBRID/fsqlf

Use whichever you prefer. I personally recommend the first one combined with PrintNthQuery.

You have three experimental options.

### Sleek

```sh
cat output.sql | ./sleek-binary > formatted.sql
```

### sql-format

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

### CUBRID fsqlf

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
