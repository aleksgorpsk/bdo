taskLog = """
    SLF4J: Class path contains multiple SLF4J bindings.
    SLF4J: Found binding in [jar:file:/opt/hive/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
    SLF4J: Found binding in [jar:file:/opt/hadoop-2.7.4/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
    SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
    SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
    Connecting to jdbc:hive2://localhost:10000/
    Connected to: Apache Hive (version 2.3.2)
    Driver: Hive JDBC (version 2.3.2)
    Transaction isolation: TRANSACTION_REPEATABLE_READ
    WARNING: Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
    +---------+--------------------+
    |   cnt   |        mean        |
    +---------+--------------------+
    | 349965  | 65665.00440044004  |
    +---------+--------------------+
    | 22     | 33333        |
    +---------+--------------------+
    1 row selected (1.679 seconds)
    Beeline version 2.3.2 by Apache Hive
    Closing: 0: jdbc:hive2://localhost:10000/
"""

import groovy.json.JsonOutput

def parse(taskLog, vars){
    def start = callLog.indexOf("+-");
    def stop = callLog.lastIndexOf("-+")+2;
    def stable = callLog[start..stop]
    Map rst= [:]
    def header =[]
    def index =0
    stable.eachLine(line   -> {
        if (!line.trim().startsWith("+-")) {
            def row1 =line.split(/\|/)
            def row = []
            row1.each(r-> {
                def rr = r.trim()
                if (rr.length() > 0) {
                    row.add(rr)
                }
            }
            )

            if (index ==0){
                header = row
                header.each { rw ->
                    rst.put(rw,[])
                }
            }else{
                def i= 0
                header.each { rw ->
                    rst.get(rw).add(row[i])
                    i++
                }
            }
            index ++
        }
        }
    )
    Map result = [:]

    result.put(vars.get(resultName,result))
    return JsonOutput.toJson(result)
}
return  parse(taskLog, vars)
