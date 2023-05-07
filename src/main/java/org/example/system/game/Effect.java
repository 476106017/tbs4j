package org.example.system.game;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.system.turnobj.FollowCard;
import org.example.system.turnobj.GameObj;
import org.example.system.util.FunctionN;
import org.example.system.util.PredicateN;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@Setter
public class Effect{
    private GameObj parent;
    private GameObj ownerObj;
    private EffectTiming timing;
    private int canUseTurn = -1; // 可使用回合（包含敌方回合）
    private Predicate<Object> canEffect = o -> true;
    private Consumer<Object> effect;

    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn,
                  Predicate<Object> canEffect, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canUseTurn = canUseTurn;
        this.canEffect = canEffect;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing,
                  Predicate<Object> canEffect, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canEffect = canEffect;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing,
                  PredicateN canEffect, FunctionN effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canEffect = obj-> canEffect.test();
        this.effect = obj-> effect.apply();
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canUseTurn = canUseTurn;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn, FunctionN effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canUseTurn = canUseTurn;
        this.effect = obj->effect.apply();
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, FunctionN effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.effect = obj->effect.apply();
    }

    public PlayerInfo ownerPlayer(){
        return ownerObj.ownerPlayer();
    }

    @Setter
    @Getter
    public static class EffectInstance {
        private Effect effect;
        private Object param;

        public EffectInstance(Effect effect, Object param) {
            assert param==null || effect.getTiming().getParamClass()==param.getClass();
            this.effect = effect;
            this.param = param;
        }

        public EffectInstance(Effect effect) {
            this(effect, null);
        }

        public void consume() {
            if (effect.getCanEffect().test(param)) {
                GameObj effectOwnerCard = effect.getOwnerObj();
                PlayerInfo ownerPlayer = effectOwnerCard.ownerPlayer();
                GameInfo info = effectOwnerCard.getInfo();

                // region 判断结算时是否在场
                if (List.of(EffectTiming.WhenKill, EffectTiming.WhenAtArea,
                        EffectTiming.WhenRecalled, EffectTiming.WhenOthersRecall,
                        EffectTiming.WhenOverDraw, EffectTiming.WhenEnemyOverDraw,
                        EffectTiming.Entering, EffectTiming.AfterDamaged,
                        EffectTiming.WhenDraw, EffectTiming.WhenEnemyDraw,
                        EffectTiming.WhenSummon, EffectTiming.WhenEnemySummon,
                        EffectTiming.WhenDestroy, EffectTiming.WhenEnemyDestroy,
                        EffectTiming.AfterLeaderDamaged, EffectTiming.AfterEnemyLeaderDamaged,
                        EffectTiming.LeaderHealed)
                    .contains(effect.getTiming())
                    && effect.getOwnerObj() instanceof FollowCard areaCard
                    && !areaCard.atArea()) return;
                // endregion 判断结算时是否在场
                // region 判断结算时是否离场
                if (List.of(EffectTiming.WhenNoLongerAtArea, EffectTiming.Leaving,
                        EffectTiming.Exile, EffectTiming.DeathRattle)
                    .contains(effect.getTiming())
                    && effect.getOwnerObj() instanceof FollowCard card
                    && card.atArea()) return;
                // endregion 判断结算时是否离场

                if (effect.getTiming().isSecret())
                    info.msgTo(ownerPlayer.getSession(), effectOwnerCard.getNameWithOwner() + "发动【" + effect.getTiming().getName() + "】效果");
                else
                    info.msg(effectOwnerCard.getNameWithOwner() + "发动【" + effect.getTiming().getName() + "】效果");

                effect.getEffect().accept(param);
            }
        }
    }
}