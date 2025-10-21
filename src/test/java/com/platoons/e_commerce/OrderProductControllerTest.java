package com.platoons.e_commerce;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.context.ActiveProfiles;

import com.platoons.e_commerce.dto.AddToCartRequestDto;
import com.platoons.e_commerce.entity.Customer;
import com.platoons.e_commerce.entity.Order;
import com.platoons.e_commerce.entity.OrderProduct;
import com.platoons.e_commerce.entity.OrderStatus;
import com.platoons.e_commerce.entity.Product;
import com.platoons.e_commerce.exceptions.BadRequestException;
import com.platoons.e_commerce.exceptions.EntityNotFoundException;
import com.platoons.e_commerce.repository.CustomerRepository;
import com.platoons.e_commerce.repository.OrderProductRepository;
import com.platoons.e_commerce.repository.OrderRepository;
import com.platoons.e_commerce.repository.OrderStatusRepository;
import com.platoons.e_commerce.repository.ProductRepository;
import com.platoons.e_commerce.service.impl.OrderProductServiceImpl;

@ActiveProfiles("test")
class OrderProductControllerTest {

    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private OrderProductRepository orderProductRepository;
    private OrderStatusRepository orderStatusRepository;
    private CustomerRepository customerRepository;

    private OrderProductServiceImpl service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderProductRepository = mock(OrderProductRepository.class);
        orderStatusRepository = mock(OrderStatusRepository.class);
        customerRepository = mock(CustomerRepository.class);

        service = new OrderProductServiceImpl(
                productRepository, orderRepository, orderProductRepository,
                orderStatusRepository, customerRepository
        );
    }

    private Product product(String id, double price, double discountPct, double discountAmt, int stock) {
        Product p = new Product();
        p.setProductId(id);
        p.setPrice(price);
        p.setDiscount(discountPct);
        p.setDiscountAmount(discountAmt);
        p.setStockQuantity(stock);
        return p;
    }

    private Customer customer(long id) {
        Customer c = new Customer();
        c.setCustomerId(String.valueOf(id));
        return c;
    }

    private OrderStatus cartStatus() {
        OrderStatus s = new OrderStatus();
        s.setStatusId(10L);
        s.setStatusName("CART");
        return s;
    }

    private Order order(long id, Customer c, OrderStatus s) {
        Order o = new Order();
        o.setOrderId(id);
        o.setCustomer(c);
        o.setOrderStatus(s);
        o.setSubtotalAmount(0.0);
        o.setTotalAmount(0.0);
        return o;
    }

    @Test
    void addToCart_createsCartAndLine_ok() {
        var req = new AddToCartRequestDto("PROD-1", 2, "red");

        Product mockProduct = product("PROD-1", 100.0, 10.0, 5.0, 5);

        when(productRepository.findByProductIdAndDeletedAtIsNull("PROD-1"))
                .thenReturn(Optional.of(mockProduct));

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(customer(1L)));

        when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                .thenReturn(Optional.of(cartStatus()));

        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(any(), any()))
                .thenReturn(Optional.empty());

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    o.setOrderId(99L);
                    return o;
                });

        when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(any(Order.class)))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> service.addToCart(req, "1"));

        // Verificaciones
        verify(orderProductRepository, times(1)).save(any(OrderProduct.class));
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void addToCart_incrementsExistingLine_ok() {
        var req = new AddToCartRequestDto("PROD-1", 1, null);

        Product p = product("PROD-1", 200.0, 0.0, 0.0, 10); // unit = 200
        Customer c = customer(1L);
        OrderStatus s = cartStatus();
        Order o = order(50L, c, s);

        OrderProduct existing = new OrderProduct();
        existing.setOrder(o);
        existing.setProduct(p);
        existing.setColor(null);
        existing.setQuantity(2);
        existing.setTotalPrice(400.0); // 2 * 200

        when(productRepository.findByProductIdAndDeletedAtIsNull("PROD-1")).thenReturn(Optional.of(p));
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(c));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART")).thenReturn(Optional.of(s));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(c, s)).thenReturn(Optional.of(o));
        when(orderProductRepository.findByOrderAndProductAndColor(o, p, null)).thenReturn(Optional.of(existing));

        when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(o)).thenReturn(List.of(existing));

        service.addToCart(req, "1");

        assertEquals(3, existing.getQuantity());           // 2 + 1
        assertEquals(600.0, existing.getTotalPrice());      // 3 * 200
        verify(orderProductRepository).save(existing);
        verify(orderRepository).save(o);
    }

    // 404: producto no existe
    @Test
    void addToCart_productNotFound_throws404() {
        var req = new AddToCartRequestDto("NOPE", 1, null);

        when(productRepository.findByProductIdAndDeletedAtIsNull("NOPE"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addToCart(req, "1"));
    }

    // 400: cantidad > stock
    @Test
    void addToCart_quantityGreaterThanStock_throws400() {
        var req = new AddToCartRequestDto("PROD-1", 10, null);
        when(productRepository.findByProductIdAndDeletedAtIsNull("PROD-1"))
                .thenReturn(Optional.of(product("PROD-1", 50.0, 0.0, 0.0, 5))); // stock 5

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(customer(1L)));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                .thenReturn(Optional.of(cartStatus()));

        assertThrows(BadRequestException.class, () -> service.addToCart(req, "1"));
        verify(orderProductRepository, never()).save(any());
    }

    // removeFromCart: línea existe → soft delete + recalcular totales
    @Test
    void removeFromCart_softDelete_ok() {
        Product p = product("PROD-1", 100.0, 0.0, 0.0, 10);
        Customer c = customer(1L);
        OrderStatus s = cartStatus();
        Order o = order(77L, c, s);

        OrderProduct line = new OrderProduct();
        line.setOrder(o);
        line.setProduct(p);
        line.setQuantity(2);
        line.setTotalPrice(200.0);

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(c));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART")).thenReturn(Optional.of(s));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(c, s)).thenReturn(Optional.of(o));

        when(orderProductRepository.findByOrderAndProductProductIdAndDeletedAtIsNull(o, "PROD-1"))
                .thenReturn(Optional.of(line));

        when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(o))
                .thenReturn(List.of()); // tras borrar, no quedan activas

        service.removeFromCart("PROD-1", "1");

        assertNotNull(line.getDeletedAt());
        assertTrue(
                line.getDeletedAt().isBefore(
                        Instant.now().plusSeconds(5).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                )
        );
        assertEquals(0.0, o.getSubtotalAmount());
        assertEquals(0.0, o.getTotalAmount());
        verify(orderProductRepository).save(line);
        verify(orderRepository).save(o);
    }

    @Test
    void removeFromCart_notFound_anything_noException() {
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.removeFromCart("PROD-1", "1"));

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(customer(1L)));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.removeFromCart("PROD-1", "1"));

        when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                .thenReturn(Optional.of(cartStatus()));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(any(), any()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.removeFromCart("PROD-1", "1"));
    }
}
