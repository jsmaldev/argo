
package de.jsmal.web;

import java.time.Instant;
import java.util.stream.Collectors;

import de.jsmal.core.ServletEngine;
import de.jsmal.core.searchObject.SearchQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.Long.parseLong;

@Slf4j
@RestController
public class TokenController {

	@Autowired
	JwtEncoder encoder;

	@Autowired
	ServletEngine servletEngine;

	@Value("${security.expiry}")
	String expiryValue;

	@PostMapping("/token")
	public String token(Authentication authentication) {
	log.info("LOG: get function in /token");
		Instant now = Instant.now();

		long expiry = parseLong(this.expiryValue); //36000L - minutes for valid cert

		// @formatter:off
		String scope = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(" "));
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("self")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(expiry))
				.subject(authentication.getName())
				.claim("scope", scope)
				.build();
		// @formatter:on
		return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	@PostMapping(
			value = "/search", consumes = "application/json", produces = "application/json")
	public String search(@RequestBody SearchQuery query) {
		//return query.toString();
		return this.servletEngine.search(query);
	}

	@GetMapping(
			value = "/dbdict", produces = "application/json")
	public String dbdict() {
		//return query.toString();
		return this.servletEngine.dbdict();
	}

}
