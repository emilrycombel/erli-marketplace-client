package pl.er.shoper.client.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.*;

class AuthInterceptorTest {

    @Test
    void addsAuthorizationHeaderForNonAuthRequests() throws IOException {
        var authService = mock(AuthService.class);
        when(authService.getValidToken()).thenReturn("test-token-123");

        var interceptor = new AuthInterceptor(authService);

        var request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("https://shop.example.com/webapi/rest/products"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        var execution = mock(ClientHttpRequestExecution.class);
        var response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        verify(authService).getValidToken();
        verify(execution).execute(eq(request), any());
    }

    @Test
    void skipsAuthorizationForAuthEndpoint() throws IOException {
        var authService = mock(AuthService.class);
        var interceptor = new AuthInterceptor(authService);

        var request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("https://shop.example.com/webapi/rest/auth"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        var execution = mock(ClientHttpRequestExecution.class);
        var response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        verify(authService, never()).getValidToken();
    }
}
