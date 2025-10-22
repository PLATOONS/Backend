package com.platoons.e_commerce.service.impl;

import java.util.List;
import java.util.Optional;

import com.platoons.e_commerce.dto.CartProductsDto;
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
    public void addToCart(AddToCartRequestDto request, String username) {

        if (request.quantity() == null || request.quantity() < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        Product product = productRepository.findByProductIdAndDeletedAtIsNull(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product", "productId", request.productId()));
        Customer customer = customerRepository
                .findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new EntityNotFoundException("Customer", "username", username));

        if (request.quantity() > product.getStockQuantity()) {
            throw new BadRequestException("Quantity is greater than available stock");
        }

        Optional<OrderStatus> optionalCartStatus = orderStatusRepository
                .findByStatusNameIgnoreCase("CART");

        OrderStatus cartStatus;

        if (optionalCartStatus.isEmpty()){
            OrderStatus status = new OrderStatus();
            status.setStatusName("CART");
            status.setDescription("User's cart");
            cartStatus = orderStatusRepository.save(status);
        }else{
            cartStatus = optionalCartStatus.get();
        }

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

        Optional<OrderProduct> existingOpt = orderProductRepository
                .findByOrderAndProductAndColorAndDeletedAtIsNull(order, product, color);

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
                .filter(op -> op.getDeletedAt() == null)
                .mapToDouble(op -> op.getTotalPrice() != null ? op.getTotalPrice() : 0.0)
                .sum();

        order.setSubtotalAmount(newSubtotal);
        order.setTotalAmount(newSubtotal);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void removeFromCart(String productId, String username) {
        var customerOpt = customerRepository.findByUsernameAndDeletedAtIsNull(username);

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
            return;
        }
        var order = orderOpt.get();

        var lineOpt = orderProductRepository.findByOrderAndProductProductIdAndDeletedAtIsNull(order, productId);
        if (lineOpt.isEmpty()) {
            return;
        }
        var line = lineOpt.get();

        line.setDeletedAt(java.time.LocalDateTime.now());
        orderProductRepository.save(line);

        var activeLines = orderProductRepository.findAllByOrderAndDeletedAtIsNull(order);
        double subtotal = activeLines.stream()
                .mapToDouble(op -> op.getTotalPrice() != null ? op.getTotalPrice() : 0.0)
                .sum();
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(subtotal); // Update total as well
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateQuantity(String productId, int quantity, String username) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        Product product = productRepository.findByProductIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", "productId", productId));

        Customer customer = customerRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new EntityNotFoundException("Customer", "username", username));

        OrderStatus cartStatus = orderStatusRepository.findByStatusNameIgnoreCase("CART")
                .orElseThrow(() -> new EntityNotFoundException("OrderStatus", "statusName", "CART"));

        Order order = orderRepository.findFirstByCustomerAndOrderStatusAndDeletedAtIsNull(customer, cartStatus)
                .orElseThrow(() -> new BadRequestException("No active cart found for user"));

        OrderProduct line = orderProductRepository
                .findByOrderAndProductProductIdAndDeletedAtIsNull(order, productId)
                .orElseThrow(() -> new EntityNotFoundException("OrderProduct", "productId", productId));

        if (quantity > product.getStockQuantity()) {
            throw new BadRequestException("Quantity is greater than available stock");
        }

        line.setQuantity(quantity);
        double unitPrice = resolveUnitPrice(product);
        line.setTotalPrice(unitPrice * quantity);
        orderProductRepository.save(line);

        List<OrderProduct> activeLines = orderProductRepository.findAllByOrderAndDeletedAtIsNull(order);
        double newSubtotal = activeLines.stream()
                .mapToDouble(op -> op.getTotalPrice() != null ? op.getTotalPrice() : 0.0)
                .sum();
        order.setSubtotalAmount(newSubtotal);
        order.setTotalAmount(newSubtotal);
        orderRepository.save(order);
    }

    @Override
    public List<CartProductsDto> fetchCartProducts(String username) {
        customerRepository.findByUsernameAndDeletedAtIsNull(username)
            .orElseThrow(() -> new EntityNotFoundException("Customer", "username", username));

        return productRepository.fetchCartProducts(username);
    }

    private double resolveUnitPrice(Product p) {
        double price = p.getPrice();
        double discountPercent = p.getDiscount();
        double discountAmount = p.getDiscountAmount();

        double priceAfterPercent = (discountPercent > 0) ? price * (1.0 - (discountPercent / 100.0)) : price;

        double finalPrice = priceAfterPercent - discountAmount;

        return Math.max(0.0, finalPrice);
    }
}
