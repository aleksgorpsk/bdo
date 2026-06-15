

// vars = [directoryName:"/Users/aleksgor/opt/files", resultName:"testFile","testFile":"2"]
//vars=        {"branches1":{"directoryName":"/Users/aleksgor/opt/files","resultName":"testFile","testFile":"2"},"branch2":{"msg":"branch1"},"branch1":{"msg":"branch2"}}
vars=  [directoryName:"/Users/aleksgor/opt/files",resultName:"testFile"]
results= [branches1:[result:2]]
def stepName="branches1"
//results=        [branches1:[directoryName:"/Users/aleksgor/opt/files",resultName:"testFile",testFile:"2"],branch2:[msg:"branch1"],branch1:[msg:"branch2"]]


import groovy.json.JsonOutput


def parse(stepName,vars, results){
    def step1=results.get((stepName))
    def branch = step1.get("result")
    def result = []
    if (branch<21){
        result= ["branch1","branch11"]
    }else{
        result = ["branch2","branch12"]
    }
    println result

    return JsonOutput.toJson(result)
}

return  parse(stepName, vars, results)
