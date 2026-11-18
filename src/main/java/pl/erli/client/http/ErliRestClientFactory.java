package pl.erli.client.http;

import pl.erli.client.auth.TokenProvider;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

public class ErliRestClientFactory {

    private ErliRestClientFactory() {}

    public static RestClient create(String baseUrl, TokenProvider tokenProvider,
                                     int maxRetries, long initialDelayMs,
                                     Duration connectTimeout, Duration readTimeout) {
        var httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(tokenProvider.getToken());
                    return execution.execute(request, body);
                })
                .requestInterceptor(new RateLimitInterceptor(maxRetries, initialDelayMs))
                .defaultStatusHandler(new ErliErrorHandler())
                .build();
    }
}
