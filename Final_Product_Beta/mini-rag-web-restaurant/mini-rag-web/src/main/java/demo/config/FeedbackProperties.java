package demo.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
@ConfigurationProperties(prefix = "feedback")
public record FeedbackProperties(@DefaultValue("1000") int maxLength,
                                 @DefaultValue("5") int maxPerWindow,
                                 @DefaultValue("10") int windowMinutes) {}
