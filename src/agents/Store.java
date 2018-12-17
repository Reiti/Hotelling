package agents;

import behaviours.StoreBehaviour;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Store extends Agent {

    private List<String> customers = new ArrayList<>();
    private int previous = 0;
    private int location = 0;
    private int price = 1;

    private List<String> shoppingCustomers = new ArrayList<>();

    protected void setup() {
        System.out.println("Store " + getLocalName() + " starting!");
        Random rnd = new Random();
        location = rnd.nextInt(World.SIZE);
        System.out.println("Location: " + location);

        for(int i = 0; i< World.SIZE; i++) {
            customers.add("Customer"+Integer.toString(i));
        }

        StoreBehaviour b = new StoreBehaviour(this);
        addBehaviour(b);
    }

    public void move(int amount) {
        int n = location + amount;
        if(n >= 0 && n<=World.SIZE) {
            location = n;
        }
    }

    public int getLocation() {
        return location;
    }

    public void saveOld() {
        previous = location;
    }

    public int getPreviousLocation() {
        return previous;
    }

    public void resetShoppingCustomers() {
        shoppingCustomers = new ArrayList<>();
    }

    public void addShoppingCustomer(String c) {
        shoppingCustomers.add(c);
    }

    public int getNumberOfShoppingCustomers() {
        return shoppingCustomers.size();
    }

    public List<String> getCustomers() {
        return customers;
    }

    public void setLocation(int loc) {
        this.location = loc;
    }

}
