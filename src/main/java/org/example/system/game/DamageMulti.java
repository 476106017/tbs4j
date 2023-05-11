package org.example.system.game;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.turnobj.FollowCard;

import java.util.ArrayList;
import java.util.List;

/**
 * 同步伤害
 */
@Getter
@Setter
public class DamageMulti {
    private GameInfo info;
    private List<Damage> damages;

    public DamageMulti(GameInfo info, List<Damage> damages) {
        this.info = info;
        this.damages = new ArrayList<>(damages);
    }

    public void apply(){
        // 受伤前
        damages.forEach(damage -> {
            damage.getTo().useEffects(EffectTiming.BeforeDamaged,damage);
        });
        // 如果被伤害随从不在场上或者免疫/闪避，则从伤害列表中移除
        damages.removeIf(damage -> !damage.getTo().atArea() || damage.avoid());
        // region 扣血
        damages.forEach(damage -> {
            FollowCard to = damage.getTo();
            damage.reduce();
            to.setHp(to.getHp() - damage.getDamage());
            info.msg(to.getNameWithOwner()+"受到了来自"+damage.getFrom().getNameWithOwner()+"的"+damage.getDamage()+"点伤害！" +
                "（剩余"+ to.getHp()+"点生命值）");

            try {
                Thread.sleep(500);
                info.pushInfo();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // endregion 扣血
        // region 受伤
        damages.forEach(damage -> {
            if(damage.getTo().atArea()){
                if(damage.getFrom() instanceof FollowCard follow && follow.hasKeyword("重伤")){
                    info.msg(damage.getFrom().getNameWithOwner() + "发动重伤效果！");
                    damage.getTo().addKeyword("无法回复");
                }
                if(damage.getFrom() instanceof FollowCard follow && follow.hasKeyword("吸血")){
                    info.msg(damage.getFrom().getNameWithOwner() + "发动吸血效果！");
                    follow.heal(damage.getDamage());
                }
                damage.getTo().setIncommingDamage(damage);
            }
        });
        // endregion 受伤


        // region 破坏
        damages.forEach(damage -> {
            if(damage.getTo().atArea()){
                if(damage.getFrom() instanceof FollowCard follow && follow.hasKeyword("剧毒")) {
                    // 剧毒伤害击杀
                    info.msg(damage.getFrom().getName() + "发动剧毒效果！");
                    damage.getTo().setDestroyedBy(follow);
                }else if(damage.getTo().getHp()<=0){
                    // 终结伤害击杀
                    damage.getTo().setDestroyedBy(damage.getFrom());
                }
            }
        });
        // endregion 结算
        info.startEffect();

    }
}
