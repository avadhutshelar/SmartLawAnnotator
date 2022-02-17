package in.edu.rvce.slanno;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	DataSource dataSource;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests()
			.antMatchers("/systemSettings").hasRole("ADMIN")
			.antMatchers("/legalActs").hasRole("ADMIN")
			.antMatchers("/users").hasRole("ADMIN")
			.antMatchers("/project/create").hasAnyRole("ADMIN")
			.antMatchers("/project/view/all").hasAnyRole("ADMIN")
			.antMatchers("/project/**/import/**").hasAnyRole("ADMIN")
			.antMatchers("/project/**/annotators/**").hasAnyRole("ADMIN")
			.antMatchers("/project/**/preprocess/**").hasAnyRole("ADMIN", "ANNOTATOR")
			.antMatchers("/project/**/veiwJson/**").hasAnyRole("ADMIN", "ANNOTATOR")
			.antMatchers("/project/**/annotate/**").hasAnyRole("ADMIN", "ANNOTATOR")
			.antMatchers("/").permitAll().and().formLogin()
			.loginPage("/login").permitAll().and().logout().permitAll();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource);
	}

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}