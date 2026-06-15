
def vars= [failTimeout: 120,
           logToTesultScript: "def stepName=\"branches1\" import groovy.json.JsonOutput\n\ndef parse(vars, stepName){\n def step1=vars.get((stepName))\n def varName = step1.get(\"resultName\")\n    def branch =vars.get((varName))\n    return JsonOutput.toJson(branch>1)\n}\n\nreturn  parse(vars, stepName)",
        stepResultName:"testFile1",
        attemptTimeOut: 12,
        sensorResultName: "sensor1",
        directoryName:"/Users/aleksgor/opt/files"
]

def taskLog="\n" +
        "       2\n" +
        "code result: 0"
import groovy.json.JsonOutput
def parse(vars, taskLog){

    def result = taskLog.find { it?.trim() }

    return JsonOutput.toJson(["result":result>1])
}

return parse(vars, taskLog)
