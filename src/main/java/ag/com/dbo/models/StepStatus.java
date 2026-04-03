package ag.com.dbo.models;

public enum StepStatus {
    InProgres(1),
    Success(3),
    Failed(4),
    Queue(5);

    private final int status;
    StepStatus(int status){
        this.status=status;
    }
    public int getStatus(){
        return this.status;
    }
}
