package io.movetodata.config;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestId = httpRequest.getHeader("RequestId");
        String userName = httpRequest.getHeader("Username");

        // Set the RequestId into MDC
        if (requestId != null) {
            MDC.put("RequestId", requestId);
            MDC.put("Username", userName);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove("RequestId");
            MDC.remove("Username");
        }
    }

    @Override
    public void destroy() {
        // Cleanup code, if needed
    }
}
