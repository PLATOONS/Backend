package com.platoons.e_commerce;

import com.platoons.e_commerce.dto.CreateOrderRequestDto;
import com.platoons.e_commerce.dto.OrderDto;
import com.platoons.e_commerce.dto.UpdateOrderDto;
import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.entity.Coupon;
import com.platoons.e_commerce.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class OrderMapperTest {

    @Test
    void testMapCreateOrderRequestDtoToOrder_WithCoupon() {
        CreateOrderRequestDto dto = new CreateOrderRequestDto();
        dto.setSubTotalAmount(100.0);
        dto.setTotalAmout(120.0);
        dto.setCustomer("1");
        dto.setCouponId(123L);

        Order order = new Order();
        Order result = OrderMapper.mapCreateOrderRequestDtoToOrder(dto, order);

        assertEquals(100.0, result.getSubtotalAmount());
        assertEquals(120.0, result.getTotalAmount());
        assertNotNull(result.getCustomer());
        assertEquals("1", result.getCustomer().getCustomerId());
        assertNotNull(result.getCoupon());
        assertEquals(123, result.getCoupon().getCouponId());
    }

    @Test
    void testMapCreateOrderRequestDtoToOrder_WithoutCoupon() {
        CreateOrderRequestDto dto = new CreateOrderRequestDto();
        dto.setSubTotalAmount(200.0);
        dto.setTotalAmout(220.0);
        dto.setCustomer("2");
        dto.setCouponId(null);

        Order order = new Order();
        Order result = OrderMapper.mapCreateOrderRequestDtoToOrder(dto, order);

        assertEquals(200.0, result.getSubtotalAmount());
        assertEquals(220.0, result.getTotalAmount());
        assertNotNull(result.getCustomer());
        assertEquals("2", result.getCustomer().getCustomerId());
        assertNull(result.getCoupon());
    }

    @Test
    void testMapOrderToOrderDto_WithCoupon() {
        Order order = new Order();
        order.setOrderId(001L);
        order.setSubtotalAmount(150.0);
        order.setTotalAmount(180.0);

        Customer customer = new Customer();
        customer.setCustomerId("5");
        customer.setUsername("jdoe");
        customer.setEmail("jdoe@mail.com");
        customer.setPhoneNumber("555-1234");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        order.setCustomer(customer);

        Coupon coupon = new Coupon();
        coupon.setCouponId(100L);
        coupon.setCouponCode("WELCOME10");
        coupon.setDiscountAmount(10.0);
        order.setCoupon(coupon);

        OrderDto orderDto = new OrderDto();
        OrderDto result = OrderMapper.mapOrderToOrderDto(order, orderDto);

        assertEquals(1, result.getOrderId());
        assertEquals(150.0, result.getSubTotalAmout());
        assertEquals(180.0, result.getTotalAmount());
        assertNotNull(result.getCustomer());
        assertEquals("5", result.getCustomer().getCustomerId());
        assertNotNull(result.getCoupon());
        assertEquals(100, result.getCoupon().getCouponId());
        assertEquals("WELCOME10", result.getCoupon().getCouponCode());
        assertEquals(10.0, result.getCoupon().getDiscountAmount());
    }

    @Test
    void testMapUpdateOrderDtoToOrder_WithoutCoupon() {
        UpdateOrderDto dto = new UpdateOrderDto();
        dto.setSubTotalAmount(300.0);
        dto.setTotalAmout(330.0);
        dto.setCustomer("9");
        dto.setCouponId(null);

        Order order = new Order();
        Order result = OrderMapper.mapUpdateOrderDtoToOrder(dto, order);

        assertEquals(300.0, result.getSubtotalAmount());
        assertEquals(330.0, result.getTotalAmount());
        assertNotNull(result.getCustomer());
        assertEquals("9", result.getCustomer().getCustomerId());
        assertNull(result.getCoupon());
    }

    @Test
    void testMapUpdateOrderDtoToOrder_WithCoupon() {
        UpdateOrderDto dto = new UpdateOrderDto();
        dto.setSubTotalAmount(400.0);
        dto.setTotalAmout(440.0);
        dto.setCustomer("10");
        dto.setCouponId(200L);

        Order order = new Order();
        Order result = OrderMapper.mapUpdateOrderDtoToOrder(dto, order);

        assertEquals(400.0, result.getSubtotalAmount());
        assertEquals(440.0, result.getTotalAmount());
        assertNotNull(result.getCustomer());
        assertEquals("10", result.getCustomer().getCustomerId());
        assertNotNull(result.getCoupon());
        assertEquals(200, result.getCoupon().getCouponId());
    }
}
