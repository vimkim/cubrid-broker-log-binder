raw-logtop:
    bat log_top.q

bind:
    java BrokerLogBinder log_top.q | bat -l sql

bind-out:
    java BrokerLogBinder log_top.q > binded.q

select-8-sed:
    sed -n '23p' binded.q

select-8:
    java PrintNthQuery binded.q 8

select-8-format:
    java PrintNthQuery binded.q 8 | ./sql-formatter | bat -l sql

select-8-format-out:
    java PrintNthQuery binded.q 8 | ./sql-formatter > 8.sql

clean:
    rm 8.sql
    rm binded.q
