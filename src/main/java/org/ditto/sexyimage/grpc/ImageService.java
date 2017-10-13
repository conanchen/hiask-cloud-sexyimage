package org.ditto.sexyimage.grpc;

import com.google.gson.Gson;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.livesexyeditor.grpc.ImageGrpc;
import net.intellij.plugins.livesexyeditor.grpc.SubscribeRequest;
import net.intellij.plugins.livesexyeditor.grpc.VisitRequest;
import net.intellij.plugins.livesexyeditor.grpc.VisitResponse;
import org.apache.commons.validator.routines.UrlValidator;
import org.ditto.sexyimage.common.grpc.Error;
import org.ditto.sexyimage.common.grpc.ImageResponse;
import org.ditto.sexyimage.common.grpc.ImageType;
import org.ditto.sexyimage.model.Image;
import org.ditto.sexyimage.repository.ImageRepository;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.fn.Consumer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static reactor.bus.selector.Selectors.$;

@GRpcService(interceptors = {LogInterceptor.class})
public class ImageService extends ImageGrpc.ImageImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(ImageService.class.getName());

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private EventBus eventBus;


    private UrlValidator urlValidator = new UrlValidator();
    private static LinkedHashSet<StreamObserver<ImageResponse>> responseObservers = new LinkedHashSet<>();


    @Override
    public void subscribe(SubscribeRequest request, StreamObserver<ImageResponse> responseObserver) {
        logger.info(String.format("subscribeImages request=[%s]", gson.toJson(request.getTypesList())));
        ServerCallStreamObserver serverCallStreamObserver = (ServerCallStreamObserver) responseObserver;
        class MyRunnable implements Runnable {
            Registration toprankImagesRegistration;

            @Override
            public void run() {
                logger.info(String.format("subscribeImages.setOnReadyHandler request=[%s]", gson.toJson(request.getTypesList())));
                nthBatchImagesForTheSubscription(serverCallStreamObserver, request.getTypesList(), 0, 5);

                toprankImagesRegistration = eventBus.on($("quotes"), new Consumer<Event<?>>() {
                    @Override
                    public void accept(Event<?> event) {
                        nthBatchImagesForTheSubscription(serverCallStreamObserver, request.getTypesList(), 0, 10);
                    }
                });
            }
        }

        MyRunnable myRunnable = new MyRunnable();
        serverCallStreamObserver.setOnReadyHandler(myRunnable);
        serverCallStreamObserver.setOnCancelHandler(() -> {
            if (myRunnable.toprankImagesRegistration != null) {
                logger.info(String.format("subscribeImages.setOnCancelHandler request=[%s]", gson.toJson(request.getTypesList())));
                myRunnable.toprankImagesRegistration.cancel();
            }
        });

    }

    final CountDownLatch finishLatch = new CountDownLatch(1);

    private void nthBatchImagesForTheSubscription(ServerCallStreamObserver<ImageResponse> serverCallStreamObserver,
                                                  List<ImageType> typesList, int page, int pageSize) {
        //start first batch images for the subscrition
        List dbimages = new ArrayList<>();
        for (ImageType it : typesList) {
            List<Image> images = imageRepository.getTopRankBy(it, new PageRequest(page, pageSize));
            dbimages.addAll(images);
        }

        sendImages(serverCallStreamObserver, dbimages);
        //end first batch images for the subscrition
    }

    private void sendImages(ServerCallStreamObserver<ImageResponse> serverCallStreamObserver, List<Image> images) {
        if (images != null) {
            for (Image im : images) {
                if (!serverCallStreamObserver.isReady()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.info(e.getMessage());
                    }
                }
                if (serverCallStreamObserver.isReady()) {
                    ImageResponse response = ImageResponse
                            .newBuilder()
                            .setUrl(im.getUrl())
                            .setInfoUrl(im.getInfoUrl())
                            .setType(im.getType())
                            .build();
                    serverCallStreamObserver.onNext(response);
                    logger.info(String.format("serverCallStreamObserver=%s SendImages onNext url=[%s]\n infoUrl=[%s]",
                            serverCallStreamObserver.toString(), im.getUrl(),im.getInfoUrl()));
                } else {
                    logger.info(String.format("SendImages serverCallStreamObserver is not ready, discard url=[%s]", im.getUrl()));
                }
            }
        }
    }

    @Override
    public void visit(VisitRequest request, StreamObserver<VisitResponse> responseObserver) {

        logger.fine(String.format("VisitRequest.url=%s", request.getUrl()));
        if (urlValidator.isValid(request.getUrl())) {
            // save and increase visitCount
            Image im = imageRepository.findByUrl(request.getUrl());
            if (im == null) {
                im = Image
                        .builder()
                        .setUrl(request.getUrl())
                        .setInfoUrl(request.getUrl())
                        .setTitle(String.format("%s",request.getUrl()))
                        .setType(ImageType.SECRET)
                        .setToprank(false)
                        .setActive(false)
                        .setCreated(System.currentTimeMillis())
                        .setLastUpdated(System.currentTimeMillis())
                        .setVisitCount(1)
                        .build();
            } else {
                im.setVisitCount(im.getVisitCount() + 1);
                im.setLastUpdated(System.currentTimeMillis());
            }
            logger.info(String.format("Visit image url=%s visitCount=%d", im.getUrl(), im.getVisitCount()));
            im = imageRepository.save(im.getUrl(), im);
            responseObserver.onNext(VisitResponse
                    .newBuilder()
                    .setError(Error
                            .newBuilder()
                            .setCode("image.visit.ok")
                            .setDetails(String.format("visitCount=%d", im.getVisitCount()))
                            .build())
                    .build());
            responseObserver.onCompleted();
        }
    }
}