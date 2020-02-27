package cn.e3mall.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.filter.function.regexMatchFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;


import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.pojo.TbItemExample.Criteria;
import cn.e3mall.service.ItemService;
/**
 * 商品管理setvice
 * @author tangmaoqin
 *
 */
@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemDescMapper itemDescMapper;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Resource
	private Destination topicDestination;
	@Resource
	private Destination topicEditDestination;
	@Resource
	private Destination topicDeleteDestination;
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${REDIS_ITEM_PRE}")
	private String REDIS_ITEM_PRE;
	@Value("${ITEM_CACHE_EXPIRE}")
	private Integer ITEM_CACHE_EXPIRE;
	
	@Override
	public TbItem getItemById(long itemId) {
		//查询缓存
		try {
			String json = jedisClient.get(REDIS_ITEM_PRE+":"+itemId+":BASE");
			if(StringUtils.isNotBlank(json)) {
				TbItem tbItem = JsonUtils.jsonToPojo(json, TbItem.class);
				return tbItem;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andIdEqualTo(itemId);
		List<TbItem> list = itemMapper.selectByExample(example);
		if(list != null && list.size()>0) {
			//添加结果到缓存
			try {
				jedisClient.set(REDIS_ITEM_PRE+":"+itemId+":BASE", JsonUtils.objectToJson(list.get(0)));
				jedisClient.expire(REDIS_ITEM_PRE+":"+itemId+":BASE", ITEM_CACHE_EXPIRE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return list.get(0);
		}
		return null;
	}
	
	@Override
	public EasyUIDataGridResult getItemList(int page, int rows) {
		//设置分页信息
		PageHelper.startPage(page, rows);
		//执行查询
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusNotEqualTo((byte) 3);
		List<TbItem> list = itemMapper.selectByExample(example);
		//取分页信息
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);
				
		//创建返回结果对象
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setTotal(pageInfo.getTotal());
		result.setRows(list);

		return result;

	}
	@Override
	public E3Result addItem(TbItem item, String desc) {
		final long itemId = IDUtils.genItemId();
		item.setId(itemId);
		item.setStatus((byte) 1);
		item.setCreated(new Date());
		item.setUpdated(new Date());
		itemMapper.insert(item);
		TbItemDesc itemDesc = new TbItemDesc();
		itemDesc.setItemId(itemId);
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(new Date());
	    itemDesc.setUpdated(new Date());
	    itemDescMapper.insert(itemDesc);
	    //发生商品添加消息
	  	jmsTemplate.send(topicDestination, new MessageCreator() {
	  			
	  	@Override
	  	public Message createMessage(Session session) throws JMSException {
	  			TextMessage textMessage = session.createTextMessage(itemId+"");
	  			return textMessage;
	  		}
	  	});
		return E3Result.ok();
	}
	@Override
	public E3Result editItem(TbItem item, String desc) {
		Date created = getItemById(item.getId()).getCreated();
		item.setStatus((byte) 1);
		item.setCreated(created);
		item.setUpdated(new Date());
		final long itemId = item.getId();
		itemMapper.updateByPrimaryKey(item);
		TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(item.getId());
		Date createdDesc = itemDesc.getCreated();
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(createdDesc);
		itemDesc.setUpdated(new Date());
		itemDescMapper.updateByPrimaryKeyWithBLOBs(itemDesc);
		//缓存同步
		try {
			jedisClient.del(REDIS_ITEM_PRE+":"+itemId+":BASE");
			jedisClient.del(REDIS_ITEM_PRE+":"+itemId+":DESC");
		} catch (Exception e) {
			
		}
		//发送商品添加消息
	  	jmsTemplate.send(topicEditDestination, new MessageCreator() {
	  			
	  	@Override
	  	public Message createMessage(Session session) throws JMSException {
	  			TextMessage textMessage = session.createTextMessage(itemId+"");
	  			return textMessage;
	  		}
	  	});
		return E3Result.ok();
	}

	@Override
	public E3Result deleteItem(long[] ids) {
		for (final long id : ids) {
			TbItem item = itemMapper.selectByPrimaryKey(id);
			item.setStatus((byte) 3);
			itemMapper.updateByPrimaryKey(item);
			//缓存同步
			try {
				jedisClient.del(REDIS_ITEM_PRE+":"+id+":BASE");
				jedisClient.del(REDIS_ITEM_PRE+":"+id+":DESC");
			} catch (Exception e) {		
			}
			//发生商品删除消息
		  	jmsTemplate.send(topicDeleteDestination, new MessageCreator() {
		  			
		  	@Override
		  	public Message createMessage(Session session) throws JMSException {
		  			TextMessage textMessage = session.createTextMessage(id+"");
		  			return textMessage;
		  		}
		  	});
		}
		return E3Result.ok();
	}

	@Override
	public E3Result downItem(long[] ids) {
		for (long id : ids) {
			TbItem item = itemMapper.selectByPrimaryKey(id);
			item.setStatus((byte) 2);
			itemMapper.updateByPrimaryKey(item);
			//缓存同步
			try {
				jedisClient.del(REDIS_ITEM_PRE+":"+id+":BASE");
				jedisClient.del(REDIS_ITEM_PRE+":"+id+":DESC");
			} catch (Exception e) {		
			}
		}
		return E3Result.ok();
	}

	@Override
	public E3Result upItem(long[] ids) {
		for (long id : ids) {
			TbItem item = itemMapper.selectByPrimaryKey(id);
			item.setStatus((byte) 1);
			itemMapper.updateByPrimaryKey(item);
			//缓存同步
			try {
				jedisClient.del(REDIS_ITEM_PRE+":"+id+":BASE");
				jedisClient.del(REDIS_ITEM_PRE+":"+id+":DESC");
			} catch (Exception e) {		
			}
		}
		return E3Result.ok();
	}

	@Override
	public TbItemDesc geTbItemDescById(long itemId) {
		//查询缓存
		try {
			String json = jedisClient.get(REDIS_ITEM_PRE+":"+itemId+":DESC");
			if(StringUtils.isNotBlank(json)) {
				TbItemDesc tbItemDesc = JsonUtils.jsonToPojo(json, TbItemDesc.class);
				return tbItemDesc;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(itemId);
		try {
			jedisClient.set(REDIS_ITEM_PRE+":"+itemId+":DESC", JsonUtils.objectToJson(itemDesc));
			jedisClient.expire(REDIS_ITEM_PRE+":"+itemId+":DESC", ITEM_CACHE_EXPIRE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemDesc;
	}
	

}
