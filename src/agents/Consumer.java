package agents;

import behaviours.ConsumerBehaviour;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

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
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                for(String s: shops) {
                    ACLMessage rec = this.myAgent.blockingReceive();
                    String store = rec.getSender().getLocalName();
                    Integer location = Integer.parseInt(rec.getContent());
                    locations.put(store, location);
                }
                this.myAgent.addBehaviour(b);
            }
        });
    }

    public List<String> getShops() {
        return shops;
    }

    public void setLocation(String name, Integer loc) {
        locations.put(name, loc);
    }

    public Map<String, Integer> getLocations() {
        return locations;
    }

    public int getPos() {
        return pos;
    }

    public Map.Entry<String, Integer> getBestShop() {
        int best = World.SIZE;
        Map.Entry<String, Integer> curr = null;
        for(Map.Entry<String, Integer> en: locations.entrySet()) {
            int dist = Math.abs(pos - en.getValue());
            if(dist <= best) {
                curr = en;
                best = dist;
            }
        }

        return curr;
    }
}
