package agents;

import behaviours.ConsumerBehaviour;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Consumer extends Agent {
    private List<String> shops = new ArrayList<>();

    private Map<String, Integer> locations = new HashMap<>();

    private int pos = 0;

    protected void setup() {
        System.out.println("Consumer " + getLocalName() + " starting.");
        pos = (Integer)getArguments()[0];
        for(int i=0; i<World.STORES; i++) {
            shops.add("Store"+Integer.toString(i));
        }

        ConsumerBehaviour b = new ConsumerBehaviour(this);
        addBehaviour(b);
    }

    public List<String> getShops() {
        return shops;
    }

    public void setLocation(String name, Integer loc) {
        locations.put(name, loc);
    }

    public String getBestShop() {
        String best = null;
        Integer min = World.SIZE;
        for(String k : locations.keySet()) {
            Integer curr = Math.abs(locations.get(k) - pos);
            if(curr < min) {
                min = curr;
                best = k;
            }
        }

        return best;
    }
}
