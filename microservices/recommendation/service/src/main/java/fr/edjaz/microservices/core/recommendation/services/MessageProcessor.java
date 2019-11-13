package fr.edjaz.microservices.core.recommendation.services;

import fr.edjaz.api.core.recommendation.Recommendation;
import fr.edjaz.api.core.recommendation.RecommendationService;
import fr.edjaz.api.event.Event;
import fr.edjaz.util.exceptions.EventProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@EnableBinding(Sink.class)
@Slf4j
public class MessageProcessor {

    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessor(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Recommendation> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Recommendation recommendation = event.getData();
            LOG.info("Create recommendation with ID: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
            recommendationService.createRecommendation(recommendation);
            break;

        case DELETE:
            int productId = event.getKey();
            LOG.info("Delete recommendations with ProductID: {}", productId);
            recommendationService.deleteRecommendations(productId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
