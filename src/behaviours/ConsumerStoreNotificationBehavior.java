package behaviours;

import agents.ConsumerNew;
import agents.StoreNew;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.Serializable;

public class ConsumerStoreNotificationBehavior extends CyclicBehaviour {
    private final ConsumerNew consumer;
    private MessageTemplate mt;

    public ConsumerStoreNotificationBehavior(ConsumerNew consumer) {
        this.consumer = consumer;
        this.mt = null;
    }

    @Override
    public void onStart() {
        // Get a topic helper for topic creation. We want to listen for a
        // certain topic, in order to be notified when a Store changes its position
        TopicManagementHelper topicHelper = null;
        AID topic = null;
        try {
            topicHelper = (TopicManagementHelper)consumer.getHelper(TopicManagementHelper.SERVICE_NAME);
            topic = topicHelper.createTopic("store-update-notify");
            topicHelper.register(topic);
        } catch (ServiceException ex) {
            ex.printStackTrace();
        }

        // Set the topic to the Store notification topic
        if (topic != null) {
            MessageTemplate topicMatcher = MessageTemplate.MatchTopic(topic);
            MessageTemplate perfMatcher = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            mt = MessageTemplate.and(topicMatcher, perfMatcher);
        }
    }

    @Override
    public void action() {
        // Just block and do nothing if our message template is null
        if (mt == null) {
            block();
            return;
        }

        // Check if we got a matching message, block behavior otherwise
        ACLMessage msg = consumer.receive(mt);
        if (msg == null || !msg.getSender().equals(consumer.getBestStore())) {
            block();
            return;
        }

        // Make sure the object in the message is a readable StoreInfo object
        try {
            Serializable s = msg.getContentObject();
            if (s instanceof StoreNew.StoreInfo)
                consumer.updateStoreInfo((StoreNew.StoreInfo)s);
        } catch (UnreadableException ex) {
            ex.printStackTrace();
        }
    }
}
