/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;

import client.MapleClient;
import client.character.MapleCharacter;
import net.packet.CField;
import net.packet.field.DragonPacket;

import java.awt.Point;
import java.util.List;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.StaticLifeMovement;

/**
 *
 * @author Itzik
 */
public class MapleHaku extends AnimatedMapleMapObject {

    private final int owner;
    private final int jobid;
    private final int fh;
    private boolean stats;
    private Point pos = new Point(0, 0);

    public MapleHaku(MapleCharacter owner) {
        this.owner = owner.getID();
        this.jobid = owner.getJob();
        this.fh = owner.getFH();
        this.stats = false;

        if ((this.jobid < 4200) || (this.jobid > 4212)) {
            throw new RuntimeException("Trying to create a haku while not a kanna.");
        }
        setPosition(owner.getTruePosition());
        setStance(this.fh);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(CField.spawnHaku(this));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(DragonPacket.removeDragon(this.owner));
    }

    public int getOwner() {
        return this.owner;
    }

    public int getJobId() {
        return this.jobid;
    }

    public void sendStats() {
        this.stats = (!this.stats);
    }

    public boolean getStats() {
        return this.stats;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }

    public final Point getPos() {
        return this.pos;
    }

    public final void setPos(Point pos) {
        this.pos = pos;
    }

    public final void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if ((move instanceof LifeMovement)) {
                if ((move instanceof StaticLifeMovement)) {
                    setPos(((LifeMovement) move).getPosition());
                }
                setStance(((LifeMovement) move).getMoveAction());
            }
        }
    }
}
