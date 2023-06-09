package org.example.system.game;

import org.example.turnobj.FollowCard;
import org.example.turnobj.GameObj;
import org.example.system.util.FunctionN;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** 使用 */
public record Play(Supplier<List<FollowCard>> canTargets,

                   boolean mustTarget,
                   Consumer<FollowCard> effect){

    public Play(Supplier<List<FollowCard>> canTargets, boolean mustTarget, Consumer<FollowCard> effect) {
        this.canTargets = canTargets;
        this.mustTarget = mustTarget;
        this.effect = effect;
    }

    /**
     * 不需要抉择/选择目标
     */
    public Play(FunctionN effect) {
        this(ArrayList::new,false, p -> effect.apply());
    }

}