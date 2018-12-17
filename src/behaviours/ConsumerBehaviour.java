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
        //Receive Shop Locations
        for(String shop: c.getShops()) {
            ACLMessage rec = c.blockingReceive();
            String shopName = rec.getSender().getLocalName();
            String loc = rec.getContent();
            c.setLocation(shopName, Integer.parseInt(loc));
        }
        //Calculate best
        String nearest = c.getBestShop();


        //Shop there and tell the others no
        for(String shop: c.getShops()) {
            ACLMessage inf = null;
            if(shop.equals(nearest)) {
                inf = new ACLMessage(ACLMessage.AGREE);

            }
            else {
                inf = new ACLMessage(ACLMessage.REFUSE);
            }
            inf.addReceiver(new AID(shop, AID.ISLOCALNAME));

            c.send(inf);
        }

    }
}
