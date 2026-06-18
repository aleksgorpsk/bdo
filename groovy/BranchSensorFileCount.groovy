

// vars = [directoryName:"/Users/aleksgor/opt/files", resultName:"testFile","testFile":"2"]
//vars=        {"branches1":{"directoryName":"/Users/aleksgor/opt/files","resultName":"testFile","testFile":"2"},"branch2":{"msg":"branch1"},"branch1":{"msg":"branch2"}}
vars=        [branches1:[directoryName:"/Users/aleksgor/opt/files",resultName:"testFile",testFile:"2"],branch2:[msg:"branch1"],branch1:[msg:"branch2"]]
def stepName="branches1"


import groovy.json.JsonOutput

def parse(stepName,vars, results){
    def step1=vars.get((stepName))
    def result = step1.get("result")
    return JsonOutput.toJson(result>1)
}

return  parse(stepName,vars, results)
