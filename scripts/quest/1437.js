/**
 * Quest Name: [Job Advancement] Priest
 * Description: Priest 3rd Job Advancement
 * NPC: Robeira
 */
var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
	}
	if (status == 0) {
		qm.warp(211040401);
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
    if (mode == -1) {
		qm.dispose();
    } else {
		if (mode == 1) {
			status++;
		} else {
			if (status == 1) {
				qm.sendOk("I'll be here waiting.");
				qm.dispose();
			}
			status--;
		}
	}
	if (status == 0) {
		if (!qm.haveItem(4031059)) {
			qm.sendOk("Come back to me when you have received a #i4031059");
			qm.dispose();
		} else {
			qm.sendNext("Did you meet #bthe other Grendel the Really Old#k? The #bHoly Stone#k is more powerful than I thought, if it allows you to fight a doppelganger in another dimension. You should thank #bGrendel the Really Old#k... he prepared all of this just for you...");
		}
	} else if (status == 1) {
		qm.sendYesNo("By fighting against a true Magician, you have proven your talents with magic. The only thing left is the Job Advancement to Priest. Are you ready?");
	} else if (status == 2) {
		qm.getPlayer().changeJob(231);
		qm.gainItem(4031059, -1);
		qm.gainAp(5);
		qm.forceCompleteQuest();
		qm.sendOk("You are now a #bPriest#k, a master of #bhealing and holy power#k. As a true Priest, show the world your strength!");
		qm.dispose();
	}
}