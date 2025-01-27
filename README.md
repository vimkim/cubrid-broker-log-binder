
## DEPRECATION WARNING
> **DEPRECATION NOTICE**  
> 이 프로젝트는 더이상 유지보수되지 않습니다.  
> **더 빠르고, 강력하며, Rust로 새로 작성된 [cubrid-logtopbind-rs](https://github.com/vimkim/cubrid-logtopbind-rs)를 사용해 주세요.**  
> 이 새로운 버전은 내장 SQLite 지원으로 훨씬 다양한 분석을 손쉽게 수행할 수 있으며, 대용량 로그 처리에서도 높은 성능을 보장합니다.

---

## 왜 `cubrid-logtopbind-rs` 로 이주해야 하나요?

1. **Rust로 작성**  
   - Rust는 **네이티브 바이너리**를 생성합니다. 이는 실행 파일이 바로 운영체제에서 동작할 수 있음을 의미하며, **추가 런타임 환경(JRE)**이 필요하지 않습니다.  
     - Java는 JVM(Java Virtual Machine) 위에서 실행되기 때문에, 반드시 JRE가 사전에 설치되어 있어야 합니다. 이는 배포 과정에서 추가적인 설치 작업이 필요하며, 환경 설정 문제로 인해 호환성 이슈가 발생할 수 있습니다.  
     - Rust 바이너리는 이런 제약에서 자유로우며, 빌드된 실행 파일을 어디서나 바로 실행할 수 있습니다.
   - **더 적은 메모리 사용**  
     - JVM은 Garbage Collector와 같은 메모리 관리 시스템을 포함하고 있어 추가적인 메모리 오버헤드가 발생합니다. 반면, Rust는 **컴파일 시점에서 메모리 안정성을 보장**하며, 런타임에 별도의 관리 비용이 들지 않습니다.
   - **성능 최적화**  
     - Rust는 C/C++에 버금가는 최적화된 네이티브 성능을 제공합니다. JVM이 Java 바이트코드를 네이티브로 변환하여 실행하는 과정과 비교하면, Rust는 이미 **최적화된 네이티브 코드로 컴파일**되기 때문에 추가적인 성능 저하가 없습니다.
   - **환경 독립적**  
     - Java 애플리케이션은 종종 JRE 버전 차이(예: Java 8, Java 11)로 인해 호환성 문제가 발생하지만, Rust 바이너리는 **환경에 종속되지 않고 완벽히 독립적**입니다. 따라서 특정 OS나 라이브러리 요구 사항 없이 실행할 수 있습니다.
   - **간편한 배포**  
     - Rust 프로그램은 추가 의존성 없이 **단일 실행 파일**로 제공됩니다. Java의 경우, `.jar` 파일과 함께 JVM 환경을 준비해야 하며, 복잡한 배포 스크립트를 요구하는 경우가 많습니다.

   - 이러한 특성 덕분에, Rust 기반의 `cubrid-logtopbind-rs`는 **설치가 간편하고, 성능이 뛰어나며, 어디서든 실행 가능한** 최고의 대안이 됩니다.

2. **내장 SQLite 데이터베이스**  
   - 로그 분석 결과가 SQLite DB에 자동으로 저장됩니다.  
   - 단순한 텍스트 출력뿐만 아니라, `sqlite3` 혹은 내장 툴(`sqlite-rs`)을 통해 **쿼리를 직접 날려** 원하는 모든 분석을 자유롭게 할 수 있습니다.

3. **변수 바인딩 & SQL 포매팅**  
   - 기존 Java 버전보다 더 정밀하게 바인딩 정보를 추출하고, SQL 문장을 보기 좋게 포매팅합니다.  
   - Rust 생태계의 다양한 라이브러리(예: `regex`, `sqlformat`, `serde_json` 등)를 활용해 에러 처리가 견고합니다.

4. **명령어·사용법이 간단**  
   - `cargo build --release`로 빌드 후, `./target/release/logtopbind path/to/broker.log` 형태로 바로 실행 가능합니다.  
   - SQLite 파일(`queries.db`)이 자동 생성되며, 이를 통해 후속 분석 및 보고서 작성이 더욱 간편해집니다.

5. **높은 확장성**  
   - Rust로 작성된 만큼, 필요한 기능을 직접 추가·변경하기 용이합니다.  
   - 내부 구조가 모듈화되어 있어, 파싱 로직이나 포매터 등을 교체하거나 확장하는 작업이 수월합니다.

---

## 간단 예시

다음은 `broker.log` 파일을 변환해 SQLite DB로 만드는 예시입니다:

```bash
# 1. 소스 다운로드 및 빌드 (바이너리를 바로 wget으로 다운로드하여 사용하실 수도 있습니다.)
git clone https://github.com/vimkim/cubrid-logtopbind-rs.git
cd cubrid-logtopbind-rs
cargo build --release

# 2. 로그 파일을 SQLite DB에 변환
./target/release/logtopbind broker.log
# 실행 후, queries.db 라는 파일이 생성됩니다.

# 3. logtopprint 유틸리티로 특정 쿼리만 확인
./target/release/logtopprint --query-no 3
# 또는
./target/release/logtopprint -q 3
```

생성된 `queries.db`를 통해, 원하는 SQL 데이터를 마음껏 조회할 수 있습니다:

```bash
sqlite3 queries.db
# 혹은
./target/release/sqlite-rs queries.db
```

데이터 스키마 예시는 다음과 같습니다:

```sql
CREATE TABLE IF NOT EXISTS logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    query_no TEXT NOT NULL,
    filename TEXT NOT NULL,
    original_query TEXT NOT NULL,
    replaced_query TEXT,
    bind_vars JSON NOT NULL
);
```

위 테이블의 `replaced_query` 열을 조회하면 바인딩 변수가 반영된 최종 SQL을 한 눈에 파악할 수 있습니다.

---

## 결론

기존 Java 버전(`BrokerLogBinder`, `PrintNthQuery` 등)은 더 이상 유지보수되지 않습니다. 대규모 로그 처리 성능, 분석 편의성, 커뮤니티 확장성 모두에서 **Rust 기반의 `cubrid-logtopbind-rs`**가 훨씬 뛰어난 대안입니다.  

새로운 레포지토리로 넘어오셔서, 기존보다 **빠르고 정확하며 강력해진** CUBRID 브로커 로그 분석을 직접 체험해 보세요!  

---

# Cubrid Broker Log Utilities

## DEPRECATION WARNING
## DEPRECATION WARNING
## DEPRECATION WARNING

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

![image](https://github.com/vimkim/cubrid-broker-log-binder/assets/18080546/99395e8a-12ff-47bb-9f21-369798c61f10)

위 이미지와 같이 bind 변수 1을 ?의 자리에 자동으로 넣어주고 포매팅합니다.

## Summary

사용법 요약:

```sh
# remove single-line comments
./remove-sigleline-comments/remove-singleline-comments < log_top.q > log_top_wo_comments.q

# The above operation is equivalent to using the following sed commands:
# $ sed 's/--[^\t]*\t/\t/g' log_top.q | sed 's/\/\/[^\t]*\t/\t/g' > log_top_wo_comments.q

# remove multi-line comments and bind parameters
javac BrokerLogBinder.java && java BrokerLogBinder --rm-comments log_top_wo_comments.q > output.sql

# Compile and run PrintNthQuery to extract the 3rd SQL query from output.sql
# Usage: javac PrintNthQuery.java && java PrintNthQuery output.sql 3
javac PrintNthQuery.java && java PrintNthQuery output.sql 3

# Note:
# Using formatters like 'sleek' or 'sqlformat' directly on output.sql, which contains multiple SQL statements,
# can be resource-intensive and time-consuming.
# It is recommended to use the PrintNthQuery utility to extract and format a specific SQL statement.
# This approach minimizes memory usage and processing time.

# Example:
# Extract and format the 3rd SQL query from output.sql using the PrintNthQuery utility and sleek formatter
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
