/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.channel.handler;

import client.MapleClient;
import client.MapleDisease;
import client.character.MapleCharacter;
import net.packet.CWvsContext;
import net.packet.MonsterCarnivalPacket;

import java.util.List;
import server.MapleCarnivalFactory;
import server.MapleCarnivalFactory.MCSkill;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class MonsterCarnivalHandler {

    public static final void MonsterCarnival(final LittleEndianAccessor slea, final MapleClient c) {
        if (c.getCharacter().getCarnivalParty() == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final int tab = slea.readByte();
        final int num = slea.readInt();

        if (tab == 0) {
            final List<Pair<Integer, Integer>> mobs = c.getCharacter().getMap().getMobsToSpawn();
            if (num >= mobs.size() || c.getCharacter().getAvailableCP() < mobs.get(num).right) {
                c.getCharacter().dropMessage(5, "You do not have the CP.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            final MapleMonster mons = MapleLifeFactory.getMonster(mobs.get(num).left);
            if (mons != null && c.getCharacter().getMap().makeCarnivalSpawn(c.getCharacter().getCarnivalParty().getTeam(), mons, num)) {
                c.getCharacter().getCarnivalParty().useCP(c.getCharacter(), mobs.get(num).right);
                c.getCharacter().CPUpdate(false, c.getCharacter().getAvailableCP(), c.getCharacter().getTotalCP(), 0);
                for (MapleCharacter chr : c.getCharacter().getMap().getCharactersThreadsafe()) {
                    chr.CPUpdate(true, c.getCharacter().getCarnivalParty().getAvailableCP(), c.getCharacter().getCarnivalParty().getTotalCP(), c.getCharacter().getCarnivalParty().getTeam());
                }
                c.getCharacter().getMap().broadcastMessage(MonsterCarnivalPacket.playerSummoned(c.getCharacter().getName(), tab, num));
                c.getSession().write(CWvsContext.enableActions());
            } else {
                c.getCharacter().dropMessage(5, "You may no longer summon the monster.");
                c.getSession().write(CWvsContext.enableActions());
            }

        } else if (tab == 1) { //debuff
            final List<Integer> skillid = c.getCharacter().getMap().getSkillIds();
            if (num >= skillid.size()) {
                c.getCharacter().dropMessage(5, "An error occurred.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            final MCSkill skil = MapleCarnivalFactory.getInstance().getSkill(skillid.get(num)); //ugh wtf
            if (skil == null || c.getCharacter().getAvailableCP() < skil.cpLoss) {
                c.getCharacter().dropMessage(5, "You do not have the CP.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            final MapleDisease dis = skil.getDisease();
            boolean found = false;
            for (MapleCharacter chr : c.getCharacter().getMap().getCharactersThreadsafe()) {
                if (chr.getParty() == null || (c.getCharacter().getParty() != null && chr.getParty().getId() != c.getCharacter().getParty().getId())) {
                    if (skil.targetsAll || Randomizer.nextBoolean()) {
                        found = true;
                        if (dis == null) {
                            chr.dispel();
                        } else if (skil.getSkill() == null) {
                            chr.giveDebuff(dis, 1, 30000, dis.getDisease(), 1);
                        } else {
                            chr.giveDebuff(dis, skil.getSkill());
                        }
                        if (!skil.targetsAll) {
                            break;
                        }
                    }
                }
            }
            if (found) {
                c.getCharacter().getCarnivalParty().useCP(c.getCharacter(), skil.cpLoss);
                c.getCharacter().CPUpdate(false, c.getCharacter().getAvailableCP(), c.getCharacter().getTotalCP(), 0);
                for (MapleCharacter chr : c.getCharacter().getMap().getCharactersThreadsafe()) {
                    chr.CPUpdate(true, c.getCharacter().getCarnivalParty().getAvailableCP(), c.getCharacter().getCarnivalParty().getTotalCP(), c.getCharacter().getCarnivalParty().getTeam());
                    //chr.dropMessage(5, "[" + (c.getPlayer().getCarnivalParty().getTeam() == 0 ? "Red" : "Blue") + "] " + c.getPlayer().getName() + " has used a skill. [" + dis.name() + "].");
                }
                c.getCharacter().getMap().broadcastMessage(MonsterCarnivalPacket.playerSummoned(c.getCharacter().getName(), tab, num));
                c.getSession().write(CWvsContext.enableActions());
            } else {
                c.getCharacter().dropMessage(5, "An error occurred.");
                c.getSession().write(CWvsContext.enableActions());
            }
        } else if (tab == 2) { //skill
            final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
            if (skil == null || c.getCharacter().getAvailableCP() < skil.cpLoss) {
                c.getCharacter().dropMessage(5, "You do not have the CP.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if (c.getCharacter().getMap().makeCarnivalReactor(c.getCharacter().getCarnivalParty().getTeam(), num)) {
                c.getCharacter().getCarnivalParty().useCP(c.getCharacter(), skil.cpLoss);
                c.getCharacter().CPUpdate(false, c.getCharacter().getAvailableCP(), c.getCharacter().getTotalCP(), 0);
                for (MapleCharacter chr : c.getCharacter().getMap().getCharactersThreadsafe()) {
                    chr.CPUpdate(true, c.getCharacter().getCarnivalParty().getAvailableCP(), c.getCharacter().getCarnivalParty().getTotalCP(), c.getCharacter().getCarnivalParty().getTeam());
                }
                c.getCharacter().getMap().broadcastMessage(MonsterCarnivalPacket.playerSummoned(c.getCharacter().getName(), tab, num));
                c.getSession().write(CWvsContext.enableActions());
            } else {
                c.getCharacter().dropMessage(5, "You may no longer summon the being.");
                c.getSession().write(CWvsContext.enableActions());
            }
        }

    }
}
