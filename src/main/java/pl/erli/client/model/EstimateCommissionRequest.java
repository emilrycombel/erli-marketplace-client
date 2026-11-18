package pl.erli.client.model;

public record EstimateCommissionRequest(int categoryId, int quantity, int unitPrice) {}
