package com.example.RestaurantApplication.module.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.example.RestaurantApplication.module.ingredient.model.Ingredient;
import com.example.RestaurantApplication.module.ingredient.repository.IngredientRepository;
import com.example.RestaurantApplication.module.order.dto.CreateOrderRequest;
import com.example.RestaurantApplication.module.order.dto.OrderDetailDto;
import com.example.RestaurantApplication.module.order.dto.OrderItemDto;
import com.example.RestaurantApplication.module.order.dto.OrderItemRequest;
import com.example.RestaurantApplication.module.order.dto.OrderSummaryDto;
import com.example.RestaurantApplication.module.order.model.Order;
import com.example.RestaurantApplication.module.order.model.OrderDetail;
import com.example.RestaurantApplication.module.order.repository.OrderDetailRepository;
import com.example.RestaurantApplication.module.order.repository.OrderRepository;
import com.example.RestaurantApplication.utils.Exceptions.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.RestaurantApplication.config.tracing.LogHelper;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    /**
     * Get all orders (Hibernate Filter will apply for STAFF/MANAGER)
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllActive();
    }

    /**
     * Get order by ID (Hibernate Filter will apply for STAFF/MANAGER)
     */
    public Order getOrderById(Long id) {
        return orderRepository.findByIdActive(id)
            .orElseThrow(() -> {
                log.warn("[{}] ORDER_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("ORDER_NOT_FOUND",
                    "Order not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });
    }

    /**
     * Get order details with items
     */
    public OrderDetailDto getOrderDetailDto(Long id) {
        Order order = getOrderById(id);
        List<OrderDetail> details = orderDetailRepository.findByOrderIdWithIngredient(id);

        List<OrderItemDto> items = details.stream()
            .map(d -> new OrderItemDto(
                d.getId(),
                d.getIngredientId(),
                d.getIngredient().getCode(),
                d.getIngredient().getName(),
                d.getIngredient().getPrice(),
                d.getQuantity()
            ))
            .collect(Collectors.toList());

        return new OrderDetailDto(
            order.getId(),
            order.getCode(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUserId(),
            order.getRestaurantId(),
            items
        );
    }

    /**
     * Create new order
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // Get user info from SecurityContext
        Long userId = getAuthenticatedUserId();
        Long restaurantId = getAuthenticatedRestaurantId();

        // Validate and get ingredients
        List<Long> ingredientIds = request.getItems().stream()
            .map(OrderItemRequest::getIngredientId)
            .collect(Collectors.toList());

        List<Ingredient> ingredients = ingredientRepository.findAllById(ingredientIds);

        if (ingredients.size() != ingredientIds.size()) {
            log.warn("[{}] INGREDIENT_NOT_FOUND: some ingredients missing", LogHelper.loc());
            throw new BusinessException("INGREDIENT_NOT_FOUND",
                "One or more ingredients not found",
                HttpStatus.BAD_REQUEST);
        }

        // Create map for quick lookup
        Map<Long, Ingredient> ingredientMap = ingredients.stream()
            .collect(Collectors.toMap(Ingredient::getId, i -> i));

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            Ingredient ingredient = ingredientMap.get(item.getIngredientId());
            totalAmount = totalAmount.add(
                BigDecimal.valueOf(ingredient.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        // Create order
        Order order = new Order();
        order.setCode(NanoIdUtils.randomNanoId());
        order.setTotalAmount(totalAmount);
        order.setUserId(userId);
        order.setRestaurantId(restaurantId);

        order = orderRepository.save(order);
        log.info("[{}] Order created: code={}, total={}", LogHelper.loc(), order.getCode(), totalAmount);

        // Create order details
        for (OrderItemRequest item : request.getItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getId());
            detail.setIngredientId(item.getIngredientId());
            detail.setQuantity(item.getQuantity());
            detail.setUserId(userId);
            detail.setRestaurantId(restaurantId);
            orderDetailRepository.save(detail);
        }

        return order;
    }

    /**
     * Soft delete order (Admin only)
     */
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("[{}] ORDER_NOT_FOUND: id={}", LogHelper.loc(), id);
                return new BusinessException("ORDER_NOT_FOUND",
                    "Order not found with id " + id,
                    HttpStatus.NOT_FOUND);
            });

        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Also soft delete order details
        List<OrderDetail> details = orderDetailRepository.findByOrderIdActive(id);
        for (OrderDetail detail : details) {
            detail.setDeletedAt(LocalDateTime.now());
            orderDetailRepository.save(detail);
        }
        log.info("[{}] Order deleted: id={}", LogHelper.loc(), id);
    }

    /**
     * Convert Order to OrderSummaryDto
     */
    public OrderSummaryDto toSummaryDto(Order order) {
        return new OrderSummaryDto(
            order.getId(),
            order.getCode(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUserId(),
            order.getRestaurantId()
        );
    }

    // Helper methods to get user info from SecurityContext
    @SuppressWarnings("unchecked")
    private Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof Map)) {
            log.warn("[{}] UNAUTHORIZED: user not authenticated", LogHelper.loc());
            throw new BusinessException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        Map<String, Object> details = (Map<String, Object>) auth.getDetails();
        return (Long) details.get("user_id");
    }

    @SuppressWarnings("unchecked")
    private Long getAuthenticatedRestaurantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof Map)) {
            log.warn("[{}] UNAUTHORIZED: user not authenticated", LogHelper.loc());
            throw new BusinessException("UNAUTHORIZED", "User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        Map<String, Object> details = (Map<String, Object>) auth.getDetails();
        return (Long) details.get("restaurant_id");
    }
}