package br.com.meslin.lac;

import lac.cnet.groupdefiner.components.GroupDefiner;
import lac.cnet.groupdefiner.components.groupselector.GroupSelector;

public class HelloGroupDefiner {
  public static void main(String[] args) {
    GroupSelector groupSelector = new HelloGroupSelector();
    new GroupDefiner(groupSelector);
    System.out.println("GroupDefiner ready!");

    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
