package com.biometricsystem.api.security;
import com.biometricsystem.api.security.jwt.JwtRequestFilter;
import com.biometricsystem.api.service.EmployeeDetailsService;
import com.biometricsystem.api.employee.EmployeeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private EmployeeDetailsService employeeDetailsService;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /*@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }*/

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(employeeDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
            csrf().disable().
            authorizeRequests().
                antMatchers("/model","/employeepage","/employeeslist").hasAnyAuthority(EmployeeType.ADMIN.getName()).
                antMatchers("/","/personalpage").hasAnyAuthority(EmployeeType.STANDARD.getName(),EmployeeType.ADMIN.getName()).
                antMatchers(HttpMethod.POST,"/login").permitAll().
                anyRequest().authenticated().
                and().
            sessionManagement().
                sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().
            formLogin().
                disable().
                /*loginPage("/login.html").
                loginProcessingUrl("/perform_login")
                defaultSuccessUrl("/",true).
                failureUrl("/login.html?error=true").
                and().
            logout().
                permitAll().*/
                /*logoutUrl("/logout").*/
            exceptionHandling().
                accessDeniedHandler(new AccessDeniedExceptionHandler()).
                and().
            addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

}