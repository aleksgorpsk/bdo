import groovy.json.JsonOutput

def listOut = List.of("Branch1")

def branch = Map.of("branch", listOut)
return JsonOutput.toJson(listOut)
return branch



