package rg.dcsa.skernel.http.headers.forwarded;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Slf4j
@Configuration
public class ForwardedHeadersConfiguration {

  @Value("${dcsa.supportProxyHeaders:true}")
  private boolean supportProxyHeaders;

  @Bean
  public FilterRegistrationBean<Filter> forwardedHeaderFilter() {
    FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
    if (!supportProxyHeaders) {
      log.info("Disabled support for Proxy headers (X-Forwarded-*, etc.). Use dcsa.supportProxyHeader=[true|false] to change this");
      bean.setFilter(new DoNothingFilter());
    } else {
      log.info("Enabled support for Proxy headers (X-Forwarded-*, etc.) from *any* IP. Use dcsa.supportProxyHeader=[true|false] to change this");
      bean.setFilter(new ForwardedHeaderFilter());
      bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
      bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
    return bean;
  }

  private static class DoNothingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
      chain.doFilter(request, response);
    }
  }
}
