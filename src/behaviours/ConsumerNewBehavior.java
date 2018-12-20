package behaviours;

import agents.ConsumerNew;
import agents.StoreNew;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;

import java.io.IOException;
import java.io.Serializable;

public class ConsumerNewBehavior extends AchieveREResponder {
    private final ConsumerNew consumer;

    public ConsumerNewBehavior(ConsumerNew consumer) {
        super(consumer, MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        ));
        this.consumer = consumer;
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
        StoreNew.StoreInfo[] info;

        //System.out.format("Consumer %s: Got message '%s'.%n", consumer.getLocalName(),
        //        request.getSender().getLocalName());

        try {
            // Make sure the object in the message is a
            // readable StoreInfo object, so we can compare it
            // with the store saved in the Consumer agent
            Serializable s = request.getContentObject();
            if (s instanceof StoreNew.StoreInfo[])
                info = (StoreNew.StoreInfo[])s;
            else
                throw new NotUnderstoodException("not-store-info");
        } catch (UnreadableException ex) {
            throw new NotUnderstoodException(ex.getMessage());
        }


        // Inform the Store what the consumer thinks about the locations.
        // A 1 in the quality array marks the consumer switching over,
        // a 0 marks the consumer rather sticking with the old location;
        int[] quality = locationQuality(request.getSender(), info);
        //System.out.format("Consumer %s: Location %d, %d%n", consumer.getLocalName(), consumer.consumerInfo.location, quality[0]);
        ACLMessage msg = request.createReply();
        msg.setPerformative(ACLMessage.INFORM);

        // Either serialize the quality array, or send an error message
        try {
            msg.setContentObject(quality);
        } catch (IOException ex) {
            msg.setContent(ex.getMessage());
            msg.setPerformative(ACLMessage.FAILURE);
        }

        return msg;
    }

    private int[] locationQuality(AID store, StoreNew.StoreInfo[] infos) {
        int[] quality = new int[infos.length];

        for (int i = 0; i < infos.length; i++)
            quality[i] = consumer.isBestStore(store, infos[i]) ? 1 : 0;

        return quality;
    }
}
