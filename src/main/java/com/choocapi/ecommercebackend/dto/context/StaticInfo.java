package com.choocapi.ecommercebackend.dto.context;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaticInfo {
    String contactEmail;
    String contactPhone;
    String businessHours;
    String aboutUs;
    String shippingPolicy;
    String returnPolicy;
    String privacyPolicy;
    List<String> paymentMethods;
    String address;
}
