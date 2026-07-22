def localResults=new HashMap()
localResults.put("result","        total 8\n" +
"-rw-r--r--@ 1 aleksgor  staff   5 Jun  8 13:12 1\n" +
"-rw-r--r--@ 1 aleksgor  staff   0 Jun 18 12:03 2\n" +
"drwxr-xr-x@ 2 aleksgor  staff  64 Jul 20 16:19 d1\n")

def listFiles = localResults.get("result")
def listOut = new ArrayList();

def listLines = listFiles.readLines()
for (String record : listLines){
    def words = record.split(" ");
    def www = words-""
    if (www.collect().size() == 9) {
        if (www[0].indexOf("d")== -1 ) {
            listOut.add(record)
        }
    }
}

def filesNumber = listOut.size();
def result =  filesNumber>1
return result



