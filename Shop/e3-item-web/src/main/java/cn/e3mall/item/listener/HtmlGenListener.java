package cn.e3mall.item.listener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import cn.e3mall.item.pojo.Item;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.service.ItemService;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
/**
 * 监听商品添加消息
 * @author tangmaoqin
 *
 */
public class HtmlGenListener implements MessageListener {

	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
	@Autowired
	private ItemService itemService;
	@Value("${HTML_GEN_PATH}")
	private String HTML_GEN_PATH;
	@Override
	public void onMessage(Message message) {
		try {
			TextMessage textMessage = (TextMessage) message;
			String text = textMessage.getText();
			Long itemId = new Long(text);
			//等待事务提交
			Thread.sleep(1000);
			TbItem tbItem = itemService.getItemById(itemId);
			Item item = new Item(tbItem);
			TbItemDesc itemDesc = itemService.geTbItemDescById(itemId);
			Map data = new HashMap<>();
			data.put("item", item);
			data.put("itemDesc", itemDesc);
			Configuration configuration = freeMarkerConfigurer.getConfiguration();
			Template template = configuration.getTemplate("item.ftl");
			Writer out = new FileWriter(HTML_GEN_PATH+itemId+".html");
			template.process(data, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
