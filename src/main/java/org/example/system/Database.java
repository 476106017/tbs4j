package org.example.system;

import jakarta.websocket.Session;
import org.example.system.game.GameInfo;
import org.example.system.game.PlayerDeck;
import org.example.turnobj.FollowCard;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class Database {

    public static Map<Session,String> userNames = new ConcurrentHashMap<>();
    public static Map<Session, PlayerDeck> userDecks = new ConcurrentHashMap<>();
    public static Map<Session, String > userRoom = new ConcurrentHashMap<>();
    public static String waitRoom = "";// 匹配中的房间
    public static Session waitUser;// 匹配中的人
    public static Map<String, GameInfo> roomGame = new ConcurrentHashMap<>();
    public static  Map<String,ScheduledExecutorService> roomSchedule = new ConcurrentHashMap<>();
    public static Map<String,Class<? extends FollowCard>> nameToCardClass = new ConcurrentHashMap<>();
    public static Map<Class<? extends FollowCard>, FollowCard> prototypes = new ConcurrentHashMap<>();

    public static <T extends FollowCard> T getPrototype(Class<T> clazz) {
        FollowCard prototype = prototypes.get(clazz);
        if(prototype!=null) return (T)prototype;
        try {
            FollowCard card = clazz.getDeclaredConstructor().newInstance();
            card.init();
            prototypes.put(clazz,card);
            nameToCardClass.put(card.getName(),clazz);
            return (T)card;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
