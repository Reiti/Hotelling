package behaviours;

import agents.Store;
import agents.World;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.concurrent.ThreadLocalRandom;

public class StoreBehaviour extends CyclicBehaviour {
    private final Store s;

    private int iteration = 0;

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

        // Print info in a structured/unstructured way
        if (s.isStructured()) {
            System.out.format("%s,%d,%d,%d%n", s.getLocalName(), iteration, s.getLocation(), share);
            iteration += 1;
        } else {
            System.out.format("%s        - Location: %d Market Share: %d/%d%n",
                    s.getLocalName(), s.getLocation(), share, customers.length);
        }
        s.setShare(share);

        // Pick strategy for moving
        int[] directions;
        switch (s.getStrategy()) {
            case "Random":
                directions = new int[]{
                        ThreadLocalRandom.current().nextInt(-customers.length, -1),
                        ThreadLocalRandom.current().nextInt(1, customers.length) };
                break;
            case "Step1":
                directions = new int[]{-1, 1};
                break;
            case "Step20":
                directions = new int[]{-20, -19, -18, -17, -16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
                break;
            case "Step2":
            default:
                directions = new int[]{-2, -1, 1, 2};
                break;
        }

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
                if (!s.isStructured()) {
                    System.out.format("%s moved to location: %d Market Share: %d/%d%n",
                            s.getLocalName(), s.getLocation(), share, customers.length);
                }

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
