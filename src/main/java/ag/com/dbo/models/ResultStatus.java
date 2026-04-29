package ag.com.dbo.models;

public enum ResultStatus {
    Ok(1),
    Failed(2);

    private final int status;
    ResultStatus(int status){
        this.status=status;
    }
    public int getStatus(){
        return this.status;
    }
}
