

// vars = [directoryName:"/Users/aleksgor/opt/files", resultName:"testFile","testFile":"2"]
//vars=        {"branches1":{"directoryName":"/Users/aleksgor/opt/files","resultName":"testFile","testFile":"2"},"branch2":{"msg":"branch1"},"branch1":{"msg":"branch2"}}
vars=        [branches1:[directoryName:"/Users/aleksgor/opt/files",resultName:"testFile",testFile:"2"],branch2:[msg:"branch1"],branch1:[msg:"branch2"]]
def stepName="branches1"

import groovy.json.JsonOutput

def parse(vars, stepName){
    def step1=vars.get((stepName))
    def varName = step1.get("resultName")
    def branch =vars.get((varName))
    return JsonOutput.toJson(branch>1)
}

return  parse(vars, stepName)
