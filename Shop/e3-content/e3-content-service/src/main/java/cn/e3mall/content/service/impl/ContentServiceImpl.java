package cn.e3mall.content.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.content.service.ContentService;
import cn.e3mall.mapper.TbContentMapper;
import cn.e3mall.pojo.TbContent;
import cn.e3mall.pojo.TbContentExample;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.pojo.TbContentExample.Criteria;
/**
 * 内容 管理
 * @author tangmaoqin
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private JedisClient JedisClient;
	
	@Value("${CONTENT_LIST}")
	private String CONTENT_LIST;
	
	@Override
	public E3Result addContent(TbContent content) {
		content.setCreated(new Date());
		content.setUpdated(new Date());
		contentMapper.insert(content);
		//缓存同步，删除缓存中对于的数据
		JedisClient.hdel(CONTENT_LIST, content.getCategoryId().toString());
		return E3Result.ok();
	}
	@Override
	public List<TbContent> getContentListByCid(long cid) {
		 //查询缓存
		try {
			String json = JedisClient.hget(CONTENT_LIST, cid+"");
			if(StringUtils.isNoneBlank(json)) {
				List<TbContent> list = JsonUtils.jsonToList(json, TbContent.class);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(cid);
		List<TbContent> list = contentMapper.selectByExampleWithBLOBs(example);
		//把结果添加到缓存
       try {
			JedisClient.hset(CONTENT_LIST, cid+"", JsonUtils.objectToJson(list));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	@Override
	public EasyUIDataGridResult getContentList(Long categoryId, Integer page, Integer rows) {
		PageHelper.startPage(page, rows);
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		List<TbContent> list = contentMapper.selectByExample(example);
		PageInfo<TbContent> pageInfo = new PageInfo<>(list);
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setTotal(pageInfo.getTotal());
		result.setRows(list);
		return result;
	}
	@Override
	public E3Result editContent(TbContent content) {
		content.setUpdated(new Date());
		contentMapper.updateByPrimaryKey(content);
		//缓存同步，删除缓存中对于的数据
		JedisClient.hdel(CONTENT_LIST, content.getCategoryId().toString());
		return E3Result.ok();
	}
	@Override
	public E3Result deleteContent(long[] ids) {
		for (long id : ids) {
			TbContent content = contentMapper.selectByPrimaryKey(id);
			contentMapper.deleteByPrimaryKey(id);
			//缓存同步，删除缓存中对于的数据
			JedisClient.hdel(CONTENT_LIST, content.getCategoryId().toString());
		}
		return E3Result.ok();
	}

}
