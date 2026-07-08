import epam.arsen.burko.gym.filter.TransactionIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Test
    void doFilter_WithExistingTransactionId_ReusesHeaderAndClearsMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/trainings");
        request.addHeader(TransactionIdFilter.TRANSACTION_ID_HEADER, "tx-123");
        request.setContent("{\"a\":1}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            HttpServletResponse wrapperResponse = invocation.getArgument(1);
            wrapperResponse.setStatus(200);
            wrapperResponse.getWriter().write("ok");
            assertEquals("tx-123", MDC.get("transactionId"));
            return null;
        }).when(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        filter.doFilter(request, response, chain);

        assertEquals("tx-123", response.getHeader(TransactionIdFilter.TRANSACTION_ID_HEADER));
        assertEquals("ok", response.getContentAsString());
        assertNull(MDC.get("transactionId"));
    }

    @Test
    void doFilter_WithoutTransactionId_GeneratesValidUuidHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainees");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            HttpServletResponse wrapperResponse = invocation.getArgument(1);
            wrapperResponse.setStatus(500);
            wrapperResponse.getWriter().write("error");
            return null;
        }).when(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        filter.doFilter(request, response, chain);

        String transactionId = response.getHeader(TransactionIdFilter.TRANSACTION_ID_HEADER);
        assertNotNull(transactionId);
        UUID.fromString(transactionId);
        assertEquals("error", response.getContentAsString());
        assertNull(MDC.get("transactionId"));
    }
}


