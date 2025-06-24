package es.ulpgc.dacd.newsapi.infrastructure.adapters.storage.ActiveMQ;

public class JmsConfig {
    public final String brokerUrl;
    public final String queueName;
    public final String topicName;

    public JmsConfig(String brokerUrl, String queueName, String topicName) {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
        this.topicName = topicName;
    }
}

