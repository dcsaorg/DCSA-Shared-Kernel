package org.dcsa.skernel.controller.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ApiVersionHeaderFilter extends OncePerRequestFilter {
  @Value("${dcsa.specification.version:N/A}")
  private String apiVersion;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    response.setHeader("API-Version", apiVersion);
    filterChain.doFilter(request, response);
  }
}
