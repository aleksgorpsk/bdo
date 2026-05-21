package ag.com.dbo.services.queue.model;

import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import ag.com.dbo.services.queue.TaskProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.util.concurrent.Callable;

@Slf4j
public class HiveToJdbcTask extends TaskProperties implements Callable<PropData> {


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


    private QueueStorage task;
    private String out;

    private final  Environment env;
    private final QueueStorageRepository queueStorageRepository;

    public HiveToJdbcTask(QueueStorage data, Environment env, QueueStorageRepository queueStorageRepository){
        super(env);
        task= data;
        this.env = env;
        this.queueStorageRepository = queueStorageRepository;
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
            return new PropData(10,  new QueueStorage(), ExceptionUtils.getStackTrace(e) + System.lineSeparator()+ e.getMessage());
        }

        return new PropData(0, new QueueStorage(), "tests");
    }
}
