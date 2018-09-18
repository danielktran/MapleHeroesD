/**
 * Quest Name: Magicians of Ellinia
 * Description: Magician's job advancement.
 * NPC: Grendel the Really Old
 */
var status = -1;

function start(mode, type, selection) {
    qm.dispose();
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            qm.sendYesNo("Ahh, welcome. It's good to finally meet, face-to-face. I can already see that you will become a great Magician. Let's make this official right away.");
        } else if (status == 1) {
            qm.resetStats(4, 4, 25, 4);
            qm.expandInventory(1, 4);
            qm.expandInventory(4, 4);
            qm.changeJob(200); // MAGICIAN
            qm.gainItem(1372005, 1);
            qm.forceCompleteQuest();
            qm.sendNext("With this, you will be able to use a wide variety of magic skills. I gave you a bit of #bSP#k, so open the #bSkill#k window and try learning a skill. If possible, try to learn some #bmagic attacks#k.");
        } else if (status == 2) {
            qm.sendNextPrev("But remember, skills aren't everything. Your stats should also support your skills as a Magician. Magicians use INT as their main stat, and LUK as their secondary stat. If raising stats is difficult, just use #bAuto-Assign#k.");
        } else if (status == 3) {
            qm.sendNextPrev("Now, one more word of warning for you. If you fall in battle from this point on, you will lose a portion of your total EXP. Be extra mindful of this, since you have less HP than most.");
        } else if (status == 4) {
            qm.sendNextPrev("This is all I can teach you. You have your wand for training, so use it well. I wish you luck on your journey.");
        } else if (status == 5) {
            qm.dispose();
        }
    }
}