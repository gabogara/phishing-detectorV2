package com.gabriel.phishingagent.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiKeyFilterTest {

    private static final String API_KEY = "test-key";

    private final ApiKeyFilter filter = new ApiKeyFilter(API_KEY);

    @Test
    void letsRequestWithValidKeyThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/analyses");
        request.addHeader(ApiKeyFilter.HEADER_NAME, API_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull(); // the request reached the next step
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejectsRequestWithoutKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/analyses");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull(); // the request never reached the controller
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getContentAsString()).contains("Missing or invalid API key");
    }

    @Test
    void rejectsRequestWithWrongKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/analyses/1");
        request.addHeader(ApiKeyFilter.HEADER_NAME, "wrong-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void leavesHealthCheckPublic() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void refusesToStartWithoutKey() {
        assertThatThrownBy(() -> new ApiKeyFilter("  "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_API_KEY");
    }
}