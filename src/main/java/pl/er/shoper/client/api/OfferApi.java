package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.model.offer.CreateOfferRequest;
import pl.er.shoper.client.model.offer.Offer;
import pl.er.shoper.client.model.offer.OfferPriceUpdate;
import pl.er.shoper.client.model.offer.OfferStockUpdate;
import pl.er.shoper.client.model.offer.UpdateOfferRequest;

public class OfferApi extends BaseApi<Offer> {

    private static final Logger log = LogManager.getLogger(OfferApi.class);
    private static final String RESOURCE_PATH = "/webapi/rest/offers";

    public OfferApi(RestClient restClient, ObjectMapper objectMapper) {
        super(restClient, objectMapper, RESOURCE_PATH, Offer.class);
    }

    public Offer findById(long id) {
        log.debug("Fetching offer {}", id);
        return restClient.get()
                .uri(RESOURCE_PATH + "/{id}", id)
                .retrieve()
                .body(Offer.class);
    }

    public Offer create(CreateOfferRequest request) {
        log.debug("Creating offer");
        return restClient.post()
                .uri(RESOURCE_PATH)
                .body(request)
                .retrieve()
                .body(Offer.class);
    }

    public Offer update(long id, UpdateOfferRequest request) {
        log.debug("Updating offer {}", id);
        return restClient.put()
                .uri(RESOURCE_PATH + "/{id}", id)
                .body(request)
                .retrieve()
                .body(Offer.class);
    }

    public void updateStock(long id, OfferStockUpdate update) {
        log.debug("Updating stock for offer {}", id);
        restClient.put()
                .uri(RESOURCE_PATH + "/{id}", id)
                .body(update)
                .retrieve()
                .toBodilessEntity();
    }

    public void updatePrice(long id, OfferPriceUpdate update) {
        log.debug("Updating price for offer {}", id);
        restClient.put()
                .uri(RESOURCE_PATH + "/{id}", id)
                .body(update)
                .retrieve()
                .toBodilessEntity();
    }

    public void deactivate(long id) {
        log.debug("Deactivating offer {}", id);
        restClient.put()
                .uri(RESOURCE_PATH + "/{id}", id)
                .body(java.util.Map.of("active", 0))
                .retrieve()
                .toBodilessEntity();
    }
}
