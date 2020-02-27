package cn.e3mall.item.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 生成静态页面测试
 * @author tangmaoqin
 *
 */
@Controller
public class HtmlGenController {

	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
	
	@RequestMapping("/genhtml")
	@ResponseBody
	public String genHtml() throws Exception{
		Configuration configuration = freeMarkerConfigurer.getConfiguration();
		Template template = configuration.getTemplate("hello.ftl");
		Map data = new HashMap<>();
		data.put("hello", 123456);
		Writer out = new FileWriter(new File("C:/Users/tangmaoqin/Desktop/student.html"));
		// 调用模板对象的process方法输出文件。
 		template.process(data, out);
		// 关闭流。
		out.close();
		return "ok";
	}
}
