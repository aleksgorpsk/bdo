taskLog="""
       2
 code result: 0
"""
vars = [directoryName:"/Users/aleksgor/opt/files", resultName:"testFile"]

import groovy.json.JsonOutput

def parse(taskLog, vars){
    def varName=vars.get("resultName")
    def firstValid = taskLog.readLines().find { it?.trim() }
    def result = [(varName):firstValid.trim()]
    return JsonOutput.toJson(result)
}
return   parse(taskLog,vars)
