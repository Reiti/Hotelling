package agents;

import behaviours.StoreNewBehavior;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class StoreNew extends Agent {
    private AID[] consumers = new AID[0];
    public int consumerCount = 0;
    public StoreInfo info = new StoreInfo();

    static public class StoreInfo implements Serializable {
        public int location;

        public StoreInfo() {
            location = ThreadLocalRandom.current().nextInt(0, 100);
        }
    }

    @Override
    protected void setup() {
        addBehaviour(new TickerBehaviour(this, 500) {
            @Override
            protected void onTick() {
                // Set up template for performing a yellow-pages query
                // in order to find all consumer agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("consumer");
                sd.setName("JADE-hotelling");
                template.addServices(sd);

                // Perform the query and store all found consumer agents
                // in a member array
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    consumers = new AID[result.length];
                    for (int i = 0; i < result.length; i++)
                        consumers[i] = result[i].getName();
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        addBehaviour(new StoreNewBehavior(this));
    }

    public AID[] getConsumers() {
        return consumers;
    }
}
