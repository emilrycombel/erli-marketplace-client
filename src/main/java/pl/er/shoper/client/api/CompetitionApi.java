package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.filter.FilterBuilder;
import pl.er.shoper.client.model.competition.CompetitionOffer;

import java.util.stream.Stream;

public class CompetitionApi extends BaseApi<CompetitionOffer> {

    private static final Logger log = LogManager.getLogger(CompetitionApi.class);
    private static final String RESOURCE_PATH = "/webapi/rest/competition-offers";

    public CompetitionApi(RestClient restClient, ObjectMapper objectMapper) {
        super(restClient, objectMapper, RESOURCE_PATH, CompetitionOffer.class);
    }

    public Stream<CompetitionOffer> findByEan(String ean) {
        log.debug("Fetching competition offers for EAN: {}", ean);
        return findAll(FilterBuilder.create().eq("ean", ean));
    }
}
