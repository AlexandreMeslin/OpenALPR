/**
 * From: http://wiki.lac.inf.puc-rio.br/doku.php?id=hellocore
 * This app was designed to be used with HelloCoreServer. They send and receive a custom data containing a caption string and an image file
 * The HelloCoreClient send a bunch of custom data and calculate the elapsed time
 * This app also includes:
 * 1) It sends a message containing a text string and an image
 * 2) The image source location can be informed at the command line 
 */
package br.com.meslin.main.benchmark.mrudp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.serialization.Serialization;
import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;
import br.com.meslin.lac.CustomData;

public class HelloCoreServer implements UDIDataReaderListener<ApplicationObject> {
	SddlLayer  core;
	int        counter;

	public static void main(String[] args) {
		Logger.getLogger("").setLevel(Level.OFF);

		new HelloCoreServer();
	}

	public HelloCoreServer() {
		core = UniversalDDSLayerFactory.getInstance();
		core.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);
		core.createPublisher();
		core.createSubscriber();

		Object receiveMessageTopic = core.createTopic(Message.class, Message.class.getSimpleName());
		core.createDataReader(this, receiveMessageTopic);

		Object toMobileNodeTopic = core.createTopic(PrivateMessage.class, PrivateMessage.class.getSimpleName());
		core.createDataWriter(toMobileNodeTopic);

		counter = 0;
		System.out.println("=== Server Started (Listening) ===");
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onNewData(ApplicationObject nodeMessage) {
		System.out.println("New data received");
		Message message = (Message) nodeMessage;
		Object mobileObject = null;
		
		try {
			mobileObject = new ObjectInputStream(new ByteArrayInputStream(((Message)nodeMessage).getContent())).readObject();
		}
		catch(ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if(mobileObject instanceof CustomData) {
			ApplicationMessage applicationMessage = new ApplicationMessage();
			String caption = ((CustomData) mobileObject).getCaption();
			PrivateMessage privateMessage = new PrivateMessage();
			privateMessage.setGatewayId(message.getGatewayId());
			privateMessage.setNodeId(message.getSenderId());
			applicationMessage.setContentObject("CustomData" + caption);
			privateMessage.setMessage(Serialization.toProtocolMessage(applicationMessage));
			core.writeTopic(PrivateMessage.class.getSimpleName(), privateMessage);
		}
		else if(mobileObject instanceof String) {
			System.out.println("Recebi string");
		}
		System.out.println(Serialization.fromJavaByteStream(message.getContent()));

		PrivateMessage privateMessage = new PrivateMessage();
		privateMessage.setGatewayId(message.getGatewayId());
		privateMessage.setNodeId(message.getSenderId());

		synchronized (core) {
			counter++;
		}

		ApplicationMessage appMsg = new ApplicationMessage();
		appMsg.setContentObject(counter);
		privateMessage.setMessage(Serialization.toProtocolMessage(appMsg));

		core.writeTopic(PrivateMessage.class.getSimpleName(), privateMessage);
	}
}