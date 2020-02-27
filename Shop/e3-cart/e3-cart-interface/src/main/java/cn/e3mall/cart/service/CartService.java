package cn.e3mall.cart.service;

import java.util.List;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbItem;

public interface CartService {

	E3Result addCart(long usrId, long itemId, int num);
	E3Result mergeCart(long userId,List<TbItem> itemList);
	List<TbItem> getCartList(long userId); 
	E3Result updateCartNum(long userId, long ItemId, int num);
	E3Result deleteCartNum(long userId, long ItemId);
	E3Result clearCartItem(long userId);
}
