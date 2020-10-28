package br.com.meslin.lac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
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

public class HelloGroupDefinerOddReceiver implements NodeConnectionListener, GroupMembershipListener {
  private final static String        gatewayIP    = "172.16.10.130";
  private final static int           gatewayPort  = 5500;
  private GroupCommunicationManager  groupManager;

  public HelloGroupDefinerOddReceiver() {
    UUID uuid = UUID.randomUUID();

    while (uuid.getLeastSignificantBits() % 2 == 0) {
      uuid = UUID.randomUUID();
    }
    InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
    try {
      MrUdpNodeConnection connection = new MrUdpNodeConnection(uuid);
      connection.connect(address);
      connection.addNodeConnectionListener(this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Logger.getLogger("").setLevel(Level.OFF);
    new HelloGroupDefinerOddReceiver();
  }

  @Override
  public void connected(NodeConnection remoteCon) {
    groupManager = new GroupCommunicationManager(remoteCon);
    groupManager.addMembershipListener(this);

    try {
      ApplicationMessage message = new ApplicationMessage();
      String serializableContent = "Bogus Message for Group Definer";
      message.setContentObject(serializableContent);
      
      remoteCon.sendMessage(message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}

  @Override
  public void disconnected(NodeConnection remoteCon) {}

  @Override
  public void newMessageReceived(NodeConnection remoteCon, Message message) {
    System.out.println(message.getContentObject());
  }

  @Override
  public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}

  @Override
  public void internalException(NodeConnection remoteCon, Exception e) {}

  @Override
  public void enteringGroups(List<Group> groups) {}

  @Override
  public void leavingGroups(List<Group> groups) {}
}