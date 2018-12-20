package agents;

import behaviours.ConsumerNewBehavior;
import behaviours.ConsumerStoreNotificationBehavior;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.concurrent.ThreadLocalRandom;

public class ConsumerNew extends Agent {
    private AID bestStore = null;
    private StoreNew.StoreInfo info = null;
    public StoreNew.StoreInfo consumerInfo;

    public ConsumerNew() {
        this.consumerInfo = new StoreNew.StoreInfo();
    }

    public ConsumerNew(StoreNew.StoreInfo consumerInfo) {
        this.consumerInfo = consumerInfo;
    }

    @Override
    protected void setup() {
        // Check if arguments have been specified, if yes, read location from there
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.consumerInfo.location = (int)args[0];
        }

        // Create an agent description for the yellow-pages service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // Register the consumer agent as a service at the yellow-pages
        ServiceDescription sd;
        sd = new ServiceDescription();
        sd.setName("JADE-hotelling");
        sd.setType("consumer");
        dfd.addServices(sd);

        // Add the behavior which is responsible for answering inquiries
        // made by a Store agent, deciding whether this agents wants
        // to visit another Store or stick with the Store he previously visited
        addBehaviour(new ConsumerNewBehavior(this));

        // Add the behavior responsible for updating the stored StoreInfo
        // if the Consumer's favorite Store has changed its position.
        addBehaviour(new ConsumerStoreNotificationBehavior(this));

        // Register our agent at the yellow-pages service
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        System.out.format("Consumer %s is terminating.%n", getLocalName());

        // Remove consumer agent from the yellow-pages service
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public boolean isBestStore(AID store, StoreNew.StoreInfo info) {
        // If no Store has been chosen yet, it's trivially gonna be the best Store
        if (this.info == null) {
            this.bestStore = store;
            //System.out.format("Case 1: Consumer %s belongs to Store %s%n", getLocalName(), store.getLocalName());
            return true;
        }

        // If the store is the same as the chosen one, it's gonna stay the same.
        // This is important for market share calculation.
        if (this.bestStore.equals(store)) {
            //System.out.format("Case 2: Consumer %s belongs to Store %s%n", getLocalName(), store.getLocalName());
            return true;
        }

        // Check if new distance is less than existing distance to best store
        int distBest = Math.abs(this.info.location - consumerInfo.location);
        int distNew = Math.abs(info.location - consumerInfo.location);
        if (distNew < distBest) {
            this.bestStore = store;
            //System.out.format("Case 3: Consumer %s belongs to Store %s%n", getLocalName(), store.getLocalName());
            return true;
        } else if (distNew == distBest && ThreadLocalRandom.current().nextInt(0, 2) == 1) {
            this.bestStore = store;
            return true;
        }

        return false;
    }

    public AID getBestStore() {
        return bestStore;
    }

    public void updateStoreInfo(StoreNew.StoreInfo info) {
        this.info = info;
    }
}
