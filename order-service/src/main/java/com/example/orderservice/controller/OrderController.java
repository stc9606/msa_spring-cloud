package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.OrderServiceImpl;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order-service")
public class OrderController {

    Environment env;
    OrderService orderService;
    KafkaProducer kafkaProducer;
    OrderProducer orderProducer;

    @Autowired
    public OrderController(OrderService orderService, Environment env, KafkaProducer kafkaProducer,
                           OrderProducer orderProducer) {
        this.orderService = orderService;
        this.env = env;
        this.kafkaProducer = kafkaProducer;
        this.orderProducer = orderProducer;
    }

    @GetMapping("/health_check")
    public String status() {
        return "";
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createUser(@PathVariable("userId") String userId, @RequestBody RequestOrder requestOrder) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto requestOrderDto = mapper.map(requestOrder, OrderDto.class);
        requestOrderDto.setUserId(userId);

        /* JPA */
//        OrderDto responseUserDto = orderService.createOrder(requestOrderDto);
//        ResponseOrder responseOrder = mapper.map(responseUserDto, ResponseOrder.class);

        /* Kafka */
        requestOrderDto.setOrderId(UUID.randomUUID().toString());
        requestOrderDto.setTotalPrice(requestOrder.getQty() * requestOrder.getUnitPrice());

        /* send tihs order to the kafka */
        kafkaProducer.send("example-catalog-topic", requestOrderDto);
        orderProducer.send("orders", requestOrderDto);

        ResponseOrder responseOrder = mapper.map(requestOrderDto, ResponseOrder.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId")String userId) {
        Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> responseOrders = new ArrayList<>();

        orderList.forEach(entity -> {
            responseOrders.add(new ModelMapper().map(entity, ResponseOrder.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(responseOrders);
    }

}
