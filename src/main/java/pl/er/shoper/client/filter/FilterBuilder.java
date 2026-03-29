package pl.er.shoper.client.filter;

import java.util.LinkedHashMap;
import java.util.Map;

public class FilterBuilder {

    private final Map<String, String> filters = new LinkedHashMap<>();
    private String sortField;
    private SortOrder sortOrder;
    private Integer limit;

    private FilterBuilder() {
    }

    public static FilterBuilder create() {
        return new FilterBuilder();
    }

    public FilterBuilder eq(String field, Object value) {
        filters.put(field, value.toString());
        return this;
    }

    public FilterBuilder gte(String field, Object value) {
        filters.put(field + "[gte]", value.toString());
        return this;
    }

    public FilterBuilder lte(String field, Object value) {
        filters.put(field + "[lte]", value.toString());
        return this;
    }

    public FilterBuilder like(String field, Object value) {
        filters.put(field + "[like]", value.toString());
        return this;
    }

    public FilterBuilder in(String field, Object... values) {
        var joined = String.join(",", java.util.Arrays.stream(values).map(Object::toString).toList());
        filters.put(field + "[in]", joined);
        return this;
    }

    public FilterBuilder sort(String field, SortOrder order) {
        this.sortField = field;
        this.sortOrder = order;
        return this;
    }

    public FilterBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Map<String, String> toQueryParams() {
        var params = new LinkedHashMap<String, String>();
        filters.forEach((key, value) -> params.put("filters[" + key + "]", value));
        if (sortField != null && sortOrder != null) {
            params.put("order", sortField + " " + sortOrder.name().toLowerCase());
        }
        if (limit != null) {
            params.put("limit", limit.toString());
        }
        return params;
    }
}
