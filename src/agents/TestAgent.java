package agents;

import jade.core.Agent;

public class TestAgent extends Agent{

    protected void setup() {
        System.out.println("Sup");
        System.out.println("I am "+ getLocalName());
    }
}
