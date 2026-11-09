package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeliveryAddress(
        String firstName,
        String lastName,
        String companyName,
        String address,
        String street,
        String buildingNumber,
        String flatNumber,
        String zip,
        String city,
        String country,
        String phone
) {}
