package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.filter.FilterBuilder;
import pl.er.shoper.client.model.PagedResponse;
import pl.er.shoper.client.pagination.PagedStream;

import java.util.stream.Stream;

abstract class BaseApi<T> {

    private static final Logger log = LogManager.getLogger(BaseApi.class);

    protected final RestClient restClient;
    protected final ObjectMapper objectMapper;
    protected final String resourcePath;
    private final JavaType pagedResponseType;

    protected BaseApi(RestClient restClient, ObjectMapper objectMapper, String resourcePath, Class<T> elementType) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.resourcePath = resourcePath;
        this.pagedResponseType = objectMapper.getTypeFactory()
                .constructParametricType(PagedResponse.class, elementType);
    }

    public Stream<T> findAll() {
        return findAll(FilterBuilder.create());
    }

    public Stream<T> findAll(FilterBuilder filters) {
        log.debug("Fetching {} with filters", resourcePath);
        return PagedStream.create(page -> fetchPage(page, filters));
    }

    protected PagedResponse<T> fetchPage(int page, FilterBuilder filters) {
        log.debug("Fetching {} page {}", resourcePath, page);
        var queryParams = filters.toQueryParams();

        var json = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(resourcePath);
                    uriBuilder.queryParam("page", page);
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(json, pagedResponseType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize paged response from " + resourcePath, e);
        }
    }
}
