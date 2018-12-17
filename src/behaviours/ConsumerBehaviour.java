package behaviours;

import agents.Consumer;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ConsumerBehaviour extends CyclicBehaviour {
    private Consumer c = null;

    public ConsumerBehaviour(Consumer c) {
        this.c = c;
    }


    @Override
    public void action() {
        ACLMessage rec = c.blockingReceive();
        String store = rec.getSender().getLocalName();
        Integer loc = Integer.parseInt(rec.getContent());

        if(rec.getPerformative() == ACLMessage.REQUEST) { //Here is just a request by the shop to see how many customers would shop
            System.out.println("Test: "+loc);
            //Check if customer would shop at this shop (nearest shop atm)
            int dist = Math.abs(c.getPos() - loc); //Distance to the new location
            boolean best = true;
            for(Integer d: c.getLocations().values()) {
                if(d <= dist) {
                    best = false;
                }
            }
            ACLMessage msg;
            if(best) { //the new location is nearest
                msg = new ACLMessage(ACLMessage.AGREE);
            }
            else {
                msg = new ACLMessage(ACLMessage.REFUSE);
            }
            msg.addReceiver(new AID(store, AID.ISLOCALNAME));
            c.send(msg);
        } else { //Here the shop actually decides to move
            System.out.println("Set: "+loc);
            c.setLocation(store, loc);
        }


    }
}
