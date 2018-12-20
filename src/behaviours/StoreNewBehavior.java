package behaviours;

import agents.StoreNew;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class StoreNewBehavior extends FSMBehaviour {
    private final static int MSG_TIMEOUT = 2000;
    private final StoreNew store;

    private StoreNew.StoreInfo[] newLocations;
    private int[] consumerAgreements;

    public StoreNewBehavior(StoreNew store) {
        this.store = store;

        final String STATE_LOCATION = "A";
        final String STATE_INQUIRY = "B";
        final String STATE_MOVE = "C";
        final String STATE_TERMINATE = "D";

        // Define state machine states
        registerFirstState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.format("Store %s, Location: %d, Share: %d/%d%n", store.getLocalName(), store.info.location, store.consumerCount, store.getConsumers().length);
                newLocations = new StoreNew.StoreInfo[2];
                consumerAgreements = new int[2];
                newLocations[0] = new StoreNew.StoreInfo();
                newLocations[0].location = store.info.location;
                newLocations[1] = new StoreNew.StoreInfo();
                newLocations[1].location = ThreadLocalRandom.current().nextInt(0, 100);
                block(1000);
            }
        }, STATE_LOCATION);
        registerState(new Inquiry(), STATE_INQUIRY);
        registerState(new Move(), STATE_MOVE);
        registerLastState(new Termination(), STATE_TERMINATE);

        // Define state machine transitions
        registerDefaultTransition(STATE_LOCATION, STATE_INQUIRY);
        registerDefaultTransition(STATE_INQUIRY, STATE_MOVE);
        registerTransition(STATE_MOVE, STATE_LOCATION, 0);
        registerTransition(STATE_MOVE, STATE_TERMINATE, 1);
    }

    private class Inquiry extends SequentialBehaviour {
        private Behaviour dynamicBehavior = null;

        private Inquiry() {
            // Prepare sending an inquiry message to all consumers
            addSubBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    // Initialize request data staying the same over all requests
                    ACLMessage inquiry = new ACLMessage(ACLMessage.REQUEST);
                    inquiry.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    inquiry.setReplyByDate(new Date(System.currentTimeMillis() + MSG_TIMEOUT));

                    // Address the inquiry to all known consumers
                    inquiry.clearAllReceiver();
                    AID[] consumers = store.getConsumers();
                    for (AID consumer : consumers)
                        inquiry.addReceiver(consumer);

                    // Only prepare inquiry if we have at least one consumer to inquire
                    int numConsumers = consumers.length;
                    if (numConsumers == 0)
                        return;

                    //System.out.format("%s: Preparing inquiry for %d consumers.%n",
                    //        store.getLocalName(), numConsumers);

                    // Serialize the store info we want to send to the consumers
                    try {
                        inquiry.setContentObject(newLocations);
                    } catch (IOException ex) {
                        System.out.format("%s: Could not serialize message: %s", store.getLocalName(), ex);
                        return;
                    }

                    // Use the FIPA Request infrastructure to send a request to all consumer. The
                    // request asks the consumers to decide, whether they would visit this Store
                    // over the Store they currently frequent. We only care about the positive
                    // responses (INFORM) and ignore the rest.
                    dynamicBehavior = new AchieveREInitiator(store, inquiry) {
                        protected void handleInform(ACLMessage inform) {
                            int[] quality;

                            // Unpack array from message data
                            try {
                                Serializable s = inform.getContentObject();
                                if (s instanceof int[])
                                    quality = (int[])s;
                                else
                                    return;
                            } catch (UnreadableException ex) {
                                ex.printStackTrace();
                                return;
                            }

                            // Add message data to global array
                            for (int i = 0; i < quality.length; i++)
                                consumerAgreements[i] += quality[i];
                        }
                    };
                    Inquiry.this.addSubBehaviour(dynamicBehavior);
                }
            });
        }

        @Override
        public int onEnd() {
            if (dynamicBehavior != null)
                removeSubBehaviour(dynamicBehavior);
            reset();
            return super.onEnd();
        }
    }

    private class Move extends OneShotBehaviour {
        private final AID topic;
        private int exitValue = 0;

        private Move() {
            // Get a topic helper for topic creation. We want to listen for a
            // certain topic, in order to be notified when a Store changes its position
            TopicManagementHelper topicHelper = null;
            try {
                topicHelper = (TopicManagementHelper)store.getHelper(TopicManagementHelper.SERVICE_NAME);
            } catch (ServiceException ex) {
                ex.printStackTrace();
            }

            // Set the topic to the Store notification topic
            if (topicHelper != null)
                topic = topicHelper.createTopic("store-update-notify");
            else
                topic = null;
        }

        @Override
        public void action() {
            // Find location with maximum agreement
            int maxAgree = 0;
            int maxIndex = 0;
            for (int i = 0; i < newLocations.length; i++) {
                if (consumerAgreements[i] > maxAgree) {
                    maxAgree = consumerAgreements[i];
                    maxIndex = i;
                }
            }

            // Check if maximum agreement is better then current
            // market share, otherwise just do nothing
            if (maxAgree <= store.consumerCount) {
                // We need to set the market share to the one of
                // the first entry in the array. The first entry is
                // the old location and the share on this location might
                // have changed since last time.
                store.consumerCount = consumerAgreements[0];
                return;
            }

            // Update store
            store.consumerCount = maxAgree;
            store.info = newLocations[maxIndex];

            // Now notify all consumers...
            //System.out.format("Store %s is moving.%n", store.getLocalName());
            if (topic != null) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(topic);

                // Serialize the store info we want to send to the consumers
                try {
                    msg.setContentObject(store.info);
                } catch (IOException ex) {
                    System.out.format("%s: Could not serialize update: %s%n", store.getLocalName(), ex);
                    return;
                }

                store.send(msg);
            }
        }

        @Override
        public int onEnd() {
            return exitValue;
        }
    }

    private class Termination extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.format("%s: Finite state machine reached termination state.%n", store.getLocalName());
        }
    }
}
