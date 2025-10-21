package com.platoons.e_commerce;

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
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
class OrderProductControllerTest {

    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private OrderProductRepository orderProductRepository;
    @MockitoBean
    private OrderStatusRepository orderStatusRepository;
    @MockitoBean
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

    private Customer customer(String id) {
        Customer c = new Customer();
        c.setCustomerId(id);
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

        when(customerRepository.findByUsernameAndDeletedAtIsNull("1"))
                .thenReturn(Optional.of(customer("1")));

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
        assertDoesNotThrow(() -> service.addToCart(req, "1"));

        verify(orderProductRepository, times(1)).save(any(OrderProduct.class));
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void addToCart_incrementsExistingLine_ok() {
        var req = new AddToCartRequestDto("PROD-1", 1, null);

        Product p = product("PROD-1", 200.0, 0.0, 0.0, 10);
        Customer c = customer("1");
        OrderStatus s = cartStatus();
        Order o = order(50L, c, s);

        OrderProduct existing = new OrderProduct();
        existing.setOrder(o);
        existing.setProduct(p);
        existing.setColor(null);
        existing.setQuantity(2);
        existing.setTotalPrice(400.0); // 2 * 200

        when(productRepository.findByProductIdAndDeletedAtIsNull("PROD-1")).thenReturn(Optional.of(p));
        when(customerRepository.findByUsernameAndDeletedAtIsNull("1")).thenReturn(Optional.of(c));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART")).thenReturn(Optional.of(s));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(c, s)).thenReturn(Optional.of(o));
        when(orderProductRepository.findByOrderAndProductAndColorAndDeletedAtIsNull(o, p, null)).thenReturn(Optional.of(existing));

        when(orderProductRepository.findAllByOrder(o)).thenReturn(List.of(existing));

        service.addToCart(req, "1");

        assertEquals(3, existing.getQuantity());           // 2 + 1
        assertEquals(600.0, existing.getTotalPrice());
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
        verify(customerRepository, never()).findByCustomerIdAndDeletedAtIsNull(anyString());
    }

    // 400: cantidad < stock
    @Test
    void addToCart_newQtyGreaterThanStock_throws400() {
        var req = new AddToCartRequestDto("PROD-1", 10, null);
        Product p = product("PROD-1", 50.0, 0.0, 0.0, 5);

        when(productRepository.findByProductIdAndDeletedAtIsNull("PROD-1")).thenReturn(Optional.of(p));
        when(customerRepository.findByUsernameAndDeletedAtIsNull("1")).thenReturn(Optional.of(customer("1")));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART")).thenReturn(Optional.of(cartStatus()));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(any(), any())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> service.addToCart(req, "1"), "Quantity is greater than available stock");
        verify(orderProductRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // 400: cantidad > stock
    @Test
    void addToCart_existingQtyPlusNewGreaterThanStock_throws400() {
        var req = new AddToCartRequestDto("PROD-1", 4, null); // Pide 4 más
        Product p = product("PROD-1", 50.0, 0.0, 0.0, 5); // Stock 5
        Customer c = customer("1");
        OrderStatus s = cartStatus();
        Order o = order(50L, c, s);

        OrderProduct existing = new OrderProduct();
        existing.setOrder(o);
        existing.setProduct(p);
        existing.setColor(null);
        existing.setQuantity(2);

        when(productRepository.findByProductIdAndDeletedAtIsNull("PROD-1")).thenReturn(Optional.of(p));
        when(customerRepository.findByUsernameAndDeletedAtIsNull("1")).thenReturn(Optional.of(c));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART")).thenReturn(Optional.of(s));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(c, s)).thenReturn(Optional.of(o));
        when(orderProductRepository.findByOrderAndProductAndColorAndDeletedAtIsNull(o, p, null)).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> service.addToCart(req, "1"), "Quantity is greater than available stock");
        verify(orderProductRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // removeFromCart
    @Test
    void removeFromCart_softDelete_ok() {
        Product p = product("PROD-1", 100.0, 0.0, 0.0, 10);
        Customer c = customer("1");
        OrderStatus s = cartStatus();
        Order o = order(77L, c, s);

        OrderProduct line = new OrderProduct();
        line.setOrderProductId(123L);
        line.setOrder(o);
        line.setProduct(p);
        line.setQuantity(2);
        line.setTotalPrice(200.0);

        when(customerRepository.findByUsernameAndDeletedAtIsNull("1")).thenReturn(Optional.of(c));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART")).thenReturn(Optional.of(s));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(c, s)).thenReturn(Optional.of(o));

        when(orderProductRepository.findByOrderAndProductProductIdAndDeletedAtIsNull(o, "PROD-1"))
                .thenReturn(Optional.of(line));

        when(orderProductRepository.findAllByOrderAndDeletedAtIsNull(o))
                .thenReturn(List.of());

        service.removeFromCart("PROD-1", "1");
        assertNotNull(line.getDeletedAt());
        assertTrue(line.getDeletedAt().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));

        verify(orderProductRepository).save(line);
        verify(orderRepository).save(o);
        assertEquals(0.0, o.getSubtotalAmount());
        assertEquals(0.0, o.getTotalAmount());

    }

    @Test
    void removeFromCart_notFound_anything_noException() {
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("1"))
                .thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.removeFromCart("PROD-1", "1"));
        verify(orderStatusRepository, never()).findByStatusNameIgnoreCase(any());
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("1"))
                .thenReturn(Optional.of(customer("1")));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                .thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.removeFromCart("PROD-1", "1"));
        verify(orderRepository, never()).findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(any(), any());

        // Caso 3: orden no existe
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("1"))
                .thenReturn(Optional.of(customer("1")));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                .thenReturn(Optional.of(cartStatus()));
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(any(), any()))
                .thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.removeFromCart("PROD-1", "1"));
        verify(orderProductRepository, never()).findByOrderAndProductProductIdAndDeletedAtIsNull(any(), any());

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("1"))
                .thenReturn(Optional.of(customer("1")));
        when(orderStatusRepository.findByStatusNameIgnoreCase("CART"))
                .thenReturn(Optional.of(cartStatus()));
        Order existingOrder = order(1L, customer("1"), cartStatus());
        when(orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(any(), any()))
                .thenReturn(Optional.of(existingOrder));
        when(orderProductRepository.findByOrderAndProductProductIdAndDeletedAtIsNull(existingOrder, "PROD-1"))
                .thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.removeFromCart("PROD-1", "1"));
        verify(orderProductRepository, never()).save(any(OrderProduct.class));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
