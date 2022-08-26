package de.jsmal;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import de.jsmal.core.engine.model.exeptions.InitDObjectIsNotFoundException;
import de.jsmal.core.engine.model.source.data.DataSource;
import de.jsmal.core.engine.model.source.data.MySqlDataSource;
import de.jsmal.core.engine.model.source.dictionary.DictionarySource;
import de.jsmal.core.engine.model.source.dictionary.InstanceDictionary;
import de.jsmal.core.engine.model.source.dictionary.MySqlDictionarySource;
import de.jsmal.core.engine.model.utils.Dictionary;
import de.jsmal.security.LocalUserDetailsService;
import de.jsmal.security.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class SpringBeansConfig {

	@Value("${jwt.public.key}")
	RSAPublicKey key;

	@Value("${jwt.private.key}")
	RSAPrivateKey priv;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		http
				.authorizeHttpRequests((authorize) -> authorize
						.anyRequest().authenticated()
				)
				.csrf((csrf) -> csrf.ignoringAntMatchers("/token"))
				.httpBasic(Customizer.withDefaults())
				.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
				.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling((exceptions) -> exceptions
						.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
						.accessDeniedHandler(new BearerTokenAccessDeniedHandler())
				);
		// @formatter:on
		return http.build();
	}

	// -------- worked example -----------
//	@Bean
//	UserDetailsService users() {
//		return new InMemoryUserDetailsManager(
//			User.withUsername("user")
//				.password("{noop}password")
//				.authorities("app")
//				.build()
//		);
//	}
	// -------- worked example END -----------

	@Bean
	UserDetailsService users() {
		return new LocalUserDetailsService();
	}


	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.key).build();
	}

	@Bean
	JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
		JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}

	//-------------------------------------------
	@Bean
	DataSource dataSource(
			@Value("${spring.datasource.url}") String datasource_url,
			@Value("${spring.datasource.username}") String datasource_username,
			@Value("${spring.datasource.password}") String datasource_password
	) {

		try {
			log.info("===== Data Source is initialised ================");
			log.info("=================================================");
			return new MySqlDataSource().initDataSource();
		} catch (InitDObjectIsNotFoundException e) {
			log.error("ERROR by initialization Data Source");
			e.printStackTrace();
			return null;
		}
	}

	@Bean
	InstanceDictionary instanceDictionary (
			@Value("${spring.datasource.url}") String datasource_url,
			@Value("${spring.datasource.username}") String datasource_username,
			@Value("${spring.datasource.password}") String datasource_password
	) {
		InstanceDictionary ret_instanceDictionary;
		//------DB PARAMETERS-------------
		try {
			ArrayList<String> parameters = new ArrayList<>(Arrays.asList(
					datasource_url,
					datasource_username,
					datasource_password
			));
			//------INIT DICTIONARY FROM DB----------
			DictionarySource dictionarySource = new MySqlDictionarySource().initDictionarySource(parameters);

			ret_instanceDictionary = Dictionary.getDictionaryFromMySqlSource(dictionarySource);
			log.info("=================================================");
			log.info("===== Instance Dictionary is initialised ========");
			ret_instanceDictionary.getDbIndexMap();
			log.info("===== Indexes are initialised ===================");
			return ret_instanceDictionary;

		} catch (InitDObjectIsNotFoundException e) {
			log.error("ERROR by initialization DB parameters");
			e.printStackTrace();
			return null;
		}
	}

}
