package ag.com.dbo.models.script;

public enum ScriptType {
    Common,  // calculate date, run....,
    Branch, // calculate branches only
    Sensor,
    ShellCommand, // calculate Sensor result
    PreExecution;
}
