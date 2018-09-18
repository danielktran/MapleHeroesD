/* RED 1st impact
    Vasily
    Made by Daenerys
*/
var status = -1;

function action(mode, type, selection) {
    if (mode == 1)
	status++;
    else
	status--;
    if (pi.getQuestStatus(32214)==2){
        pi.openNpc(10305, "ExplorerTut05");
    } else if (status == 0) {
	    cm.sendNext("The ship isn't ready to set sail yet.");
		cm.dispose();
    }
}