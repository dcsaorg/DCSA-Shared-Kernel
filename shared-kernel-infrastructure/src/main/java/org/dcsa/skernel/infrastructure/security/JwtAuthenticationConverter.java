package org.dcsa.skernel.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class JwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {
  private static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";

  private static final Collection<String> WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES =
      Arrays.asList("scope", "scp", "authorities"); // added authorities

  protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    return this.getScopes(jwt).stream()
        .map(authority -> SCOPE_AUTHORITY_PREFIX + authority)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }

  private Collection<String> getScopes(Jwt jwt) {
    Collection<String> authorities = new ArrayList<>();
    // add to collection instead of returning early
    for (String attributeName : WELL_KNOWN_SCOPE_ATTRIBUTE_NAMES) {
      Object scopes = jwt.getClaims().get(attributeName);
      if (scopes instanceof String scopesAsStr) {
        if (StringUtils.hasText(scopesAsStr)) {
          authorities.addAll(Arrays.asList(scopesAsStr.split(" ")));
        }
      } else if (scopes instanceof Collection) {
        authorities.addAll((Collection<String>) scopes);
      }
    }

    return authorities;
  }

  @Override
  public JwtAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> scopes =
        getScopes(jwt).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    authorities.addAll(scopes);
    return new JwtAuthenticationToken(jwt, authorities);
  }
}
