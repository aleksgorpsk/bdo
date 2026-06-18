
def stepName="sensor1"

def vars=[sensorScript:"GROOVY:",
            sensorTest1:1,
          attemptTimeOut:12,
            failTimeout: 120,
          logToTesultScript: "import groovy.json.JsonOutput\n\ndef parse(taskLog, vars){\n    def firstValid = taskLog.readLines().find { it?.trim() }\n    def result = [\"result\":firstValid.trim()]\n    return JsonOutput.toJson(result)\n}\nreturn   parse(taskLog,vars)",
        stepResultName:  "testFile1",
        directoryName :"/Users/aleksgor/opt/files"]

def results=[sensor1:[
        directoryName: "/Users/aleksgor/opt/files",
        stepResultName: "testFile1",
        attemptTimeOut:  12,
        failTimeout:120,
        logToTesultScript: "import groovy.json.JsonOutput\n\ndef parse(taskLog, vars){\n    def firstValid = taskLog.readLines().find { it?.trim() }\n    def result = [\"result\":firstValid.trim()]\n    return JsonOutput.toJson(result)\n}\nreturn   parse(taskLog,vars)",
        sensorScript:  "GROOVY:sensorTest1:1",
        result: "{\"result\":\"1\"}"
]]

import groovy.json.JsonOutput

def parse(stepName,vars, results){
    def step1=results.get((stepName))
    def result = step1.get("result")
    return JsonOutput.toJson(result>1)
}
def x= parse(stepName,vars, results)
println(x)
return  parse(stepName,vars, results)
