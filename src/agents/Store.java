package agents;

import behaviours.StoreBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Store extends Agent {
    private AID[] customers;

    private int worldSize = 0;
    private String strategy = "Step2";
    private int structured = 0;

    private int location = 0;
    private int share = 0;

    protected void setup() {
        Object[] args = getArguments();
        worldSize = (Integer)args[0];
        strategy = args[2].toString();
        structured = (Integer)args[3];
        location = (Integer)args[5];

        customers = new AID[worldSize];
        for (int i = 0; i < worldSize; i++)
            customers[i] = new AID("Customer" + i, AID.ISLOCALNAME);

        if (structured == 0) {
            System.out.println("Store " + getLocalName() + " starting!");
            System.out.println("Location: " + location);
        }

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
        if(n >= 0 && n < worldSize) {
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

    public boolean isStructured() {
        return structured == 1;
    }

    public String getStrategy() {
        return strategy;
    }
}
