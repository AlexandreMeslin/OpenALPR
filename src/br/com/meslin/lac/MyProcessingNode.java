/**
 * 
 */
package br.com.meslin.lac;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;

/**
 * @author meslin
 *
 */
public class MyProcessingNode implements UDIDataReaderListener<ApplicationObject> {
	private SddlLayer core;

	/**
	 * 
	 */
	public MyProcessingNode() {
		core = UniversalDDSLayerFactory.getInstance();
		core.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);

		core.createPublisher();
		core.createSubscriber();

		Object receiveMessageTopic = core.createTopic(Message.class, Message.class.getSimpleName());
		core.createDataReader(this, receiveMessageTopic);

		Object toMobileNodeTopic = core.createTopic(PrivateMessage.class, PrivateMessage.class.getSimpleName());
		core.createDataWriter(toMobileNodeTopic);

		System.out.println("=== Server Started (Listening) ===");
		synchronized(this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MyProcessingNode();
		while (true);
	}

	@Override
	public void onNewData(ApplicationObject topicSample) {
		if(topicSample instanceof Message) {
			Message msg = (Message) topicSample;
			String content = new String(msg.getContent());
			JSONParser parser = new JSONParser();

			try {
				JSONObject object = (JSONObject) parser.parse(content);
				String tag = (String) object.get("tag");

				switch (tag) {
				case "SensorData":
					handleSensorData(object);
					break;

				case "EventData":
					final String label = (String) object.get("label");
					final String data = (String) object.get("data");
					handleEvent(label, data);
					break;

				case "ReplyData":
				case "ErrorData":
					handleMessage(tag, object);
					break;
				}
			} catch(Exception e) {
				System.out.println(msg.getSenderId().toString() +  " Mensagem sem tag (pode ter sido um keep-alive): " + e.getMessage());
			}
		}
	}

	private void handleMessage(String tag, JSONObject object) {
		System.out.println("handleMessage: " + tag + "(" + object.toString() + ")");
	}

	private void handleEvent(String label, String data) {
		System.out.println("handleEvent: " + data);
	}

	private void handleSensorData(JSONObject object) {
		System.out.println("handleSensorData: " + object.toString());
	}
}
