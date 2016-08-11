package webservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

/**
 * Created by alexanderweiss on 14.03.16.
 * Class for configuring the authentication database and userdetailservice
 */
@Configuration
public class AuthenticationProviderConfig {

    /**
     * Set up our user database
     * @return
     */
    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://127.0.0.1:8889/users");
        ds.setUsername("root");
        ds.setPassword("root");
        return ds;
    }

    /**
     * Make the userdatabase available in the userDetailService
     * @return
     */
    @Bean(name="userDetailsService")
    public UserDetailsService userDetailsService(){
        JdbcDaoImpl jdbcDaoImpl = new JdbcDaoImpl();
        jdbcDaoImpl.setDataSource(dataSource());
        jdbcDaoImpl.setUsersByUsernameQuery("select username,password,enabled from users where username=?");
        jdbcDaoImpl.setAuthoritiesByUsernameQuery("select b.username, a.authority from authorities a, users b where b.username=? and a.username = b.username");
        return jdbcDaoImpl;
    }
}
