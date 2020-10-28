/**
 * 
 */
package br.com.meslin.lac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;
import lac.cnclib.sddl.message.Message;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SendMEPAQuery implements NodeConnectionListener {
	private MrUdpNodeConnection connection;
	private String gatewayIP = "127.0.0.1";
	private int gatewayPort = 5500;

	public SendMEPAQuery() {
		InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
		UUID myUUID = UUID.randomUUID();
		try {
			connection = new MrUdpNodeConnection(myUUID);
			connection.addNodeConnectionListener(this);
			connection.connect(address);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		SendMEPAQuery mepa = new SendMEPAQuery();
		mepa.doAll();
	}

	private void doAll() {
		String eplString = "select avg(sensorValue[1]) as value "
				+ "from SensorData(sensorName='Temperature').win:time_batch(10 sec) "
				+ "where sensorValue[1] > 20";
		JSONObject mepaQuery =  new JSONObject();
		mepaQuery.put("type", "add");	// add|remove|start|stop|clear|get
		mepaQuery.put("label", "AVGTemp");	// rule|event
		mepaQuery.put("object", "rule");
		mepaQuery.put("rule", eplString);
		mepaQuery.put("target", "local");

		JSONArray jsonMsg = new JSONArray();
		JSONObject oneQuery = new JSONObject();
		oneQuery.put("MEPAQuery", mepaQuery);
		jsonMsg.add(oneQuery);

		ApplicationMessage msg = new ApplicationMessage();
		msg.setRecipientID(UUID.fromString("63c38558-8f1e-4096-bd43-876872072f50"));
		msg.setContentObject(jsonMsg.toString());
		msg.setPayloadType(PayloadSerialization.JSON);
		try {
			connection.sendMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connected(NodeConnection arg0) { }

	@Override
	public void disconnected(NodeConnection arg0) { }

	@Override
	public void internalException(NodeConnection arg0, Exception arg1) { }

	@Override
	public void newMessageReceived(NodeConnection arg0, Message topicSample) { }

	@Override
	public void reconnected(NodeConnection arg0, SocketAddress arg1, boolean arg2, boolean arg3) { }

	@Override
	public void unsentMessages(NodeConnection arg0, List<Message> arg1) { }
}
