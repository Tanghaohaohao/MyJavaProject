package cn.e3mall.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.springframework.aop.ThrowsAdvice;

public class ActiveMqTest {

	@Test
	public void testQueueProducer() throws Exception{
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.128:61616");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		//第一个参数：是否开启事务。true：开启事务，第二个参数忽略。
		//第二个参数：当第一个参数为false时，才有意义。消息的应答模式。
		//1、自动应答2、手动应答。一般是自动应答。
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue("test-queue");
		MessageProducer producer = session.createProducer(queue);
		//TextMessage textMessage = new ActiveMQTextMessage();
		//textMessage.setText("hello Activemq");
		TextMessage textMessage = session.createTextMessage("hello activemq");
		producer.send(textMessage);
		producer.close();
		session.close();
		connection.close();
	}
	
	@Test
	public void testQueueConsumer() throws Exception{
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.128:61616");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue("spring-queue");
	    MessageConsumer consumer = session.createConsumer(queue);	
	    consumer.setMessageListener(new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				TextMessage textMessage = (TextMessage) message;
				String text;
				try {
					text = textMessage.getText();
					System.out.print(text);
				} catch (Exception e) {
					e.printStackTrace();
				}		
			}
		});
	    System.in.read();
	    consumer.close();
	    session.close();
	    connection.close();
	 }
	 
	@Test
	public void testTopicProducer() throws Exception{
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.128:61616");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		//第一个参数：是否开启事务。true：开启事务，第二个参数忽略。
		//第二个参数：当第一个参数为false时，才有意义。消息的应答模式。
		//1、自动应答2、手动应答。一般是自动应答。
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic("test-potic");
		MessageProducer producer = session.createProducer(topic);
		//TextMessage textMessage = new ActiveMQTextMessage();
		//textMessage.setText("hello Activemq");
		TextMessage textMessage = session.createTextMessage("topic message");
		producer.send(textMessage);
		producer.close();
		session.close();
		connection.close();
	}
	
	@Test
	public void testTopicConsumer() throws Exception{
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.128:61616");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic("test-potic");
	    MessageConsumer consumer = session.createConsumer(topic);	
	    consumer.setMessageListener(new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				TextMessage textMessage = (TextMessage) message;
				String text;
				try {
					text = textMessage.getText();
					System.out.println(text);
				} catch (Exception e) {
					e.printStackTrace();
				}		
			}
		});
	    System.out.println("topic消费者1启动");
	    System.in.read();
	    consumer.close();
	    session.close();
	    connection.close();
	 } 
}
