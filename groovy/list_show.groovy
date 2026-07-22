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
    return  Map.of("result",listOut )
}
return  parse(stepName, vars, etlResults, localResults)
