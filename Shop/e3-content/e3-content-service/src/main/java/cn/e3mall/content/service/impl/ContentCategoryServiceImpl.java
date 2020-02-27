package cn.e3mall.content.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentCategoryService;
import cn.e3mall.mapper.TbContentCategoryMapper;
import cn.e3mall.pojo.TbContentCategory;
import cn.e3mall.pojo.TbContentCategoryExample;
import cn.e3mall.pojo.TbContentCategoryExample.Criteria;
/**
 * 内容分类管理
 * @author tangmaoqin
 *
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

	@Autowired
	private TbContentCategoryMapper contentCategoryMapper;
	@Override
	public List<EasyUITreeNode> getContentList(long parentId) {
		TbContentCategoryExample example = new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		criteria.andStatusNotEqualTo(2);
		List<TbContentCategory> catList = contentCategoryMapper.selectByExample(example);
		List<EasyUITreeNode> nodeList = new ArrayList<>();
		for (TbContentCategory tbContentCategory : catList) {
			EasyUITreeNode node = new EasyUITreeNode();
			node.setId(tbContentCategory.getId());
			node.setText(tbContentCategory.getName());
			node.setState(tbContentCategory.getIsParent()?"closed":"open");
			nodeList.add(node);
		}
		return nodeList;
	}
	@Override
	public E3Result addContentCategory(long parentId, String name) {
		TbContentCategory contentCategory = new TbContentCategory();
		contentCategory.setParentId(parentId);
		contentCategory.setName(name);
		//1(正常)2(删除)
		contentCategory.setStatus(1);
		contentCategory.setSortOrder(1);
		contentCategory.setIsParent(false);
		contentCategory.setCreated(new Date());
		contentCategory.setUpdated(new Date());
		contentCategoryMapper.insert(contentCategory);
		TbContentCategory parent = contentCategoryMapper.selectByPrimaryKey(parentId);
		if(!parent.getIsParent()) {
			parent.setIsParent(true);
			contentCategoryMapper.updateByPrimaryKey(parent);
		}
		return E3Result.ok(contentCategory);
	}
	@Override
	public E3Result updateContentCatgory(long id, String name) {
		TbContentCategory contentCategory = contentCategoryMapper.selectByPrimaryKey(id);
		contentCategory.setName(name);
		contentCategory.setUpdated(new Date());
		contentCategoryMapper.updateByPrimaryKey(contentCategory);
		return E3Result.ok(contentCategory);
	}
	@Override
	public E3Result deleteContentCatgory(long id) {
		TbContentCategory contentCategory = contentCategoryMapper.selectByPrimaryKey(id);
		contentCategory.setStatus(2);
		contentCategoryMapper.updateByPrimaryKey(contentCategory);
		long parenrId = contentCategory.getParentId();
		TbContentCategoryExample example = new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parenrId);
		criteria.andStatusEqualTo(1);
		int count = contentCategoryMapper.countByExample(example);
		if(count == 0) {		
			TbContentCategory parContentCategory = contentCategoryMapper.selectByPrimaryKey(parenrId);
			parContentCategory.setIsParent(false);
			contentCategoryMapper.updateByPrimaryKey(parContentCategory);
		}		
		return E3Result.ok();
	}

}
