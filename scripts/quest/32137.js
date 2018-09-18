/**
 * Quest Name: [Theme Dungeon] Ellinel Fairy Academy
 * Description: Starts the Ellinel Fairy Academy questline.
 * Start NPC: Grendel the Really Old
 * End NPC: Fanzy
 * Author: Richard
 */
var status = -1;

function start(mode, type, selection) {
	if (mode == 1) {
		status++;
	} else {
		if (status == 0)  {
			qm.sendOk("I was hoping you would say yes...");
			qm.dispose();
		} else if (status == 3) {
			qm.sendOk("If you insist.... You'll find Fanzy in the #bEllinia North Forest#k, perched on the Giant Tree. Please hurry.");
			status += 3;
		}
		status--;
	}
	if (status == 0) {
		qm.sendAcceptDecline("Right on time. I've received some disturbing news...");
    } else if (status == 1) {	   
        qm.sendNext("There's been an incident at the #bEllinel Fairy Academy#k. Unlike Ellinia, Ellinel has been a sacred place for fairies to live and learn uninterrupted by the outside world. However, a #rhuman Magician#k has trespassed on their territory.");
    } else if (status == 2) {
        qm.sendNextPrev("I don't know all the details, but I know our relationship with the fairies is strained enough as it is. Will you go to the North Forest near Ellinia and meet with #bFanzy#k?");
	} else if (status == 3) {	
	    qm.sendYesNo("If you'd rather, I can send you directly to Fanzy. It might save you a search.");
	} else if (status == 4) { // If you agree to being warped directly to Fanzy.
		qm.warp(101030000,0);
		qm.forceStartQuest();
		qm.dispose();
	} else if (status == 5) { // If you decline to being warped directly to Fanzy.
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendNext("Are you the one I invited to help with the ruckus at the Ellinel Fairy Academy?");
	} else if (status == 1) {
		qm.sendNextPrevS("Um, of course?", 3);
	} else if (status == 2) {
		qm.sendNextPrev("You don't look as strong as I'd hoped. But, you're famous, so I'll leave it to you.");
	} else if (status == 3) {
		qm.forceCompleteQuest();
		//qm.updateQuest(32137, 2);
		qm.startQuest(32101);
		qm.dispose();
	}
}
