package app.adapters;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import app.configs.ApplicationConfig;
import app.filters.JwtRequestFilter;
import app.services.UserService;

@Configuration
@EnableWebSecurity
public class ApplicationSecurityAdapter extends WebSecurityConfigurerAdapter {

    private static final int TOKEN_VALIDITY_SECONDS = 31536000;

    @Autowired
    private UserDetailsService myUserDetailsService;
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.
        csrf().disable()
        .cors().configurationSource(corsConfigurationSource()).and().
				authorizeRequests().
                antMatchers("/auth/**").permitAll().
                antMatchers(HttpMethod.GET, "/**").permitAll().
                antMatchers("/auth/login").permitAll().
						anyRequest().authenticated().and().
						exceptionHandling().and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

	}

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE"));
        configuration.setAllowCredentials(true);
        //the below three lines will add the relevant CORS response headers
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    // @Override
    // protected void configure(final HttpSecurity httpSecurity) throws Exception {

    //     httpSecurity.cors().and().authorizeRequests()
    //     .antMatchers("/**").permitAll()
    //     .antMatchers("/swagger-ui/**").permitAll()
    //     .antMatchers("/v2/**").permitAll()
    //     .antMatchers("/swagger-resources/**").permitAll()
    //     .antMatchers("/webjars/**").permitAll()
    //     .antMatchers("/sql/**").permitAll()
    //     .antMatchers("/register").permitAll()
    //     .antMatchers("/activate").permitAll()
    //     .antMatchers("/activation-send").permitAll()
    //     .antMatchers("/reset-password").permitAll()
    //     .antMatchers("/reset-password-change").permitAll()
    //     .antMatchers("/autologin").access("hasRole('ROLE_ADMIN')")
    //     .antMatchers("/delete").access("hasRole('ROLE_ADMIN')")
    //     .antMatchers("/img/**").permitAll()
    //     .antMatchers("/css/**").permitAll()
    //     .antMatchers("/js/**").permitAll()
    //     .antMatchers("/images/**").permitAll()
    //     .anyRequest().authenticated()
    //     .and()
    //     .formLogin().loginPage("/login").failureUrl("/login?error").permitAll()
    //     .and()
    //     .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
    //     .and()
    //     .rememberMe().key(config.getSecret())
    //     .tokenValiditySeconds(TOKEN_VALIDITY_SECONDS).and().csrf().disable().headers().frameOptions().disable();

    // }

    // @Override
    // public void configure(final AuthenticationManagerBuilder auth) throws Exception {

    //     auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
    // }

    // @Bean
    // CorsConfigurationSource corsConfigurationSource() {
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
    //     return source;
    // }
}
