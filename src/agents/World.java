package agents;

import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Agent;
import jade.core.Runtime;
import jade.wrapper.StaleProxyException;

import java.util.concurrent.ThreadLocalRandom;

public class World extends Agent {
    private final ContainerController container = Runtime.instance().createAgentContainer(new ProfileImpl());

    protected void setup() {
        Object[] args = getArguments();
        if (args == null)
            args = new Object[0];

        int size = args.length > 0 ? Integer.parseInt(args[0].toString()) : 100;
        int stores = args.length > 1 ? Integer.parseInt(args[1].toString()) : 2;
        String strategy = args.length > 2 ? args[2].toString() : "Step2";
        int structured = args.length > 3 && args[3].toString().equals("structured") ? 1 : 0;

        // Wait for container to start
        this.doWait(500);

        initCustomers(size, stores, structured);

        // Wait till customers have been started
        this.doWait(1000);

        initStores(size, stores, strategy, structured);

    }

    private void initCustomers(int size, int stores, int structured) {
        for(int i = 0; i < size; i++) {
            try {
                String nick = "Customer"+Integer.toString(i);
                Object[] data = new Object[]{ size, stores, structured, i };
                AgentController c = container.createNewAgent(nick, Consumer.class.getCanonicalName(), data);
                c.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    private void initStores(int size, int stores, String strategy, int structured) {
        for(int i = 0; i < stores; i++) {
            try {
                String nick = "Store"+Integer.toString(i);
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                int loc = rnd.nextInt(size);
                Object[] data = new Object[]{size, stores, strategy, structured, i, loc};
                AgentController c = container.createNewAgent(nick, Store.class.getCanonicalName(), data);
                c.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

}
