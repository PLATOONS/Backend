package com.platoons.e_commerce.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

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
import com.platoons.e_commerce.service.IOrderProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderProductServiceImpl implements IOrderProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public void addToCart(AddToCartRequestDto request, String userId) {

        if (request.quantity() == null || request.quantity() < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        Product product = productRepository.findByProductIdAndDeletedAtIsNull(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product", "productId", request.productId().toString()));

        Long customerId;
        try {
            customerId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid user id");
        }

        Customer customer = customerRepository
                .findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer", "customerId", customerId.toString()));

        if (request.quantity() > product.getStockQuantity()) {
            throw new BadRequestException("Quantity is greater than available stock");
        }

        OrderStatus cartStatus = orderStatusRepository
                .findByStatusNameIgnoreCase("CART")
                .orElseThrow(() -> new EntityNotFoundException("OrderStatus", "statusName", "CART"));

        Order order = orderRepository
                .findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus)
                .orElseGet(() -> {
                    Order o = new Order();
                    o.setCustomer(customer);
                    o.setOrderStatus(cartStatus);
                    o.setSubtotalAmount(0.0);
                    o.setTotalAmount(0.0);
                    return orderRepository.save(o);
                });

        String color = (request.color() == null || request.color().isBlank()) ? null : request.color();
        if (color != null) {
            Set<String> colors = product.getAvailableColors();
            if (colors == null || !colors.contains(color)) {
                throw new BadRequestException("Product isn't available in given color");
            }
        }

        Optional<OrderProduct> existingOpt = orderProductRepository
                .findByOrderAndProductAndColor(order, product, color);

        OrderProduct line = existingOpt.orElseGet(() -> {
            OrderProduct l = new OrderProduct();
            l.setOrder(order);
            l.setProduct(product);
            l.setColor(color);
            l.setQuantity(0);
            l.setTotalPrice(0.0);
            return l;
        });

        int newQty = line.getQuantity() + request.quantity();
        if (newQty > product.getStockQuantity()) {
            throw new BadRequestException("Quantity is greater than available stock");
        }

        line.setQuantity(newQty);
        double unitPrice = resolveUnitPrice(product);
        line.setTotalPrice(unitPrice * newQty);
        orderProductRepository.save(line);

        List<OrderProduct> lines = order.getOrderId() != null
                ? orderProductRepository.findAllByOrder(order)
                : List.of(line);

        double newSubtotal = lines.stream()
                .mapToDouble(op -> op.getTotalPrice() != null ? op.getTotalPrice() : 0.0)
                .sum();

        order.setSubtotalAmount(newSubtotal);
        order.setTotalAmount(newSubtotal);
        orderRepository.save(order);
    }

    public void removeFromCart(String productId, String userId) {

        Long customerId;
        try {
            customerId = Long.valueOf(userId);
        } catch (NumberFormatException e) {
            return;
        }

        var customerOpt = customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId);
        if (customerOpt.isEmpty()) {
            return;
        }
        var customer = customerOpt.get();

        var cartStatusOpt = orderStatusRepository.findByStatusNameIgnoreCase("CART");
        if (cartStatusOpt.isEmpty()) {
            return;
        }
        var cartStatus = cartStatusOpt.get();

        var orderOpt = orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus);
        if (orderOpt.isEmpty()) {
            return; // 204
        }
        var order = orderOpt.get();

        var lineOpt = orderProductRepository.findByOrderAndProductProductIdAndDeletedAtIsNull(order, productId);
        if (lineOpt.isEmpty()) {
            return; // 204
        }
        var line = lineOpt.get();

        line.setDeletedAt(java.time.LocalDateTime.now());
        orderProductRepository.save(line);

        var activeLines = orderProductRepository.findAllByOrderAndDeletedAtIsNull(order);
        double subtotal = activeLines.stream()
                .mapToDouble(op -> op.getTotalPrice() != null ? op.getTotalPrice() : 0.0)
                .sum();
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(subtotal);
        orderRepository.save(order);

    }

    private double resolveUnitPrice(Product p) {
        double price = p.getPrice();
        double discountPercent = p.getDiscount();
        double discountAmount = p.getDiscountAmount();

        double afterPercent = (discountPercent > 0) ? price * (1.0 - discountPercent / 100.0) : price;
        double finalPrice = afterPercent - Math.max(0.0, discountAmount);
        return Math.max(0.0, finalPrice);
    }
}
