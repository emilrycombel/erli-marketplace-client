package pl.er.shoper.client.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterBuilderTest {

    @Test
    void emptyFilterProducesNoParams() {
        var params = FilterBuilder.create().toQueryParams();
        assertTrue(params.isEmpty());
    }

    @Test
    void eqFilterProducesCorrectParam() {
        var params = FilterBuilder.create()
                .eq("category_id", 90)
                .toQueryParams();

        assertEquals(1, params.size());
        assertEquals("90", params.get("filters[category_id]"));
    }

    @Test
    void gteFilterProducesCorrectParam() {
        var params = FilterBuilder.create()
                .gte("price", 10.0)
                .toQueryParams();

        assertEquals("10.0", params.get("filters[price[gte]]"));
    }

    @Test
    void lteFilterProducesCorrectParam() {
        var params = FilterBuilder.create()
                .lte("price", 100)
                .toQueryParams();

        assertEquals("100", params.get("filters[price[lte]]"));
    }

    @Test
    void likeFilterProducesCorrectParam() {
        var params = FilterBuilder.create()
                .like("name", "phone")
                .toQueryParams();

        assertEquals("phone", params.get("filters[name[like]]"));
    }

    @Test
    void inFilterProducesCorrectParam() {
        var params = FilterBuilder.create()
                .in("status", "active", "pending")
                .toQueryParams();

        assertEquals("active,pending", params.get("filters[status[in]]"));
    }

    @Test
    void sortProducesCorrectParam() {
        var params = FilterBuilder.create()
                .sort("price", SortOrder.ASC)
                .toQueryParams();

        assertEquals("price asc", params.get("order"));
    }

    @Test
    void limitProducesCorrectParam() {
        var params = FilterBuilder.create()
                .limit(50)
                .toQueryParams();

        assertEquals("50", params.get("limit"));
    }

    @Test
    void combinedFilters() {
        var params = FilterBuilder.create()
                .eq("category_id", 5)
                .gte("price", 10)
                .sort("name", SortOrder.DESC)
                .limit(25)
                .toQueryParams();

        assertEquals("5", params.get("filters[category_id]"));
        assertEquals("10", params.get("filters[price[gte]]"));
        assertEquals("name desc", params.get("order"));
        assertEquals("25", params.get("limit"));
    }

    @Test
    void fluentApiReturnsSameInstance() {
        var builder = FilterBuilder.create();
        var result = builder.eq("a", 1).gte("b", 2).limit(10);
        assertEquals(builder, result);
    }
}
