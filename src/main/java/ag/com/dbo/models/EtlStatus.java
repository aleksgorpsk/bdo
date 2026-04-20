package ag.com.dbo.models;

public enum EtlStatus {
    Ready(1),
    AutoStart(2),
    ManualStart(3);

    private final int status;
    EtlStatus(int status){
        this.status=status;
    }
    public int getStatus(){
        return this.status;
    }
}
