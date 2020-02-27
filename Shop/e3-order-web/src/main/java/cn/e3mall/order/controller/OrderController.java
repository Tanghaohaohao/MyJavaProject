package cn.e3mall.order.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.e3mall.cart.service.CartService;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.order.pojo.OrderInfo;
import cn.e3mall.order.service.OrderService;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbUser;

/**
 * 订单管理
 * @author tangmaoqin
 *
 */
@Controller
public class OrderController {

	@Autowired
	private CartService cartService;
	@Autowired
	private OrderService orderservice;
	
	@RequestMapping("/order/order-cart")
	public String showOrderCart(HttpServletRequest request) {
		//根据用户id取地址表，支付方式
		TbUser user = (TbUser) request.getAttribute("user");
		List<TbItem> cartList = cartService.getCartList(user.getId());
		request.setAttribute("cartList", cartList);
		return "order-cart";
	}
	
	@RequestMapping(value="/order/create",method=RequestMethod.POST)
	public String createOrder(OrderInfo orderInfo, HttpServletRequest request) {
		TbUser user = (TbUser) request.getAttribute("user");
		orderInfo.setUserId(user.getId());
		orderInfo.setBuyerNick(user.getUsername());
		E3Result e3Result = orderservice.createOreder(orderInfo);
		if(e3Result.getStatus() == 200) {
			//清空购物车
			cartService.clearCartItem(user.getId());
		}
		request.setAttribute("orderId", e3Result.getData());
		request.setAttribute("payment", e3Result.getMsg());
		return "success";
	}
}
