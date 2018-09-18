/**
 * Quest Name: Path of the Cleric
 * Description: Cleric's job advancement.
 * NPC: Grendel the Really Old
 */
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
		qm.dispose();
    } else {
		if (mode == 1) {
			status++;
		} else {
			if (status == 5) {
				qm.sendOk("Not quite ready? I'll be waiting.");
				qm.dispose();
			}
			status--;
		}
	
		if (status == 0) {
			qm.sendNext("You seek to follow the path of the Cleric? Unlike the other two Magician paths, Clerics specialize in #brecovery#k and #bsupport#k magic.");
		} else if (status == 1) {
			qm.sendNextPrev("Of course, Clerics have the fundamentals of a Magician. These include #bMP Eater#k, which absorbs enemy MP, and #bSpell Mastery#k and #bHigh Wisdom#k, which are fundamental for learning more powerful spells. Clerics also have a skill called #bInvincible#k, which decreases damage received.");
		} else if (status == 2) {
			qm.sendNextPrev("The signature Cleric spell is #bHeal#k. This amazing spell heals not only your own HP, but your whole party's. And since you can also #bBless#k your party to increase their stats, you'll never have trouble finding others to adventure with.");
		} else if (status == 3) {
			qm.sendNextPrev("Of course, Clerics are not without attack spells. You can attack multiple enemies with #bHoly Arrow#k. It is particularly effective against undead and devil-type monsters. They can heal party members, as well.");
		} else if (status == 4) {
			qm.sendNextPrev("Of course, the fun of magic is in the using of it. If you're interested in becoming a Cleric, I have a test for you.");
		} else if (status == 5) {
			qm.sendAcceptDecline("The test is simple... Eliminate the monsters at the test site and return with #r30 Dark Marbles#k. If you're ready, I'll send you to the test site now.");
		} else if (status == 6) {
			qm.forceStartQuest();
			if (!qm.haveItem(4031013, 30)) {
					qm.warp(910140000); // mage test
					qm.dispose();
			} else {
				qm.dispose();
			}
		}
	}
}

function end(mode, type, selection) {
    if (mode == -1) {
		qm.dispose();
    } else {
		if (mode == 1) {
			status++;
		} else {
			if (status == 0) {
				qm.sendOk("Very well. I'll be here.");
				qm.dispose();
			}
			status--;
		}
	if (status == 0) {
		qm.sendAcceptDecline("These are the #t4031013#s... I knew you could do it. Are you prepared to become a Cleric?");
	} else if (status == 1) {
		qm.removeAll(4031013);
		qm.changeJob(230);
		qm.forceCompleteQuest();
		qm.sendOk("From this day forth, you are a #bCleric#k! Go forth and do good in the world!");
	} else if (status == 2) {
            qm.dispose();
	    }
	}
}