package cn.e3mall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentService;
import cn.e3mall.pojo.TbContent;

/**
 * 内容管理
 * @author tangmaoqin
 *
 */
@Controller
public class ContentController {

	@Autowired
	private ContentService contentService;
	
	@RequestMapping(value="/content/save",method=RequestMethod.POST)
	@ResponseBody
	public E3Result addContent(TbContent content) {
		E3Result e3Result = contentService.addContent(content);
		return e3Result;
	}
	
	@RequestMapping("/content/query/list")
	@ResponseBody
	public EasyUIDataGridResult getContentList(Long categoryId, Integer page,Integer rows) {
		EasyUIDataGridResult result = contentService.getContentList(categoryId, page, rows);
		return result;
	}
	
	@RequestMapping("/rest/content/edit")
	@ResponseBody
	public E3Result editContent(TbContent content) {
		E3Result e3Result = contentService.editContent(content);
		return e3Result;
	}
	
	@RequestMapping("/content/delete")
	@ResponseBody
	public E3Result deleteContent(long[] ids) {
		E3Result e3Result = contentService.deleteContent(ids);
		return e3Result;
	}
}
