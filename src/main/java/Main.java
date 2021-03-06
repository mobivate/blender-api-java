import com.rawmobility.blender.demo.BulkXmlApi;
import com.rawmobility.blender.demo.GenericXMLMarshaller;
import com.rawmobility.blender.demo.MarshallerFactory;
import com.rawmobility.blender.demo.dto.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

public class Main {
    private final String username;
    private final String password;
    BulkXmlApi api;

    public Main(String username, String password) {
        this.username = username;
        this.password = password;

    }

    public static void main(String[] args) throws Exception {
        Main a = new Main("USERNAME", "PASSWORD");
        String routeId = "ROUTEID?";

        /*
        // Send a message to single recipient
        a.sendSingleMessage("test", "61417188345", "Test MT # $", routeId, "myref");

        // Send the SAME message to a batch
        List<String> recipients = new ArrayList<String>();
        recipients.add("61417188345");
        recipients.add("61400000000");
        //a.sendBatchMessage("test", recipients, "Test MT # $", routeId);

        a.sendBatchToMulti("origin", routeId);


        String xml = "<deliverymessage><created>2011-07-11T11:30:36+0000</created><id>0ed7b241-096e-4ca4-abc3-a365f11fae8f</id><body>Test 11:33</body><inReplyTo>cad1bf26-1294-4076-8b44-1197f2568104</inReplyTo><logicalMessageId>c8aca89b-84e1-42fe-a32c-5050cd6a0590</logicalMessageId><originator>44000009999</originator><recipient>44000000001</recipient></deliverymessage>";
        DeliveryMessage deliveryMessage = a.receiveMessage(xml);
        System.out.println("Received message " + deliveryMessage);


        xml = "<deliveryreceipt><created>2000-12-19T06:29:56+0000</created><deliveryMessageId>2ae13f9d-f5dc-4478-ab17-ba13ddeffad2</deliveryMessageId><status>DELIVERED</status><clientReference>myref</clientReference><part>1</part><parts>1</parts></deliveryreceipt>";
        DeliveryReceipt deliveryReceipt = a.receiveReceipt(xml);
        System.out.println("Received receipt " + deliveryReceipt);
        */



        // Schedule a message to 20 minutes from now
        Date scheduleTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(scheduleTime);
        cal.add(Calendar.MINUTE, 20);
        List<String> recipients = new ArrayList<String>();
        recipients.add("61417188345");

        a.sendScheduledBatchMessage("test", recipients, "Test MT # $", routeId, cal.getTime());


    }

    public BulkXmlApi getApi() {
        if (api != null)
            return api;

        // Initialize the API
        api = new BulkXmlApi(username, password);

        // Login
        if (!api.login()) {
            api = null;
        }

        return api;
    }

    /**
     * @param originator Message originator
     * @param recipient  Recipient, international format (e.g. 61417188345)
     * @param body       Message text, long messages are allowed.
     * @param routeId    Route id
     * @param reference  Client reference, will be sent along with delivery receipt
     * @throws JAXBException
     * @throws IOException
     */
    public void sendSingleMessage(String originator, String recipient, String body, String routeId, String reference)
            throws JAXBException, IOException {

        // Send sms
        XMLResponseWrapper response = getApi().sendSingle(originator, recipient, body, routeId, reference);
        System.out.println("Response: " + response.getBody());
        // Get the id of the sent message
        BatchRecipientMultiBody batchRec = (BatchRecipientMultiBody) response.getBody();

        System.out.println("Submitted, remote id: " + batchRec.getId());

    }

    /**
     * @param originator Message originator
     * @param recipients Recipient list, international format (e.g. 61417188345)
     * @param body       Message text, long messages are allowed.
     * @param routeId    Route id
     * @throws JAXBException
     * @throws IOException
     */
    public void sendBatchMessage(String originator, List<String> recipients, String body, String routeId) throws JAXBException, IOException {
        BatchMessageSingleBody batch = new BatchMessageSingleBody();
        batch.setOriginator(originator);
        batch.setRouteId(routeId);
        batch.setBody(body);

        for (String recipient : recipients) {
            batch.addMSISDN(recipient);
        }

        XMLResponseWrapper xmlResponseWrapper = getApi().sendBatch(batch);
        BatchMessageSingleBody response = (BatchMessageSingleBody) xmlResponseWrapper.getBody();
        System.out.println("Submitted, remote batch id: " + response.getId());
        for (BatchRecipientSingleBody recipient : response.getRecipients()) {
            System.out.println(recipient.getRecipient() + " remote id: " + recipient.getId());
        }
    }

    /**
     * @param originator Message originator
     * @param recipients Recipient list, international format (e.g. 61417188345)
     * @param body       Message text, long messages are allowed.
     * @param routeId    Route id
     * @throws JAXBException
     * @throws IOException
     */
    public void sendScheduledBatchMessage(String originator, List<String> recipients, String body, String routeId, Date schedule) throws JAXBException, IOException {
        BatchMessageSingleBody batch = new BatchMessageSingleBody();
        batch.setOriginator(originator);
        batch.setRouteId(routeId);
        batch.setBody(body);

        // Schedule time & timezone
        batch.setDeliverySchedule(schedule);
        batch.setDeliveryTimeZone(TimeZone.getDefault().getID());

        for (String recipient : recipients) {
            batch.addMSISDN(recipient);
        }

        XMLResponseWrapper xmlResponseWrapper = getApi().sendBatch(batch);
        BatchMessageSingleBody response = (BatchMessageSingleBody) xmlResponseWrapper.getBody();
        System.out.println("Submitted, remote batch id: " + response.getId());
        for (BatchRecipientSingleBody recipient : response.getRecipients()) {
            System.out.println(recipient.getRecipient() + " remote id: " + recipient.getId());
        }
    }

    public void sendBatchToMulti(String originator, String routeId) throws JAXBException, IOException {
        BatchMessageMultiBody batch = new BatchMessageMultiBody();
        batch.setOriginator(originator);
        batch.setRouteId(routeId);

        String recipient = "61417188345";
        String body = "test";
        // This can be set on every recipient, and will be sent back with DR.
        String reference = "myref";
        batch.addRecipient(originator, recipient, body,reference, routeId);
        batch.addRecipient("6140000000", "another body");

        XMLResponseWrapper xmlResponseWrapper = getApi().sendBatch(batch);
        BatchMessageMultiBody response = (BatchMessageMultiBody) xmlResponseWrapper.getBody();

        for (BatchRecipientMultiBody rcpt : response.getRecipients()) {
            System.out.println(rcpt.getRecipient() + " remote id: " + rcpt.getId() + " my id: " + rcpt.getReference());
        }
    }

    public DeliveryReceipt receiveReceipt(String xml) throws JAXBException {
        GenericXMLMarshaller<DeliveryReceipt> marshaller = new GenericXMLMarshaller<DeliveryReceipt>(DeliveryReceipt.class);
        return marshaller.unmarshal(xml);
    }

    public DeliveryMessage receiveMessage(String xml) throws JAXBException {
        GenericXMLMarshaller<DeliveryMessage> marshaller = new GenericXMLMarshaller<DeliveryMessage>(DeliveryMessage.class);
        return marshaller.unmarshal(xml);
    }
}
