package cn.e3mall.sso.service;
/**
 * 根据token查询用户信息
 * @author tangmaoqin
 *
 */

import cn.e3mall.common.utils.E3Result;

public interface TokenService {

	E3Result getUserByToken(String token); 
}
