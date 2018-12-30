package behaviours;

import agents.Store;
import agents.World;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class StoreBehaviour extends CyclicBehaviour {
    private final Store s;

    private int[] directions = {-2, -1, 1, 2};

    public StoreBehaviour(Store s) {
        this.s = s;
    }

    @Override
    public void action() {
        AID[] customers = s.getCustomers();

        // Send Location to Customers (to check whether market share has changed)
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        for (AID c : customers)
            msg.addReceiver(c);
        msg.setContent(Integer.toString(s.getLocation()));
        s.send(msg);

        // Collect nearest Customers for market share calculation
        int share = 0;
        for (AID c : customers) {
            ACLMessage rep = s.blockingReceive();
            if (rep.getPerformative() == ACLMessage.AGREE)
                share++;
        }
        System.out.println(s.getLocalName() + ": " + s.getLocation() + " " + "Market Share: " + share + "/" + customers.length);
        s.setShare(share);

        // Try to move
        for (int dir : directions) {
            int oldLoc = s.getLocation();
            int oldShare = s.getShare();
            s.move(dir);

            // Send Location to Customers
            msg = new ACLMessage(ACLMessage.REQUEST);
            for (AID c : customers)
                msg.addReceiver(c);
            msg.setContent(Integer.toString(s.getLocation()));
            s.send(msg);

            // Collect nearest Customers
            share = 0;
            for (AID c : customers) {
                ACLMessage rep = s.blockingReceive();
                if (rep.getPerformative() == ACLMessage.AGREE)
                    share++;
            }

            if (share > oldShare) {
                System.out.println(s.getLocalName() + ": " + s.getLocation() + " " + "Market Share: " + share + "/" + customers.length);
                //Improved market share

                s.setShare(share);
                break;
            } else {
                //Else we try again at a different spot
                s.setLocation(oldLoc);
            }
        }

        // Inform customers of move (or not)
        ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
        inf.setContent(Integer.toString(s.getLocation()));
        for (AID c : customers)
            inf.addReceiver(c);
        s.send(inf);
    }
}
