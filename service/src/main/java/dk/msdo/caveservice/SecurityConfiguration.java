package dk.msdo.caveservice;

/*
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // @formatter:off

        auth.inMemoryAuthentication()
                .withUser("ris")
                .password("{noop}Kamel")
                .roles("USER");

        auth.inMemoryAuthentication()
                .withUser("ave")
                .password("{noop}Uld")
                .roles("USER");

        auth.inMemoryAuthentication()
                .withUser("phg")
                .password("{noop}Underbukser")
                .roles("USER");


        // @formatter:on
    }
    // Authorization : Role -> Access
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.httpBasic() // it indicate basic authentication is requires
                .and()
                .authorizeRequests()
                .antMatchers("/v2/room/*").permitAll()
                .antMatchers("/actuator/health").hasRole("USER")
                .antMatchers("/actuator/info").hasRole("USER")
                .anyRequest().authenticated(); // it's indicate all request will be secure

        http.csrf()
                .disable();
    }
}
*/