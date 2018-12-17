package behaviours;

import agents.Store;
import agents.World;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class StoreBehaviour extends CyclicBehaviour {

    private Store s;

    private int[] directions = {1, -1};

    public StoreBehaviour(Store s) {
        this.s = s;
    }


    @Override
    public void action() {
        for(int dir: directions) {

            int oldLoc = s.getLocation();
            int oldShare = s.getShare();

            s.move(dir);



            //Send Location to Customers
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            for (String c : s.getCustomers()) {
                msg.addReceiver(new AID(c, AID.ISLOCALNAME));
            }
            msg.setContent(Integer.toString(s.getLocation()));
            s.send(msg);
            //Collect nearest Customers
            int share = 0;
            for (String c : s.getCustomers()) {
                ACLMessage rep = s.blockingReceive();
                if (rep.getPerformative() == ACLMessage.AGREE) {
                    share ++;
                }
            }
            s.doWait(5000);
            if(share >= oldShare) {
                System.out.println(s.getLocalName()+": "+s.getLocation()+" "+"Market Share: "+share+"/"+s.getCustomers().size());
                //Improved market share
                //Inform customers of move
                ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
                inf.setContent(Integer.toString(s.getLocation()));
                for(String c:s.getCustomers()) {
                    inf.addReceiver(new AID(c, AID.ISLOCALNAME));
                }
                s.send(inf);
                s.setShare(share);
                break;
            } else {
                //Else we try again at a different spot
                s.setLocation(oldLoc);
            }


        }
    }

}
