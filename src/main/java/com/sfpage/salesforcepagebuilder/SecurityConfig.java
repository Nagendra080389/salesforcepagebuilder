package com.sfpage.salesforcepagebuilder;

import com.sfpage.canvas.CanvasAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http/* Allow the app to show in a frame */
                .headers().frameOptions().disable()
                .and()
                /* always create a server session */
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .and()
                /*
                 * Define the URLs that can be accesses without authentication
                 */
                .authorizeRequests()
                .antMatchers("/",
                        "/sfdcauth/**")
                .permitAll()
                /* Require all others to be authenticated */
                .anyRequest().authenticated()
                .and()
                /* Add the filter that turns JWT into authentication */
                .addFilter(new CanvasAuthorizationFilter(this.authenticationManager()))
                /*
                 * allow direct access to the POST form for Canvas use without a
                 * _csrd token
                 */
                .csrf()
                .ignoringAntMatchers("/sfdcauth/**");
    }
}
