package fr.edjaz.microservices.core.review.services;

import fr.edjaz.api.core.review.Review;
import fr.edjaz.api.core.review.ReviewService;
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

    private final ReviewService reviewService;

    @Autowired
    public MessageProcessor(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Review> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Review review = event.getData();
            LOG.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
            reviewService.createReview(review);
            break;

        case DELETE:
            int productId = event.getKey();
            LOG.info("Delete reviews with ProductID: {}", productId);
            reviewService.deleteReviews(productId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
