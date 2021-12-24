package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.messagequeue.KafkaProducer;
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

@RestController
@RequestMapping("/order-service")
public class OrderController {

    Environment env;
    OrderService orderService;
    KafkaProducer kafkaProducer;

    @Autowired
    public OrderController(OrderService orderService, Environment env, KafkaProducer kafkaProducer) {
        this.orderService = orderService;
        this.env = env;
        this.kafkaProducer = kafkaProducer;
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

        OrderDto responseUserDto = orderService.createOrder(requestOrderDto);

        ResponseOrder responseOrder = mapper.map(responseUserDto, ResponseOrder.class);

        /* send tihs order to the kafka */
        kafkaProducer.send("example-catalog-topic", requestOrderDto);

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
