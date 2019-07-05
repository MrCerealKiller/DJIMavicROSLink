package ca.mcgill.cim.djimavicroslink.services;

import android.util.Log;

import org.ros.android.MessageCallable;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

public class Listener<T> extends AbstractNodeMain {

    private static final String TAG = "Listener";

    private String topicName;
    private String messageType;
    private T      lastMessage;
    private MessageCallable<String, T> callable;

    public Listener(String topic, String type) {
        topicName = topic;
        messageType = type;
    }

    public T getLastMessage() {
        return lastMessage;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setMessageToStringCallable(MessageCallable<String, T> callable) {
        this.callable = callable;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("get_" + topicName + "_node");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<T> subscriber = connectedNode.newSubscriber(topicName, messageType);
        subscriber.addMessageListener(new MessageListener<T>() {
            @Override
            public void onNewMessage(final T message) {
                lastMessage = message;
                if (callable != null) {
                    callable.call(message);

                }
            }
        });
    }
}