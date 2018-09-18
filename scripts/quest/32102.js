/**
 * Quest Name: [Ellinel Fairy Academy] You Can Do It
 * NPC: Fanzy
 * Author: Richard
 */

var status = -1;

function start(mode, type, selection) {
	if (mode == 1)
	    status++;
	 else
	    status--;
	if (status == 0) {
		qm.sendNext("Are you asking where we are? Did you follow me without knowing where I was going? This is the forest path to the #bEllinel Fairy Academy#k.");
	} else if (status == 1) {
		qm.sendNextPrevS("Ellinel Fairy Academy?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("Yes. #bEllinel#k is an academy where fairy children learn magic.");
	} else if (status == 3) {
		qm.sendNextPrevS("But why is it hidden so deep within the forest?", 2);
	} else if (status == 4) {
		qm.sendNextPrev("Did you know that #bEllinia#k used to be a fairy town? Several hundred years ago, after the war with the Black Mage, humans came in and reclaimed the town and it became the #bEllinia#k we know.");
	} else if (status == 5) {
		qm.sendNextPrevS("Then that must mean that fairies live outside of Ellinia, too.");
	} else if (status == 6) {
		qm.sendNextPrev("Some fairies are okay with humans, but others very much are not. It's the same in the #bEllinel Fairy Academy#k. They don't want to mix with humans, and so they disappeared into the forest. That's why the school is far across the lake.");
	} else if (status == 7) {
		qm.sendNextPrevS("You think Cootie got captured by human-hating fairies?", 2);
	} else if (status == 8) {
		qm.sendNextPrev("Most likely. I know I thought about using him as a scratching post a few times. Master #bGrendel#k and I tried to befriend the fairies, but they just weren't listening. I think we should use more... forceful methods. #b#h0##k, let me ask... are you a good swimmer?");
	} else if (status == 9) {
		qm.sendNextPrev("Why don't you go for a swim! Show us how brave you are, meow... #b(Cross the lake to the right.)#k");
	} else if (status == 10) {
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.dispose();
}