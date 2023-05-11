package org.example.constant;

import org.example.system.Database;
import org.example.system.game.Leader;
import org.example.turnobj.FollowCard;
import org.example.turnobj.ThePlayer;
import org.example.system.util.Maps;
import org.example.turnobj.jojo.EnricoPucci;
import org.example.turnobj.pokemon.Fusigidane;
import org.example.turnobj.pokemon.Hitokage;
import org.example.turnobj.pokemon.Pikachu;
import org.example.turnobj.pokemon.Zenigame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预置牌组
 */
public class DeckPreset {
    public static final Map<String,List<Class<? extends FollowCard>>> decks = new HashMap<>();
    public static final Map<String,Class<? extends Leader>> deckLeader = new HashMap<>();
    static {
        decks.put("宝可梦御三家",List.of(
            Pikachu.class, Zenigame.class, Hitokage.class, Fusigidane.class
        ));
        decks.put("普奇神父完全体",List.of(
            EnricoPucci.class
        ));
    }
    public static List describe(){
        List<Map<String,Object>> deckInfo = new ArrayList<>();
        decks.forEach((name,cardClassList)-> {
            final List<? extends FollowCard> prototypes = cardClassList.stream().map(Database::getPrototype).toList();
            Leader leader;
            try {
                Class<? extends Leader> leaderClass = DeckPreset.deckLeader.get(name);
                if(leaderClass==null){
                    leaderClass = ThePlayer.class;
                }
                leader = leaderClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            deckInfo.add(Maps.newMap("name", name, "leader", leader,"deck", prototypes));
        });
        return deckInfo;
    }
}
