def listFiles = localResults.get("result")
def listOut = new ArrayList();
def listLines = listFiles.readLines()
for (String record : listLines){
    def words = record.split(" ");
    def www = words-""
    if (www.collect().size() == 9) {
        if (www[0].indexOf("d")== -1 )  {
            listOut.add(record)
        }
    }
}
def filesNumber = listOut.size();
if (filesNumber<2){
    return false;
}else{
    return true
} 