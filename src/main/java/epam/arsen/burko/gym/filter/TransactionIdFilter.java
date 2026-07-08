package epam.arsen.burko.gym.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Slf4j
public class TransactionIdFilter extends OncePerRequestFilter {
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        responseWrapper.setHeader(TRANSACTION_ID_HEADER, transactionId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            int status = responseWrapper.getStatus();

            if (status >= 400) {
                log.warn("transactionId={}, method={}, uri={}, status={}, durationMs={}, request={}, response={}",
                        transactionId, request.getMethod(), request.getRequestURI(), status, duration, requestBody, responseBody);
            } else {
                log.info("transactionId={}, method={}, uri={}, status={}, durationMs={}, request={}, response={}",
                        transactionId, request.getMethod(), request.getRequestURI(), status, duration, requestBody, responseBody);
            }

            responseWrapper.copyBodyToResponse();
            MDC.remove(TRANSACTION_ID_MDC_KEY);
        }
    }
}


