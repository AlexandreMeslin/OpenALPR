package br.com.meslin.lac;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lac.cnet.groupdefiner.components.groupselector.GroupSelector;
import lac.cnet.sddl.objects.GroupRegion;
import lac.cnet.sddl.objects.Message;

public class HelloGroupSelector implements GroupSelector {
	@Override
	public int getGroupType() {
		return 3;
	}

	@Override
	public Set<Integer> processGroups(Message nodeMessage) {
		System.out.println("STARTED CLASSIFYING GROUP MESSAGE " + nodeMessage.getSenderId().toString());

		// Add the node to two groups
		HashSet<Integer> groupCollection = new HashSet<Integer>(2, 1);

		// Some default group (ID = 1)
		groupCollection.add(1);

		// Even (2) or Odd Group (3)
		if (nodeMessage.getSenderId().getLeastSignificantBits() % 2 == 0) {
			groupCollection.add(2);
		} else {
			groupCollection.add(3);
		}

		System.out.println("ENDED CLASSIFYING GROUP MESSAGE at" + new Date() + "\n");
		return groupCollection;
	}

	@Override
	public void createGroup(GroupRegion groupRegion) { }
}
