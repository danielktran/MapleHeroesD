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
package server;

import client.MapleClient;
import client.anticheat.CheatingOffense;
import constants.GameConstants;
import net.packet.CWvsContext;
import net.server.channel.ChannelServer;
import scripting.portal.PortalScriptManager;

import java.awt.Point;

import server.maps.MapleMap;

public class MaplePortal {

    public static final int MAP_PORTAL = 2;
    public static final int DOOR_PORTAL = 6;
    private String name, target, scriptName;
    private Point position;
    private int targetmap, type, id;
    private boolean portalState = true;

    public MaplePortal(final int type) {
        this.type = type;
    }

    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public final String getName() {
        return name;
    }

    public final Point getPosition() {
        return position;
    }

    public final String getTarget() {
        return target;
    }

    public final int getTargetMapId() {
        return targetmap;
    }

    public final int getType() {
        return type;
    }

    public final String getScriptName() {
        return scriptName;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final void setPosition(final Point position) {
        this.position = position;
    }

    public final void setTarget(final String target) {
        this.target = target;
    }

    public final void setTargetMapId(final int targetmapid) {
        this.targetmap = targetmapid;
    }

    public final void setScriptName(final String scriptName) {
        this.scriptName = scriptName;
    }

    public final void enterPortal(final MapleClient c) {
        if (getPosition().distanceSq(c.getCharacter().getPosition()) > 40000 && !c.getCharacter().isGM()) {
            c.getSession().write(CWvsContext.enableActions());
            c.getCharacter().getCheatTracker().registerOffense(CheatingOffense.USING_FARAWAY_PORTAL);
            return;
        }
        final MapleMap currentmap = c.getCharacter().getMap();
        if (!c.getCharacter().hasBlockedInventory() && (portalState || c.getCharacter().isGM())) {
            if (getScriptName() != null) {
                c.getCharacter().checkFollow();
                try {
                    PortalScriptManager.getInstance().executePortalScript(this, c);
                } catch (final Exception e) {
                }
            } else if (getTargetMapId() != 999999999) {
                final MapleMap oldto = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(getTargetMapId());
                final MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(GameConstants.getSpecialMapTarget(getTargetMapId()));
                if (to == null) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (!c.getCharacter().isGM()) {
                    if (to.getLevelLimit() > 0 && to.getLevelLimit() > c.getCharacter().getLevel()) {
                        c.getCharacter().dropMessage(-1, "You are too low of a level to enter this place.");
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
                }
                if (c.getCharacter().getMapId() == 109010100 || c.getCharacter().getMapId() == 109010104 || c.getCharacter().getMapId() == 109020001) {
                    c.getCharacter().dropMessage(5, "You may not exit the event map.");
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                c.getCharacter().changeMapPortal(to, to.getPortal(GameConstants.getSpecialPortalTarget(oldto.getId(), getTarget())) == null ? to.getPortal(0) : to.getPortal(GameConstants.getSpecialPortalTarget(oldto.getId(), getTarget()))); //late resolving makes this harder but prevents us from loading the whole world at once
            }
        }
        if (c != null && c.getCharacter() != null && c.getCharacter().getMap() == currentmap) { // Character is still on the same map.
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    public boolean getPortalState() {
        return portalState;
    }

    public void setPortalState(boolean ps) {
        this.portalState = ps;
    }
}
