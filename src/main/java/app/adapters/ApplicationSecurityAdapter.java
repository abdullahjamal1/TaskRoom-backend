package app.adapters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import app.services.UserService;
import app.configs.ApplicationConfig;
import app.filters.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class ApplicationSecurityAdapter extends WebSecurityConfigurerAdapter {

    private static final int TOKEN_VALIDITY_SECONDS = 31536000;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ApplicationConfig config; 

    @Override
    protected void configure(final HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeRequests()
        .antMatchers("/swagger-ui/**").permitAll()
        .antMatchers("/v2/**").permitAll()
        .antMatchers("/swagger-resources/**").permitAll()
        .antMatchers("/webjars/**").permitAll()
        .antMatchers("/sql/**").permitAll()
        .antMatchers("/user/register").permitAll()
        .antMatchers("/user/activate").permitAll()
        .antMatchers("/user/activation-send").permitAll()
        .antMatchers("/user/reset-password").permitAll()
        .antMatchers("/user/reset-password-change").permitAll()
        .antMatchers("/user/autologin").access("hasRole('ROLE_ADMIN')")
        .antMatchers("/user/delete").access("hasRole('ROLE_ADMIN')")
        .antMatchers("/img/**").permitAll()
        .antMatchers("/css/**").permitAll()
        .antMatchers("/js/**").permitAll()
        .antMatchers("/images/**").permitAll()
        .anyRequest().authenticated()
        .and()
        .formLogin().loginPage("/login").failureUrl("/login?error").permitAll()
        .and()
        .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
        .and()
        .rememberMe().key(config.getSecret())
        .tokenValiditySeconds(TOKEN_VALIDITY_SECONDS).and().csrf().disable().headers().frameOptions().disable();
/*
        httpSecurity.csrf().disable()
        .authorizeRequests().antMatchers("/authenticate").permitAll()
        .antMatchers("/swagger-ui/**").permitAll()
        .antMatchers("/v2/**").permitAll()
        .antMatchers("/swagger-resources/**").permitAll()
        .antMatchers("/webjars/**").permitAll()
        .antMatchers("/sql/**").permitAll()
        .antMatchers("/user/register").permitAll()
        .antMatchers("/user/activate").permitAll()
        .antMatchers("/user/activation-send").permitAll()
        .antMatchers("/user/reset-password").permitAll()
        .antMatchers("/user/reset-password-change").permitAll()
        .antMatchers("/user/autologin").access("hasRole('ROLE_ADMIN')")
        .antMatchers("/user/delete").access("hasRole('ROLE_ADMIN')")
        .antMatchers("/img/**").permitAll()
        .antMatchers("/css/**").permitAll()
        .antMatchers("/js/**").permitAll()
        .antMatchers("/images/**").permitAll()
        .anyRequest().authenticated()
        .and()
        .formLogin().loginPage("/login").failureUrl("/login?error").permitAll()
        .and()
        .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
        .and().
                exceptionHandling().and().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        */
    }
/*
    @Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService);
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
*/
    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
    }
}
