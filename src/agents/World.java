package agents;

import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Agent;
import jade.core.Runtime;
import jade.wrapper.StaleProxyException;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class World extends Agent {

    private ContainerController container = Runtime.instance().createAgentContainer(new ProfileImpl());

    private List<AgentController> consumerControllers = new ArrayList<>();
    private List<AgentController> shopControllers = new ArrayList<>();

    private List<String> consumers = new ArrayList<>();
    private List<String> shops = new ArrayList<>();

    private Map<String, Integer> storeLocations = new HashMap<>();

    public static final int SIZE = 100;
    public static final int STORES = 2;


    protected void setup() {
        this.doWait(500); //Waiting for Container to start
        initCustomers();
        // TODO: Waiting here is IMPORTANT. Any way to handle this gracefully?
        this.doWait(1000); //Wait for the Customers
        initStores();

    }

    private void initCustomers() {
        for(int i=0; i<SIZE; i++) {
            try {
                String nick = "Customer"+Integer.toString(i);
                AgentController c = container.createNewAgent(nick, Consumer.class.getCanonicalName(), new Integer[]{i});
                c.start();
                consumerControllers.add(c);
                consumers.add(nick);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private void initStores() {
        for(int i=0; i<STORES; i++) {
            try {
                String nick = "Store"+Integer.toString(i);
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                int loc = rnd.nextInt(SIZE);
                AgentController c = container.createNewAgent(nick, Store.class.getCanonicalName(), new Integer[]{SIZE, i, loc});
                c.start();
                shopControllers.add(c);
                shops.add(nick);
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

}
