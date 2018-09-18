/* guild creation npc */
var status = -1;
var sel;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 0) {
		cm.dispose();
		return;
    }
    if (mode == 1) {
		status++;
	} else {
		if (cm.getPlayerStat("GID") <= 0) {
			if (status == 1) {
				if (sel == 2) {
					cm.sendOk("You're not ready yet? Come back to me when you want to create a guild.");
					cm.dispose();
				}
			}
		} else if (cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
			if (status == 1) {
				if (sel == 1) {
					cm.sendOk("Good thinking. I wouldn't want to break up my guild either...");
					cm.dispose();
				}
			} else if (status == 2) {
				if (sel == 0) {
					cm.dispose();
				}
			}
		}
		status--;
	}

	if (cm.getPlayerStat("GID") <= 0) {
		if (status == 0) {
			cm.sendSimple("Hey...would you happen to be interested in GUILDS by any chance?\r\n#b#L0#What's a guild?#l\r\n#L1#What do I do to form a guild?#l\r\n#L2#I want to start a guild#l#k");
		} else if (status == 1) {
			sel = selection;
			if (selection == 0) {
				cm.sendNext("You can think of a guild as a small crew full of people with similar interests and goals, except it will be officially registered in our Guild Headquarters and be accepted as a valid GUILD.");
			} else if (selection == 1) {
				cm.sendNext("You must be at least Lv. 100 to create a guild.");
			} else if (selection == 2) {
				cm.sendYesNo("Oh! So you're here to register a guild... You need 5,000,000 mesos to register a guild. I trust that you are ready. Would you like to create a guild?");
			}
		} else if (status == 2) {
			if (sel == 0) {
				cm.sendNextPrev("There are a variety of benefits that you can get through guild activities. For example, you can obtain a guild skill or an item that is exclusive to guilds.");
				cm.dispose();
			} else if (sel == 1) {
				cm.sendNextPrev("You also need 5,000,000 mesos. This is the registration fee.");
			} else if (sel == 2) {
				cm.sendNext("Enter the name of your guild, and your guild will be created. The guild will also be officially registered under our Guild Headquarters, so best of luck to you and your guild!");
			}
		} else if (status == 3) {
			if (sel == 1) {
				cm.sendNextPrev("So, come see me if you would like to register a guild! Oh, and of course you can't be already registered to another guild!");
				cm.dispose();
			}  else if (sel == 2) {
				cm.genericGuildMessage(3);
				cm.dispose();
			}
		}
	} else if (cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
		if (status == 0) {
			cm.sendSimple("Now, how can I help you?\r\n#b#L0#I want to expand my guild#l\r\n#L1#I want to break up my guild#l#k");
		} else if (status == 1) {
			sel = selection;
			if (selection == 0) {
				cm.sendNext("Are you here because you want to expand your guild? To increase the number of people you can accept into your guild, you'll have to re-register. You'll also have to pay a fee. Just so you know, the absolute maximum size of a guild is 200 members.");
			} else if (selection == 1) {
				cm.sendYesNo("Are you sure you want to break up your guild? Remember, once you break up your guild, it will be gone forever. Are you sure you still want to do it?");
			}
		} else if (status == 2) {
			if (sel == 0) {
				cm.sendYesNo("Current Max Guild Members: #b" + cm.getGuildCapacity() + "#k characters. To increase that amount by #b10#k, you need #b10000 GP#k. Your guild has #b" + cm.getGP() + " GP#k right now. Do you want to expand your guild?");
			} else if (sel == 1) {
				cm.disbandGuild();
				cm.dispose();
			}
		} else if (status == 3) {
			if (sel == 0) {
				cm.increaseGuildCapacity(false);
				cm.dispose();
			}
		}
	}
	/*
    if (status == 0)
	cm.sendSimple("What would you like to do?\r\n#b#L0#Create a Guild#l\r\n#L1#Disband your Guild#l\r\n#L2#Increase your Guild's capacity (limited to 100)#l\r\n#L3#Increase your Guild's capacity (limited to 200)#l#k");
    else if (status == 1) {
	sel = selection;
	if (selection == 0) {
	    if (cm.getPlayerStat("GID") > 0) {
		cm.sendOk("You may not create a new Guild while you are in one.");
		cm.dispose();
	    } else
		cm.sendYesNo("Creating a Guild costs #b500,000 mesos#k, are you sure you want to continue?");
	} else if (selection == 1) {
	    if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
		cm.sendOk("You can only disband a Guild if you are the leader of that Guild.");
		cm.dispose();
	    } else
		cm.sendYesNo("Are you sure you want to disband your Guild? You will not be able to recover it afterward and all your GP will be gone.");
	} else if (selection == 2) {
	    if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
		cm.sendOk("You can only increase your Guild's capacity if you are the leader.");
		cm.dispose();
	    } else
		cm.sendYesNo("Increasing your Guild capacity by #b5#k costs #b500,000 mesos#k, are you sure you want to continue?");
	} else if (selection == 3) {
	    if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
		cm.sendOk("You can only increase your Guild's capacity if you are the leader.");
		cm.dispose();
	    } else
		cm.sendYesNo("Increasing your Guild capacity by #b5#k costs #b25,000 GP#k, are you sure you want to continue?");
	}
    } else if (status == 2) {
	if (sel == 0 && cm.getPlayerStat("GID") <= 0) {
	    cm.genericGuildMessage(1);
	    cm.dispose();
	} else if (sel == 1 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
	    cm.disbandGuild();
	    cm.dispose();
	} else if (sel == 2 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
	    cm.increaseGuildCapacity(false);
	    cm.dispose();
	} else if (sel == 3 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
	    cm.increaseGuildCapacity(true);
	    cm.dispose();
	}
    }
    */
}