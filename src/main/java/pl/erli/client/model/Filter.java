package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface Filter permits Filter.Field, Filter.Compound, Filter.Not {

    record Field(String field, String operator, Object value) implements Filter {}

    record Compound(String operator, List<Filter> value) implements Filter {}

    record Not(String operator, Filter value) implements Filter {
        public Not(Filter value) {
            this("not", value);
        }
    }

    static Filter eq(String field, Object value) {
        return new Field(field, "=", value);
    }

    static Filter neq(String field, Object value) {
        return new Field(field, "!=", value);
    }

    static Filter gt(String field, Object value) {
        return new Field(field, ">", value);
    }

    static Filter gte(String field, Object value) {
        return new Field(field, ">=", value);
    }

    static Filter lt(String field, Object value) {
        return new Field(field, "<", value);
    }

    static Filter lte(String field, Object value) {
        return new Field(field, "<=", value);
    }

    static Filter in(String field, List<?> values) {
        return new Field(field, "in", values);
    }

    static Filter nin(String field, List<?> values) {
        return new Field(field, "nin", values);
    }

    static Filter and(Filter... filters) {
        return new Compound("and", List.of(filters));
    }

    static Filter or(Filter... filters) {
        return new Compound("or", List.of(filters));
    }

    static Filter not(Filter filter) {
        return new Not(filter);
    }
}
