package agents;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class WorldNew extends Agent {
    private ContainerController container = null;
    private static final int SIZE = 100;
    private static final int STORES = 2;


    protected void setup() {
        Profile p = new ProfileImpl();
        p.setParameter(Profile.SERVICES, "jade.core.messaging.TopicManagementService");
        container = Runtime.instance().createAgentContainer(p);

        this.doWait(1000); //Waiting for Container to start
        initCustomers();
        this.doWait(2000); //Wait for the Customers
        initStores();

    }

    private void initCustomers() {
        for(int i=0; i<SIZE; i++) {
            try {
                String nick = "Customer"+Integer.toString(i);
                AgentController c = container.createNewAgent(nick, ConsumerNew.class.getCanonicalName(), new Integer[]{i});
                c.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private void initStores() {
        for(int i=0; i<STORES; i++) {
            try {
                String nick = "Store"+Integer.toString(i);
                AgentController c = container.createNewAgent(nick, StoreNew.class.getCanonicalName(), new Integer[]{});
                c.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}
