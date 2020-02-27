package cn.e3mall.order.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import cn.e3mall.cart.service.CartService;
import cn.e3mall.common.utils.CookieUtils;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.sso.service.TokenService;
/**
 * 用户登录拦截器
 * @author tangmaoqin
 *
 */
public class LoginInterceptor implements HandlerInterceptor {

	@Value("${SSO_URL}")
	private String SSO_URL;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private CartService cartService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String token = CookieUtils.getCookieValue(request, "token");
		if(StringUtils.isBlank(token)) {
			response.sendRedirect(SSO_URL+"/page/login?redirect="+request.getRequestURL());
			return false;
		}
		E3Result e3Result = tokenService.getUserByToken(token);
		if(e3Result.getStatus()!=200) {
			response.sendRedirect(SSO_URL+"/page/login?redirect="+request.getRequestURL());
			return false;
		}
		TbUser user = (TbUser) e3Result.getData();
		request.setAttribute("user", user);
		//判断cookie中有无购物车数据，如果有，合并到服务器
		String jsonCartList = CookieUtils.getCookieValue(request, "cart", true);
		if(StringUtils.isNoneBlank(jsonCartList)) {
			cartService.mergeCart(user.getId(), JsonUtils.jsonToList(jsonCartList, TbItem.class));
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
