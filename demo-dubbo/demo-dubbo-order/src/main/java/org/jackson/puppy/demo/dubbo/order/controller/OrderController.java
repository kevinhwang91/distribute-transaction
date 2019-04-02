package org.jackson.puppy.demo.dubbo.order.controller;

import org.jackson.puppy.demo.dubbo.order.api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kevin Hwang
 * @since 4/1/2019
 */
@RestController
@RequestMapping(value = "/")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping(value = "/rollBack/orderNumber/{orderNumber}")
    public String rollback(@PathVariable String orderNumber) {
        orderService.rollBackOrder(orderNumber);
        return "ok";
    }
}
