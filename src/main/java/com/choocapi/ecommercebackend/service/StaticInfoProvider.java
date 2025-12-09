package com.choocapi.ecommercebackend.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.choocapi.ecommercebackend.dto.context.StaticInfo;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaticInfoProvider {

    public StaticInfo getContactInfo() {
        return StaticInfo.builder()
                .contactEmail("contact@acbcomputer.store")
                .contactPhone("0352343012")
                .businessHours("8:00 - 22:00 hàng ngày")
                .address("5 - 7 Nguyễn Huy Tưởng, P.6, Q.Bình Thạnh, TP. Hồ Chí Minh")
                .build();
    }

    public StaticInfo getAboutInfo() {
        return StaticInfo.builder()
                .aboutUs("ACB Computer Store là cửa hàng chuyên cung cấp linh kiện máy tính, laptop và phụ kiện công nghệ chính hãng. "
                        + "Với nhiều năm kinh nghiệm trong ngành, chúng tôi cam kết mang đến cho khách hàng những sản phẩm chất lượng cao "
                        + "với giá cả cạnh tranh và dịch vụ hậu mãi tận tâm.")
                .build();
    }

    public StaticInfo getPolicyInfo(String policyType) {
        StaticInfo.StaticInfoBuilder builder = StaticInfo.builder();

        if (policyType == null || policyType.isEmpty()) {
            // Return all policies
            builder.shippingPolicy("Chính sách giao hàng: Miễn phí giao hàng cho đơn hàng trên 500.000 VNĐ trong nội thành. "
                            + "Giao hàng toàn quốc trong 2-5 ngày làm việc.")
                    .returnPolicy("Chính sách đổi trả: Đổi trả trong vòng 7 ngày nếu sản phẩm lỗi do nhà sản xuất. "
                            + "Sản phẩm phải còn nguyên tem, hộp và chưa qua sử dụng.")
                    .privacyPolicy("Chính sách bảo mật: Chúng tôi cam kết bảo mật thông tin cá nhân của khách hàng. "
                            + "Thông tin chỉ được sử dụng cho mục đích xử lý đơn hàng và không chia sẻ cho bên thứ ba.");
        } else {
            switch (policyType.toLowerCase()) {
                case "shipping":
                case "giao hàng":
                    builder.shippingPolicy("Chính sách giao hàng: Miễn phí giao hàng cho đơn hàng trên 500.000 VNĐ trong nội thành. "
                            + "Giao hàng toàn quốc trong 2-5 ngày làm việc.");
                    break;
                case "return":
                case "đổi trả":
                    builder.returnPolicy("Chính sách đổi trả: Đổi trả trong vòng 7 ngày nếu sản phẩm lỗi do nhà sản xuất. "
                            + "Sản phẩm phải còn nguyên tem, hộp và chưa qua sử dụng.");
                    break;
                case "privacy":
                case "bảo mật":
                    builder.privacyPolicy("Chính sách bảo mật: Chúng tôi cam kết bảo mật thông tin cá nhân của khách hàng. "
                            + "Thông tin chỉ được sử dụng cho mục đích xử lý đơn hàng và không chia sẻ cho bên thứ ba.");
                    break;
                default:
                    // Return all policies if type not recognized
                    builder.shippingPolicy("Chính sách giao hàng: Miễn phí giao hàng cho đơn hàng trên 500.000 VNĐ trong nội thành. "
                                    + "Giao hàng toàn quốc trong 2-5 ngày làm việc.")
                            .returnPolicy("Chính sách đổi trả: Đổi trả trong vòng 7 ngày nếu sản phẩm lỗi do nhà sản xuất. "
                                    + "Sản phẩm phải còn nguyên tem, hộp và chưa qua sử dụng.")
                            .privacyPolicy("Chính sách bảo mật: Chúng tôi cam kết bảo mật thông tin cá nhân của khách hàng. "
                                    + "Thông tin chỉ được sử dụng cho mục đích xử lý đơn hàng và không chia sẻ cho bên thứ ba.");
            }
        }

        return builder.build();
    }

    public StaticInfo getPaymentMethodsInfo() {
        List<String> paymentMethods = Arrays.asList(
                "VNPay - Thanh toán qua cổng VNPay",
                "MoMo - Ví điện tử MoMo",
                "ZaloPay - Ví điện tử ZaloPay",
                "COD - Thanh toán khi nhận hàng"
        );

        return StaticInfo.builder()
                .paymentMethods(paymentMethods)
                .build();
    }

    public StaticInfo getAllStaticInfo() {
        return StaticInfo.builder()
                .contactEmail("contact@acbcomputer.store")
                .contactPhone("0352343012")
                .businessHours("8:00 - 22:00 hàng ngày")
                .aboutUs("ACB Computer Store là cửa hàng chuyên cung cấp linh kiện máy tính, laptop và phụ kiện công nghệ chính hãng. "
                        + "Với nhiều năm kinh nghiệm trong ngành, chúng tôi cam kết mang đến cho khách hàng những sản phẩm chất lượng cao "
                        + "với giá cả cạnh tranh và dịch vụ hậu mãi tận tâm.")
                .shippingPolicy("Chính sách giao hàng: Miễn phí giao hàng cho đơn hàng trên 500.000 VNĐ trong nội thành. "
                        + "Giao hàng toàn quốc trong 2-5 ngày làm việc.")
                .returnPolicy("Chính sách đổi trả: Đổi trả trong vòng 7 ngày nếu sản phẩm lỗi do nhà sản xuất. "
                        + "Sản phẩm phải còn nguyên tem, hộp và chưa qua sử dụng.")
                .privacyPolicy("Chính sách bảo mật: Chúng tôi cam kết bảo mật thông tin cá nhân của khách hàng. "
                        + "Thông tin chỉ được sử dụng cho mục đích xử lý đơn hàng và không chia sẻ cho bên thứ ba.")
                .paymentMethods(Arrays.asList(
                        "VNPay - Thanh toán qua cổng VNPay",
                        "MoMo - Ví điện tử MoMo",
                        "ZaloPay - Ví điện tử ZaloPay",
                        "COD - Thanh toán khi nhận hàng"
                ))
                .address("5 - 7 Nguyễn Huy Tưởng, P.6, Q.Bình Thạnh, TP. Hồ Chí Minh")
                .build();
    }
}
