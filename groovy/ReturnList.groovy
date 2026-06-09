import groovy.json.JsonOutput


// input: list
//"vars", vars): Map String to vars
//binding.setVariable("variants" - list of String
def parse(list){

    println list
    def result = list[0]
    println result
    return JsonOutput.toJson(result)
}
print parse(["apple","banana","orange"])
