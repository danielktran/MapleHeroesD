/*
    Description: Enter & Exit Ellinel Fairy Academy Theme Dungeon
	NPC: Fanzy
    Author: Richard
*/
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else 
        if (status == 0) {
		    cm.sendNextS("Enjoy your adventure.",5);
            cm.dispose();
        status--;
    }
    if (cm.getMapId() == 101030000) {
        if (status == 0) {
            cm.sendYesNoS("Would you like to enter #b#e[Theme Dungeon Ellinel Fairy Academy]#n#k?", 4);
        } else if (status == 1) {
            cm.warp(101074000);
            cm.dispose();
        }
    } else if (cm.getMapId() == 101070000) {
        if (status == 0) {
            cm.sendYesNoS("Head back to #bNorth Forest: Giant Tree#k?", 4);
        } else if (status == 1) {
            cm.warp(101074001);
            cm.dispose();
        }
    }
}
