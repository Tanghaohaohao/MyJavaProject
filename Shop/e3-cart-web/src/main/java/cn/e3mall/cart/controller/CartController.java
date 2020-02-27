package cn.e3mall.cart.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
/**
 * 购物车处理
 * @author tangmaoqin
 *
 */
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.e3mall.cart.service.CartService;
import cn.e3mall.common.utils.CookieUtils;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.service.ItemService;
@Controller
public class CartController {

	@Value("${COOKIE_CART_EXPIRE}")
	private Integer COOKIE_CART_EXPIRE;
	@Autowired
	private ItemService itemService;
	@Autowired
	private CartService cartService;
	
	@RequestMapping("/cart/add/{itemId}")
	public String addCart(@PathVariable Long itemId,
			@RequestParam(defaultValue="1")Integer num,HttpServletRequest request,HttpServletResponse response) {
		//判断用户是否登录，如果登录，则把购物车列表放入redis中，没有则放入cookie中
		TbUser user = (TbUser) request.getAttribute("user");
		if(user != null) {
			cartService.addCart(user.getId(), itemId, num);
			return "cartSuccess";
		}
		
		List<TbItem> cartList = getCartListFromCookie(request);
		boolean flag = false;
		for (TbItem tbItem : cartList) {
			if(tbItem.getId() == itemId.longValue()){
				flag = true;
				tbItem.setNum(tbItem.getNum() + num);
				break;
				}
			}
		if(!flag) {
			TbItem tbItem = itemService.getItemById(itemId);
			tbItem.setNum(num);
			String image = tbItem.getImage();
			if(StringUtils.isNoneBlank(image)) {
				tbItem.setImage(image.split(",")[0]);
			}
			cartList.add(tbItem);
		}
		CookieUtils.setCookie(request, response, "cart", JsonUtils.objectToJson(cartList), COOKIE_CART_EXPIRE, true);
		return "cartSuccess";
	}
	
	/**
	 * 从cookie中取购物车列表
	 * @param request
	 * @return
	 */
	private List<TbItem> getCartListFromCookie(HttpServletRequest request){
		String json = CookieUtils.getCookieValue(request, "cart", true);
		if(StringUtils.isBlank(json)) {
			return new ArrayList<>();
		}
		List<TbItem> list = JsonUtils.jsonToList(json, TbItem.class);
		return list;
	}
	/**
	 * 显示购物车列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/cart/cart")
	public String showCatList(HttpServletRequest request,HttpServletResponse response) {
		List<TbItem> cartList = getCartListFromCookie(request);
		//判断用户是否为登录状态
		TbUser user = (TbUser) request.getAttribute("user");
		if(user != null) {
			cartService.mergeCart(user.getId(), cartList);
			CookieUtils.deleteCookie(request, response, "cart");
			cartList = cartService.getCartList(user.getId());
		}
	
		request.setAttribute("cartList", cartList);
		return "cart";
	}
	
	/**
	 * 更新购物车商品数量
	 */
	@RequestMapping("/cart/update/num/{itemId}/{num}")
	@ResponseBody
	public E3Result updateCartNum(@PathVariable Long itemId, @PathVariable Integer num,
			HttpServletRequest request, HttpServletResponse response) {
		TbUser user = (TbUser) request.getAttribute("user");
		if(user != null) {
			cartService.updateCartNum(user.getId(), itemId, num);
			return E3Result.ok();
		}
		List<TbItem> cartList = getCartListFromCookie(request);
		for (TbItem tbItem : cartList) {
			if(tbItem.getId().longValue() == itemId) {
				tbItem.setNum(num);
				break;
			}
		}
		CookieUtils.setCookie(request, response, "cart", JsonUtils.objectToJson(cartList), COOKIE_CART_EXPIRE, true);
		return E3Result.ok();
	}
	
	/**
	 * 删除购物车商品
	 */
	@RequestMapping("/cart/delete/{itemId}")
	public String deleteCartItem(@PathVariable Long itemId, HttpServletRequest request,
			HttpServletResponse response) {
		TbUser user = (TbUser) request.getAttribute("user");
		if(user != null) {
			cartService.deleteCartNum(user.getId(), itemId);
			return "redirect:/cart/cart.html";
		}
		List<TbItem> cartList = getCartListFromCookie(request);
		for (TbItem tbItem : cartList) {
			if(tbItem.getId().longValue() == itemId) {
				cartList.remove(tbItem);
				break;
			}
		}
		CookieUtils.setCookie(request, response, "cart", JsonUtils.objectToJson(cartList), COOKIE_CART_EXPIRE, true);
		return "redirect:/cart/cart.html";
	}
}
