
def stepName="sensor1"

/*
def vars=[sensorScript:"GROOVY:",
            sensorTest1:1,
          attemptTimeOut:12,
            failTimeout: 120,
          logToTesultScript: "import groovy.json.JsonOutput\n\ndef parse(taskLog, vars){\n    def firstValid = taskLog.readLines().find { it?.trim() }\n    def result = [\"result\":firstValid.trim()]\n    return JsonOutput.toJson(result)\n}\nreturn   parse(taskLog,vars)",
        stepResultName:  "testFile1",
        directoryName :"/Users/aleksgor/opt/files"]
*/
def results=[sensor1:[
        directoryName: "/Users/aleksgor/opt/files",
        stepResultName: "testFile1",
        attemptTimeOut:  12,
        failTimeout:120,
        logToTesultScript: "import groovy.json.JsonOutput\n\ndef parse(taskLog, vars){\n    def firstValid = taskLog.readLines().find { it?.trim() }\n    def result = [\"result\":firstValid.trim()]\n    return JsonOutput.toJson(result)\n}\nreturn   parse(taskLog,vars)",
        sensorScript:  "GROOVY:sensorTest1:1",
        result: "{\"result\":\"1\"}"
]]
// "\ntotal 1992\n-rw-r--r--@ 1 aleksgor  staff  1017544 Feb 19 12:50 over10k\ndrwxr-xr-x@ 6 aleksgor  staff      192 May  8 10:49 target\ncode result: 0"

//        {"directoryName":"/Users/aleksgor/opt/data","result":"\ntotal 1992\n-rw-r--r--@ 1 aleksgor  staff  1017544 Feb 19 12:50 over10k\ndrwxr-xr-x@ 6 aleksgor  staff      192 May  8 10:49 target\ncode result: 0"}
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
def localResults = "{\"directoryName\":\"/Users/aleksgor/opt/data\",\"result\":\"\\ntotal 1992\\n-rw-r--r--@ 1 aleksgor  staff  1017544 Feb 19 12:50 over10k\\ndrwxr-xr-x@ 6 aleksgor  staff      192 May  8 10:49 target\\ncode result: 0\"}"

import groovy.json.JsonOutput

def parse(stepName, vars, etlResults, localResults){

    def step1=localResults.get(("result"))
    def listLines = step1.readLines()
    def listOut = new ArrayList()
    for (String record : listLines){
        String[] words = record.split(" ");
        def www = words-""

        if (www.collect().size() == 9) {
            if (www[0].indexOf("d")== -1 ) {
                listOut.add(record)
            }
        }

    }
    deMap.of("result",listOut )
    return JsonOutput.toJson(listOut)
}

return  parse(stepName, vars, etlResults, localResults)
