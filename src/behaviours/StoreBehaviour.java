package behaviours;

import agents.Store;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class StoreBehaviour extends CyclicBehaviour {

    private Store s;

    private int[] directions = {-1, 1};

    public StoreBehaviour(Store s) {
        this.s = s;
    }


    @Override
    public void action() {

        for(int dir: directions) {

            int oldLoc = s.getLocation();
            int oldShare = s.getNumberOfShoppingCustomers();

            s.move(dir);
            //Send Location to Customers
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            for (String c : s.getCustomers()) {
                msg.addReceiver(new AID(c, AID.ISLOCALNAME));
            }
            msg.setContent(Integer.toString(s.getLocation()));
            s.send(msg);
            //Update shopping customers
            s.resetShoppingCustomers();
            for (String c : s.getCustomers()) {
                ACLMessage rep = s.blockingReceive();
                if (rep.getPerformative() == ACLMessage.AGREE) {
                    s.addShoppingCustomer(c);
                }
            }

            int newShare  = s.getNumberOfShoppingCustomers();
            if(newShare >= oldShare) {
                System.out.println(s.getLocalName()+": "+s.getLocation()+" "+"Market Share: "+newShare+"/"+s.getCustomers().size());
                //Improved market share
                break;
            } else {
                //Else we try again at a different spot
                s.setLocation(oldLoc);
            }
            s.doWait(2000);

        }

    }
}
