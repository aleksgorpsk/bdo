package ag.com.dbo.services.loadingService.model;

import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.StepInstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.sql.*;

import java.util.concurrent.Callable;

@Slf4j
public class HiveJdbcTask extends TaskProperties implements Callable<PropData> {


    @Value("${test1jdbc.username}")
    private String jdbsUser;

    @Value("${test1jdbc.password}")
    private String jdbsPassword;

    @Value("${test1jdbc.url}")
    private String jdbsUrl;

    @Value("${test1jdbc.driver}")
    private String jdbsDriver;

    @Value("${test1jdbchive.url}")
    private String hiveUrl;

    @Value("${test1jdbchive.user}")
    private String hiveUser;

    @Value("${test1jdbchive.password}")
    private String hivePassword;


    private StepInstance inn;
    private String out;

    private final  Environment env;
    private final StepInstanceRepository stepInstanceRepository;

    public HiveJdbcTask  (StepInstance data, Environment env, StepInstanceRepository stepInstanceRepository){
        super(env);
        inn= data;
        this.env = env;
        this.stepInstanceRepository = stepInstanceRepository;
    }


    @Override
    public PropData call() throws Exception {

        String jdbsUser = env.getProperty("app-parameters.hive-to-jdbc.jdbc-user","");

        String jdbsPassword = env.getProperty("app-parameters.hive-to-jdbc.jdbc-password","");

        String jdbsUrl = env.getProperty("app-parameters.hive-to-jdbc.jdbc-url","");

        String jdbsDriver = env.getProperty("app-parameters.hive-to-jdbc.driver","");

        String hiveUrl = env.getProperty("app-parameters.hive-to-jdbc.hive-url","");

        String hiveUser = env.getProperty("app-parameters.hive-to-jdbc.hive-user","");

        String hivePassword = env.getProperty("app-parameters.hive-to-jdbc.hive-password","");


        try {

        } catch (Throwable e) {
            e.printStackTrace();
            return new PropData(10, out = inn + " Result");
        }

        return new PropData(0, out = inn + " Result");
    }
}
