package behaviours;

import agents.Consumer;
import agents.World;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ConsumerBehaviour extends CyclicBehaviour {
    private final Consumer c;

    private int currentShop = 0;

    public ConsumerBehaviour(Consumer c) {
        this.c = c;
    }


    @Override
    public void action() {
        //Synchronization: Only handle one store at a time
        MessageTemplate temp = MessageTemplate.MatchSender(new AID("Store"+currentShop, AID.ISLOCALNAME));
        ACLMessage rec = c.blockingReceive(temp);
        String store = rec.getSender().getLocalName();
        Integer loc = Integer.parseInt(rec.getContent());

        if(rec.getPerformative() == ACLMessage.REQUEST) { // Here is just a request by the shop to see how many customers would shop
            // Check if customer would shop at this shop (nearest shop atm)
            int newDist = Math.abs(c.getPos() - loc);
            boolean best = true;

            for(String s: c.getShops()) {
                Integer d = c.getLocations().get(s);
                if (d == null) {
                    System.out.format("WTF: %s", s);
                    continue;
                }

                int currDist = Math.abs(c.getPos() - d);
                if (!s.equals(store) && currDist <= newDist) {
                    best = false;
                    break;
                }
            }

            ACLMessage msg = rec.createReply();
            if(best) // The new location is nearest
                msg.setPerformative(ACLMessage.AGREE);
            else
                msg.setPerformative(ACLMessage.REFUSE);
            c.send(msg);
        } else { // Here the shop actually decides to move
            c.setLocation(store, loc);
            currentShop = (currentShop + 1) % World.STORES;
        }
    }
}
