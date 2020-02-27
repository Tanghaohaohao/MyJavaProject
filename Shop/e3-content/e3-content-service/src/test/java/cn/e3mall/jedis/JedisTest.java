package cn.e3mall.jedis;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisTest {

	@Test
	public void testJedis() throws Exception{
		Jedis jedis = new Jedis("192.168.25.128", 6379);
		jedis.set("test123", "my first jedis test");
		String string =jedis.get("test123");
		System.out.println(string);
		jedis.close();
	}
	
	@Test
	public void testJedisPool() throws Exception{
		JedisPool jedisPool = new JedisPool("192.168.25.128", 6379);
		Jedis jedis = jedisPool.getResource();
		String string = jedis.get("test123");
		System.out.println(string);
		jedis.close();
		jedisPool.close();
	}
}
