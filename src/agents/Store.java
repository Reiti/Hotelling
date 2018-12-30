package agents;

import behaviours.StoreBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Store extends Agent {
    private AID[] customers;

    private int location = 0;
    private int share = 0;
    private int id = 0;


    protected void setup() {
        int worldSize = (Integer)getArguments()[0];
        customers = new AID[worldSize];
        for (int i = 0; i < worldSize; i++)
            customers[i] = new AID("Customer" + i, AID.ISLOCALNAME);

        id = (Integer)getArguments()[1];
        location = (Integer)getArguments()[2];

        System.out.println("Store " + getLocalName() + " starting!");
        System.out.println("Location: " + location);


        StoreBehaviour b = new StoreBehaviour(this);
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage init = new ACLMessage(ACLMessage.INFORM);
                init.setContent(Integer.toString(getLocation()));
                for (AID c : customers)
                    init.addReceiver(c);
                this.myAgent.send(init);

                this.myAgent.addBehaviour(b);
            }
        });
    }

    public void move(int amount) {
        int n = location + amount;
        if(n >= 0 && n<World.SIZE) {
            location = n;
        }
    }

    public int getLocation() {
        return location;
    }

    public void setShare(int share) {
        this.share = share;
    }

    public int getShare() {
        return share;
    }

    public AID[] getCustomers() {
        return customers;
    }

    public void setLocation(int loc) {
        this.location = loc;
    }



}
