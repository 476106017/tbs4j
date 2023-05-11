package org.example.system.game;

import lombok.Getter;
import lombok.Setter;
import org.example.system.Database;
import org.example.turnobj.FollowCard;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
@Setter
public class PlayerDeck {
    Class<? extends Leader> leaderClass;
    List<Class<? extends FollowCard>> activeDeck = new ArrayList<>();
    public Leader getLeader(int owner, GameInfo info){
        try {
            Leader leader = leaderClass.getDeclaredConstructor().newInstance();
            leader.setOwner(owner);
            leader.setInfo(info);
            return leader;
        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    public List<FollowCard> getActiveDeckInstance(int owner, GameInfo info) {
        List<FollowCard> _return = new ArrayList<>();
        activeDeck.forEach(cardClass->{
            try {
                FollowCard card = cardClass.getDeclaredConstructor().newInstance();
                card.setOwner(owner);
                card.setInfo(info);
                card.init();
                _return.add(card);
            } catch (NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        return _return;
    }

    public Map describe() {
        Map<String,Object> _return = new HashMap<>();
        try {
            Leader leader = leaderClass.getDeclaredConstructor().newInstance();
            _return.put("leader", leader);
        }catch (Exception ignored){}

        List<? extends FollowCard> deckCards = activeDeck.stream()
            .map(Database::getPrototype).toList();
        _return.put("deck", deckCards);
        return _return;
    }
}
