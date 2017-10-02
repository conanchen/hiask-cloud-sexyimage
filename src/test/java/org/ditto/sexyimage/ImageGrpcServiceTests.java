package org.ditto.sexyimage;

import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.sexyeditor.image.ImageGrpc;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.LocalManagementPort;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationSpringBoot.class, TestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageGrpcServiceTests {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(ImageGrpcServiceTests.class.getName());
    @LocalServerPort
    int randomServerPort;

    @LocalManagementPort
    int randomManagementPort;

    private ManagedChannel channel;
    private ImageGrpc.ImageStub asyncStub;

    @Value("${grpc.port}")
    int grpcPort;


    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Autowired
    @Qualifier("globalInterceptor")
    private ServerInterceptor globalInterceptor;


    @Before
    public void setup() {
        channel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
                .usePlaintext(true)
                .build();
        asyncStub = ImageGrpc.newStub(channel);
    }

    @After
    public void tearDown() {
        channel.shutdown();
    }


    @Test
    public void interceptors() throws ExecutionException, InterruptedException {

        StreamObserver<ImageOuterClass.ToprankImageRequest> imageRequestStreamObserver =
                asyncStub
                        .withWaitForReady()
                        .listToprankImages(new StreamObserver<ImageOuterClass.ImageResponse>() {
                            @Override
                            public void onNext(ImageOuterClass.ImageResponse value) {
                                logger.info(String.format("onNext(ImageOuterClass.ImageResponse value) value=[%s]", gson.toJson(value)));
                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        });

        imageRequestStreamObserver.onNext(ImageOuterClass.ToprankImageRequest
                .newBuilder()
                .addAllTypes(new ArrayList<ImageOuterClass.ImageType>() {{
                    add(ImageOuterClass.ImageType.NORMAL);
                }})
                .build());

        // global interceptor should be invoked once on each service
        Mockito.verify(globalInterceptor, Mockito.times(1))
                .interceptCall(Mockito.any(), Mockito.any(), Mockito.any());


        // log interceptor should be invoked only on GreeterService and not CalculatorService
        outputCapture.expect(CoreMatchers.containsString(ImageGrpc.METHOD_LIST_TOPRANK_IMAGES.getFullMethodName()));

    }


    @Test
    public void healthCheck() throws ExecutionException, InterruptedException {
        final HealthCheckRequest healthCheckRequest = HealthCheckRequest.newBuilder().setService(ImageGrpc.getServiceDescriptor().getName()).build();
        final HealthGrpc.HealthFutureStub healthFutureStub = HealthGrpc.newFutureStub(channel);
        final HealthCheckResponse.ServingStatus servingStatus = healthFutureStub.check(healthCheckRequest).get().getStatus();
        assertNotNull(servingStatus);
        assertEquals(servingStatus, HealthCheckResponse.ServingStatus.SERVING);
    }
}
