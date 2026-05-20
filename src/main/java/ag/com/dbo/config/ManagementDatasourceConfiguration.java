package ag.com.dbo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static ag.com.dbo.config.Utils.checkProps;

@Configuration
@EnableTransactionManagement
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories(
        basePackages = "ag.com.dbo.repositories.management",
        entityManagerFactoryRef = "managementEntityManagerFactory",
        transactionManagerRef = "managementTransactionManager"
)
public class ManagementDatasourceConfiguration {
    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "managementDataSource")
    public DataSource managementDataSource() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.management.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource.management.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.management.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.management.password"));


        return dataSource;
    }

    @Primary
    @Bean(name = "managementEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean managementEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("managementDataSource") DataSource managementDataSource) {

        Map<String,Object> property =  new HashMap<>();
        checkProps(property, "hibernate.hbm2ddl.auto.management");
        checkProps(property, "hibernate.dialect.management");
        return builder
                .dataSource(managementDataSource())
                .packages("ag.com.dbo.models.management")
                .persistenceUnit("management")
                .properties(property)
                .build();
    }


    @Primary
    @Bean(name = "managementTransactionManager")
    public PlatformTransactionManager managementTransactionManager(
            @Qualifier("managementEntityManagerFactory") EntityManagerFactory managementEntityManagerFactory) {
        return new JpaTransactionManager(managementEntityManagerFactory);
    }
}
