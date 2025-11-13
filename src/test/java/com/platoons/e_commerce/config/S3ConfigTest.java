package com.platoons.e_commerce.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class S3ConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(S3Config.class);

    @Test
    void createsS3ClientBeanWhenProfileIsNotTest() {
        contextRunner
                .withPropertyValues(
                        "aws.access-key=AKIA_TEST",
                        "aws.secret-key=SECRET",
                        "aws.region=eu-west-1")
                .run(context -> {
                    Map<String, S3Client> beans = context.getBeansOfType(S3Client.class);
                    assertThat(beans).hasSize(1);
                });
    }

    @Test
    void excludesS3ClientBeanWhenProfileIsTest() {
        contextRunner
                .withPropertyValues(
                        "spring.profiles.active=test",
                        "aws.access-key=AKIA_TEST",
                        "aws.secret-key=SECRET",
                        "aws.region=eu-west-1")
                .run(context -> {
                    assertThat(context.getBeanNamesForType(S3Client.class)).isEmpty();
                });
    }

    @Test
    void s3ClientUsesConfiguredRegionInUtilities() {
        contextRunner
                .withPropertyValues(
                        "aws.access-key=AKIA_TEST",
                        "aws.secret-key=SECRET",
                        "aws.region=eu-west-1")
                .run(context -> {
                    S3Client client = context.getBean(S3Client.class);
                    String url = client.utilities().getUrl(
                            GetUrlRequest.builder().bucket("bucket").key("key").build()
                    ).toExternalForm();
                    assertThat(url).contains("eu-west-1");
                });
    }
}
