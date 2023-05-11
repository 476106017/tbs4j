package org.example.system.effectobj;

import lombok.Getter;
import lombok.Setter;
import org.example.turnobj.GameObj;

@Getter
@Setter
public class Fire extends GameObj {
    private int speed=0;
    private String name = "灼伤";
}
