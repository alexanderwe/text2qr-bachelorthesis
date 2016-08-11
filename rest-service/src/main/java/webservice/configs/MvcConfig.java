package webservice.configs;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.sql.DriverManager;

/**
 * Configuration for the webservice
 * Sets the views (link the urls to the html templates. Templates are using bootstrap framework and JQuery)
 */
@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    /**
     * Add the mapping for the html sites
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/apidoc").setViewName("apidoc");
        registry.addViewController("/translationssite").setViewName("translationssite");
        registry.addViewController("/endpoint").setViewName("endpoint");
        registry.addViewController("/about").setViewName("about");
        registry.addViewController("/register").setViewName("register");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/account").setViewName("account");
        registry.addViewController("/register/activate/activation_success").setViewName("activation_success");
        registry.addViewController("/register/activate/sendmail").setViewName("send_mail_again");
        registry.addViewController("/forgotpassword").setViewName("forgotpassword");
        registry.addViewController("/changepassword").setViewName("changepassword");
        registry.addViewController("/error").setViewName("error");
        registry.addViewController("/privacy").setViewName("privacy");
        registry.addViewController("/language_codes").setViewName("language_codes");
        registry.addViewController("/status").setViewName("status");
        registry.addViewController("/admin/dashboard").setViewName("dashboard");
        registry.addViewController("/download").setViewName("download");
        registry.addViewController("/contact").setViewName("contact");
        registry.addViewController("/403").setViewName("403");
    }


    /**
     * Add resource handler
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    /**
     * Creates the embedded servlet container
     * @return
     */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
        return tomcat;
    }

    /**
     * Initiate the redirection from http to https
     * @return
     */
    private Connector initiateHttpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }


}
