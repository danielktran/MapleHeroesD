package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import server.maps.MapScriptMethods;
import tools.data.LittleEndianAccessor;

public class NPCTalkMoreHandler extends AbstractMaplePacketHandler {

	public NPCTalkMoreHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		final byte lastMsg = lea.readByte(); // 00 (last msg type I think)
		
        if (lastMsg == 9 && lea.available() >= 4) {
            lea.readShort();
        }
        final byte action = lea.readByte(); // 00 = end chat, 01 == follow
        byte disposeByte;
        switch(lastMsg) {
	        case 3: 
	        case 4:
	        case 5:
	        case 9:
	        	disposeByte = 0;
	        	break;
        	default:
        		disposeByte = (byte)0xFF;
        		break;
        }

        if (((lastMsg == 0x12 && c.getCharacter().getDirection() >= 0) || (lastMsg == 0x12 && c.getCharacter().getDirection() == -1)) && action == 1) {
            byte lastbyte = lea.readByte(); // 00 = end chat, 01 == follow
            if (lastbyte == 0) {
                c.getSession().write(CWvsContext.enableActions());
            } else {
                MapScriptMethods.startDirectionInfo(c.getCharacter(), lastMsg == 0x13);
                c.getSession().write(CWvsContext.enableActions());
            }
            return;
        }
        final NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);

        if(action == disposeByte) {
        	cm.dispose();
        	return;
        } 
        /*if (cm != null && lastMsg == 0x17) {
            c.getPlayer().handleDemonJob(slea.readInt());
            return;
        }*/
        if (cm == null || c.getCharacter().getConversation() == 0 || cm.getLastMsg() != lastMsg) {
            return;
        }
        cm.setLastMsg((byte) -1);
        if (lastMsg == 1) {
            NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
        } else if (lastMsg == 3) {
            if (action != 0) {
                cm.setGetText(lea.readMapleAsciiString());
                if (cm.getType() == 0) {
                    NPCScriptManager.getInstance().startQuest(c, action, lastMsg, -1);
                } else if (cm.getType() == 1) {
                    NPCScriptManager.getInstance().endQuest(c, action, lastMsg, -1);
                } else {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
                }
            } else {
                cm.dispose();
            }
        } else if (lastMsg == 0x17) {
            NPCScriptManager.getInstance().action(c, (byte) 1, lastMsg, action);
        } else if (lastMsg == 0x16) {
            NPCScriptManager.getInstance().action(c, (byte) 1, lastMsg, action);
        } else {
            int selection = -1;
            if (lea.available() >= 4) {
                selection = lea.readInt();
            } else if (lea.available() > 0) {
                selection = lea.readByte();
            }
            if (lastMsg == 4 && selection == -1) {
                cm.dispose();
                return;//h4x
            }
            if (selection >= -1 && action != -1) {
                if (cm.getType() == 0) {
                    NPCScriptManager.getInstance().startQuest(c, action, lastMsg, selection);
                } else if (cm.getType() == 1) {
                    NPCScriptManager.getInstance().endQuest(c, action, lastMsg, selection);
                } else {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
                }
            } else {
                cm.dispose();
            }
        }
        
             
        
	}	
}
