/**
 * Quest Name: Path of Ice and Lightning
 * Description: Ice/Lightning Wizard's job advancement.
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
			if (status == 4) {
				qm.sendOk("Not quite ready? I'll be waiting.");
				qm.dispose();
			}
			status--;
		}

		if (status == 0) {
			qm.sendNext("An Ice/Lightning Wizard is, as you might have guessed, a Magician who specializes in ice and lightning elemental magic.");
		} else if (status == 1) {
			qm.sendNextPrev("Wizards learn many advanced magic spells. There's #bMP Eater#k, which absorbs enemy MP, #bMeditation#k, which increases your party's magic powers, and #bSpell Mastery#k and #bHigh Wisdom#k which are fundamental for learning more powerful spells.");
		} else if (status == 2) {
			qm.sendNextPrev("Ice/Lightning Wizards rely on two main damage spells. #bCold Beam#k shoots out a powerful stream of ice. #bThunder Bolt#k creates a field that hits up to 6 enemies with powerful lightning.");
		} else if (status == 3) {
			qm.sendNextPrev("Of course, the fun of magic is in the using of it. If you're interested in becoming a Ice/Lightning Wizard, I have a test for you.");
		} else if (status == 4) {
			qm.sendAcceptDecline("The test is simple... Eliminate the monsters at the test site and return with #r30 Dark Marbles#k. If you're ready, I'll send you to the test site now.");
		} else if (status == 5) {
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
			qm.sendAcceptDecline("These are the #t4031013#s... I knew you could do it. Are you prepared to become an Ice/Lightning Wizard?");
		} else if (status == 1) {
			qm.removeAll(4031013);
			qm.changeJob(220);
			qm.forceCompleteQuest();
			qm.sendOk("From this day forth, you are an #bIce/Lightning Wizard#k! Shock and freeze evil until it can fight no more!");
		} else if (status == 2) {
			qm.dispose();
		}
	}
}