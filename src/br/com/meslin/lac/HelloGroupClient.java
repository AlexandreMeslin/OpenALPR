package br.com.meslin.lac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.groups.Group;
import lac.cnclib.net.groups.GroupCommunicationManager;
import lac.cnclib.net.groups.GroupMembershipListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;

public class HelloGroupClient implements NodeConnectionListener,
		GroupMembershipListener {
	// private final static String gatewayIP = "127.0.0.1";
	private final static String gatewayIP = "172.16.10.130";
	private final static int gatewayPort = 5500;
	private GroupCommunicationManager groupManager;

	public HelloGroupClient() {
		System.err.println("HelloGroupClient");
		InetSocketAddress address = new InetSocketAddress(gatewayIP,
				gatewayPort);
		try {
			MrUdpNodeConnection connection = new MrUdpNodeConnection();
			connection.connect(address);
			connection.addNodeConnectionListener(this);

			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.err.println("main");
		Logger.getLogger("").setLevel(Level.OFF);

		new HelloGroupClient();
	}

	@Override
	public void connected(NodeConnection remoteCon) {
		System.err.println("connected");
		groupManager = new GroupCommunicationManager(remoteCon);
		groupManager.addMembershipListener(this);

		try {
			// i = 1 Default
			// i = 2 EVEN
			// i = 3 ODD
			for (int i = 1; i < 4; i++) {
				Group group = new Group(3, i);

				ApplicationMessage appMsg = new ApplicationMessage();
				System.err.println("Message GroupID: " + i);
				appMsg.setContentObject("Message GroupID: " + i);
				groupManager.sendGroupcastMessage(appMsg, group);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reconnected(NodeConnection remoteCon, SocketAddress endPoint,
			boolean wasHandover, boolean wasMandatory) {
	}

	@Override
	public void disconnected(NodeConnection remoteCon) {
	}

	@Override
	public void newMessageReceived(NodeConnection remoteCon, Message message) {
		System.out.println(message.getContentObject());
	}

	@Override
	public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {
	}

	@Override
	public void internalException(NodeConnection remoteCon, Exception e) {
	}

	@Override
	public void enteringGroups(List<Group> groups) {
	}

	@Override
	public void leavingGroups(List<Group> groups) {
	}
}
