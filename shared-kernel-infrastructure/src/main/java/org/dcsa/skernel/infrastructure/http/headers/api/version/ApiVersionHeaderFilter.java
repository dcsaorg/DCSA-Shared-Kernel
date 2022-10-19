package org.dcsa.skernel.infrastructure.http.headers.api.version;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dcsa.skernel.infrastructure.http.headers.api.version.DcsaSpecificationConfiguration.CONTEXT_PREFIX_PATTERN;
import static org.dcsa.skernel.infrastructure.http.headers.api.version.DcsaSpecificationConfiguration.UNOFFICIAL;

@RequiredArgsConstructor
@Component
public class ApiVersionHeaderFilter extends OncePerRequestFilter {

  private final DcsaSpecificationConfiguration dcsaSpecificationConfigration;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    String specVersion = null;
    String pathPrefix = null;
    if (requestURI.startsWith(request.getContextPath() + "/actuator")) {
      // The actuators are unofficial APIs - we explicitly tag them as such because
      // they do not match the DCSA version prefix (unless the context path forces
      // them to do so), which risks rejecting requests to the health check when
      // the "by-prefix" versioning is used.
      specVersion = UNOFFICIAL;
    } else if (requestURI.toLowerCase().contains("/unofficial/")) {
      // If the path is /unofficial/, then we know it does not have a valid version
      // and we can just shorten the process here.  This also exempts unversioned
      // URLs from having a specification version in the "by-prefix" case making
      // that less of a hassle.
      specVersion = UNOFFICIAL;
    } else {
      Matcher m = CONTEXT_PREFIX_PATTERN.matcher(requestURI);
      if (m.matches()) {
        pathPrefix = m.group(1);
        specVersion = dcsaSpecificationConfigration.getSpecificationVersion(pathPrefix);
      }
    }
    if (specVersion != null) {
      response.setHeader("API-Version", specVersion);
    } else if (dcsaSpecificationConfigration.isRejectUnversionedContexts()) {
      response.setStatus(404);
      // Here to catch missing specifications, so it is sufficient as far as error goes.
      String errorMessage = "The context path \"" + request.getRequestURI()
        + "\" does not have a API specification version but is declared as using \"per prefix\""
        + " DCSA specification rules."
        + " If this path should host a DCSA compliant API, please check the configuration"
        + " (dcsa.specification) to ensure the context is properly declared.";
      if (pathPrefix != null) {
        errorMessage += " The prefix to configure would be \"" + pathPrefix + "\"";
      }
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().println(errorMessage);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
