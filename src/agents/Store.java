package agents;

import behaviours.StoreBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class Store extends Agent {

    private List<String> customers = new ArrayList<>();

    private int location = 0;
    private int share = 0;
    private int id = 0;


    protected void setup() {
        System.out.println("Store " + getLocalName() + " starting!");
        Random rnd = new Random();
        location = rnd.nextInt(World.SIZE);
        System.out.println("Location: " + location);

        id = (Integer)getArguments()[0];

        for(int i = 0; i< World.SIZE; i++) {
            customers.add("Customer"+Integer.toString(i));
        }


        StoreBehaviour b = new StoreBehaviour(this);
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage init = new ACLMessage(ACLMessage.INFORM);
                init.setContent(Integer.toString(getLocation()));
                for(String c: customers) {
                    init.addReceiver(new AID(c, AID.ISLOCALNAME));
                }
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

    public List<String> getCustomers() {
        return customers;
    }

    public void setLocation(int loc) {
        this.location = loc;
    }



}
