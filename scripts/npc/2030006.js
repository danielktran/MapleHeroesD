/* Holy Stone
	Hidden Street: Holy Ground at the Snowfield (211040401)
	
	Custom quest: 100102
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 1) {
			status++;
		} else {
			if (status == 0) {
				cm.sendOk("#b(I've got a bad feeling about this. I'll do it, but after I prepare...)");
				cm.dispose();
			}
			status--;
		}
	}

    if (status == 0) {
		cm.sendYesNo("#b(A mysterious energy surrounds this stone. The elder definitely told me to touch it... Should I really touch this thing?)");
	} else if (status == 1) {
		cm.warp(910540000);
		cm.dispose();
	}
	/*
	if (cm.getQuestStatus(1440) != 1 || cm.getQuestStatus(1439) != 1) {
	    cm.warp(910540000);
	    cm.dispose();
	} else {
	    cm.sendNext("Lulz, I am a stone.");
	}
    } else if (status == 1) {
	cm.sendNextPrev("Give me a #bDark Crystal#k and I will allow you to obtain the #bNecklace of Wisdom#k.");
    } else if (status == 2) {
	if (!cm.haveItem(4005004)) {
	    cm.sendOk("You don't have any #bDark Crystal#ks.");
	    cm.dispose();
	} else {
	    cm.gainItem(4005004, -1);
	    cm.gainItem(4031058, 1);
	    cm.sendOk("Indeed.");
	    cm.dispose();
	}
	*/
}