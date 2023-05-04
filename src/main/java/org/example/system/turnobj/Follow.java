package org.example.system.turnobj;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.system.constant.CardType;

import static org.example.system.util.TurnWrapper.TURN_DISTANCE;

@Getter
@Setter
public class Follow extends TurnObject {

    public final CardType TYPE = CardType.FOLLOW;
    private transient int atk = 0;
    private transient int hp = 0;
    private int maxHp = 0;

    public void initHp(int hp) {
        this.hp = hp;
        this.maxHp = hp;
    }

    public void attack(Follow another){
        final int hp = another.getHp() - getAtk();
        another.setHp(hp);
    }

    @Override
    public String toString() {
        return "\t"+getName()+"\t生命："+getHp()+"\t攻击力："+getAtk()+"\t速度："+getSpeed()
            + "\t距离下回合："+getWaitTimeShow();
    }
}