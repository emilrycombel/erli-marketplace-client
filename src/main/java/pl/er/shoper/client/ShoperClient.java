package pl.er.shoper.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.api.CompetitionApi;
import pl.er.shoper.client.api.MessageApi;
import pl.er.shoper.client.api.OfferApi;
import pl.er.shoper.client.api.OrderApi;
import pl.er.shoper.client.api.ProductApi;
import pl.er.shoper.client.auth.AuthInterceptor;
import pl.er.shoper.client.auth.AuthService;

public class ShoperClient {

    private final ProductApi products;
    private final OfferApi offers;
    private final OrderApi orders;
    private final MessageApi messages;
    private final CompetitionApi competition;

    public ShoperClient(ShoperClientConfig config) {
        var objectMapper = new ObjectMapper();

        var baseRestClient = RestClient.builder()
                .baseUrl(config.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();

        var authService = new AuthService(baseRestClient, config.username(), config.password(), config.tokenStore());
        var authInterceptor = new AuthInterceptor(authService);

        var restClient = RestClient.builder()
                .baseUrl(config.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory())
                .requestInterceptor(authInterceptor)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                })
                .build();

        this.products = new ProductApi(restClient, objectMapper);
        this.offers = new OfferApi(restClient, objectMapper);
        this.orders = new OrderApi(restClient, objectMapper);
        this.messages = new MessageApi(restClient, objectMapper);
        this.competition = new CompetitionApi(restClient, objectMapper);
    }

    public ProductApi products() {
        return products;
    }

    public OfferApi offers() {
        return offers;
    }

    public OrderApi orders() {
        return orders;
    }

    public MessageApi messages() {
        return messages;
    }

    public CompetitionApi competition() {
        return competition;
    }
}
