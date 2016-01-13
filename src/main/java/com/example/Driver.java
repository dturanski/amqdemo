package com.example;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.ProducerCallback;
import org.springframework.util.StopWatch;

public class Driver {
	public static void main(String[] args) {
		StopWatch watch = new StopWatch();
		if (args.length !=3 ){
			System.out.println("Usage: " + Driver.class.getName()+ " brokerURL(host:port) queue payload");
			System.exit(1);
		}
		
		final String BROKER_URL = "tcp://" + args[0];
		final String QUEUE_NAME = args[1];
		final String PAYLOAD = args[2];
		
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(BROKER_URL);
		//cf.setMaxThreadPoolSize(20);
		//cf.setUseAsyncSend(true);
		//cf.setAlwaysSyncSend(false);
		//cf.setAlwaysSessionAsync(true);
		//cf.setCopyMessageOnSend(false);
		//cf.setOptimizeAcknowledge(true);
		
		CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
		JmsTemplate template = new JmsTemplate(ccf);
		
		template.setDefaultDestinationName(QUEUE_NAME);
		
		//template.setExplicitQosEnabled(true);
		//template.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		//template.setDeliveryPersistent(false);
		//template.setSessionTransacted(false);
		
		watch.start();
		template.convertAndSend("START");
		template.execute(new ProducerCallback<Boolean>() {
			@Override
			public Boolean doInJms(Session session, MessageProducer producer) throws JMSException {
				TextMessage message = session.createTextMessage(PAYLOAD);
				for (int i = 0; i < 1000; i++) {
					producer.send(message);
				}
				return true;
			}
		});
		template.convertAndSend("END");
		watch.stop();
		System.out.println("Time to send messages: " + watch.getTotalTimeSeconds() + " sec");
		System.exit(0);
	}
}
