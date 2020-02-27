package cn.e3mall.search.message;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;

import cn.e3mall.common.pojo.SearchItem;
import cn.e3mall.search.mapper.ItemMapper;
/**
 * 监听商品添加消息
 * @author tangmaoqin
 *
 */
public class ItemAddMessageListener implements MessageListener {
 
	@Autowired
	private ItemMapper ItemMapper;
	@Autowired
	private SolrServer solrServer;
	
	@Override
	public void onMessage(Message message) {
		try {
			TextMessage textMessage = (TextMessage) message;
			String text = textMessage.getText();
			Long itemId = new Long(text);
			//System.out.println(itemId);
			//等待事务提交
			Thread.sleep(1000);
			SearchItem searchItem = ItemMapper.getItemById(itemId);
			SolrInputDocument document = new SolrInputDocument();
			document.addField("id", searchItem.getId());
			document.addField("item_title", searchItem.getTitle());
			document.addField("item_sell_point", searchItem.getSell_point());
			document.addField("item_price", searchItem.getPrice());
			document.addField("item_image", searchItem.getImage());
			document.addField("item_category_name", searchItem.getCategory_name());
			//把文档对象写入索引库
			solrServer.add(document);
			solrServer.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
