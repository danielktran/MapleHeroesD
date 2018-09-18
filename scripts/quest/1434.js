/**
 * Quest Name: [Job Advancement] Chiefs of El Nath
 * Description: Mage 3rd Job Advancement
 * Start NPC: Grendel the Really Old
 * End NPC: Robeira
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

        if (status == 0) {
            qm.warp(211000001);
            qm.forceStartQuest();
            qm.dispose();
        }
    }
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
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}