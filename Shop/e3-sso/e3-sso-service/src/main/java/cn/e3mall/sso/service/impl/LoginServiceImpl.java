package cn.e3mall.sso.service.impl;

import java.util.List;
import java.util.UUID;

import org.apache.zookeeper.Op.Create;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbUserMapper;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.pojo.TbUserExample;
import cn.e3mall.pojo.TbUserExample.Criteria;
import cn.e3mall.sso.service.LoginService;
/**
 * 用户登录处理
 * @author tangmaoqin
 *
 */
@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private TbUserMapper userMapper;
	@Autowired
	private JedisClient jedisClient;
	@Value("${SESSION_EXPIRE}")
	private Integer SESSION_EXPIRE;
	@Override
	public E3Result userLogin(String username, String password) {
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		List<TbUser> list = userMapper.selectByExample(example);
		if(list == null || list.size() == 0) {
			return E3Result.build(400, "用户名或者密码错误");
		}
		TbUser user = list.get(0);
		if(!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
			return E3Result.build(400, "用户名或者密码错误");
		}
		String token = UUID.randomUUID().toString();
		user.setPassword(null);
		jedisClient.set("SESSION:"+token, JsonUtils.objectToJson(user));
		jedisClient.expire("SESSION:"+token, SESSION_EXPIRE);
		return E3Result.ok(token);
	}

}
