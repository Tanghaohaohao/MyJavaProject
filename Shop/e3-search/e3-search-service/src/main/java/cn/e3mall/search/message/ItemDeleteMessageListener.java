package cn.e3mall.search.message;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;

import cn.e3mall.common.pojo.SearchItem;
import cn.e3mall.search.mapper.ItemMapper;

public class ItemDeleteMessageListener implements MessageListener{

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
			solrServer.deleteByQuery("id:"+itemId);
			//提交
			solrServer.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
