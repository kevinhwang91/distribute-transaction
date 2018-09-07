package org.jackson.puppy.demo.dubbo.confirm.controller;

import org.jackson.puppy.demo.dubbo.confirm.service.impl.ConfirmServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@RestController
@RequestMapping(value = "/")
public class ConfirmController {

	@Autowired
	private ConfirmServiceImpl confirmService;

	@GetMapping(value = "/")
	public String main() {
		return "index";
	}

	@GetMapping(value = "/send/orderNumber/{orderNumber}/agencyFee/{agencyFee}/unit/{unit}")
	public String sendConfirm(@PathVariable String orderNumber,
	                          @PathVariable BigDecimal agencyFee,
	                          @PathVariable BigDecimal unit) {
		confirmService.sendConfirm(orderNumber, agencyFee, unit);
		return "ok";
	}

	@GetMapping(value = "/rollBack/orderNumber/{orderNumber}")
	public String rollback(@PathVariable String orderNumber) {
		confirmService.deleteByOrderNumber(orderNumber);
		return "ok";
	}
}
