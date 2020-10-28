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
import lac.cnclib.sddl.message.Message;

public class HelloGroupPeer implements NodeConnectionListener, GroupMembershipListener {

  private static String              gatewayIP    = "127.0.0.1";
  private static int                 gatewayPort  = 5500;
  private GroupCommunicationManager  groupManager;
  private Group                      aGroup;

  public HelloGroupPeer() {
    InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
    try {
      MrUdpNodeConnection connection = new MrUdpNodeConnection();
      connection.connect(address);
      connection.addNodeConnectionListener(this);

      aGroup = new Group(250, 300);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Logger.getLogger("").setLevel(Level.OFF);

    new HelloGroupPeer();
  }

  @Override
  public void connected(NodeConnection remoteCon) {
    groupManager = new GroupCommunicationManager(remoteCon);
    groupManager.addMembershipListener(this);

    try {
      groupManager.joinGroup(aGroup);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}

  @Override
  public void disconnected(NodeConnection remoteCon) {}

  @Override
  public void newMessageReceived(NodeConnection remoteCon, Message message) {
    System.out.println("Group Peer received: " + message.getContentObject());
  }

  @Override
  public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}

  @Override
  public void internalException(NodeConnection remoteCon, Exception e) {}

  @Override
  public void enteringGroups(List<Group> groups) {
    System.out.println("Entered in the group");
  }

  @Override
  public void leavingGroups(List<Group> groups) {}
}