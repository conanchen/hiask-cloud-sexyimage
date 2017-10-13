package org.ditto.sexyimage.grpc;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import org.apache.commons.validator.routines.UrlValidator;
import org.ditto.sexyimage.common.grpc.ImageResponse;
import org.ditto.sexyimage.common.grpc.StatusResponse;
import org.ditto.sexyimage.manage.grpc.DeleteRequest;
import org.ditto.sexyimage.manage.grpc.ImageManGrpc;
import org.ditto.sexyimage.manage.grpc.ListRequest;
import org.ditto.sexyimage.manage.grpc.UpsertRequest;
import org.ditto.sexyimage.model.Image;
import org.ditto.sexyimage.repository.ImageRepository;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.ditto.sexyimage.common.grpc.Error;

import java.util.List;
import java.util.logging.Logger;

@GRpcService(interceptors = {LogInterceptor.class})
public class ImageManService extends ImageManGrpc.ImageManImplBase {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(ImageManService.class.getName());

    @Autowired
    private ImageRepository imageRepository;
    private UrlValidator urlValidator = new UrlValidator();

    @Override
    public void list(ListRequest request, StreamObserver<ImageResponse> responseObserver) {
        List<Image> imageList = imageRepository.getAllBy(request.getType(), request.getLastUpdated(), new PageRequest(0, 10));
        if (imageList != null) {
            logger.info(String.format("ListRequest request=[%s]\n send imageList.size()=%d", gson.toJson(request), imageList.size()));
            for (Image im : imageList) {

                ImageResponse response = ImageResponse
                        .newBuilder()
                        .setUrl(im.getUrl())
                        .setInfoUrl(im.getInfoUrl())
                        .setType(im.getType())
                        .setTitle(im.getTitle() == null ? "" : im.getTitle())
                        .setDesc(im.getDesc() == null ? "" : im.getDesc())
                        .setLastUpdated(im.getLastUpdated())
                        .setActive(im.isActive())
                        .build();
                responseObserver.onNext(response);
                logger.info(String.format("send image.url=%s image=[%s]", im.getUrl(), gson.toJson(im)));
            }
            responseObserver.onCompleted();
        }
    }

    @Override
    public void upsert(UpsertRequest request, StreamObserver<StatusResponse> responseObserver) {
        if (urlValidator.isValid(request.getUrl())) {
            // save and increase visitCount
            Image im = imageRepository.findOne(request.getUrl());
            if (im == null) {
                im = Image
                        .builder()
                        .setUrl(request.getUrl())
                        .setInfoUrl(request.getInfoUrl())
                        .setTitle(request.getTitle())
                        .setDesc(request.getDesc())
                        .setType(request.getType())
                        .setLastUpdated(System.currentTimeMillis())
                        .setActive(request.getActive())
                        .setToprank(request.getToprank())
                        .setVisitCount(0)
                        .build();
            } else {
                if (request.getInfoUrl() != null) im.setInfoUrl(request.getInfoUrl());
                if (request.getTitle() != null) im.setTitle(request.getTitle());
                if (request.getDesc() != null) im.setDesc(request.getDesc());

                im.setType(request.getType());
                im.setLastUpdated(System.currentTimeMillis());
                im.setActive(request.getActive());
                im.setToprank(request.getToprank());
            }
            logger.info(String.format("Upsert image url=%s visitCount=%d", im.getUrl(), im.getVisitCount()));
            im = imageRepository.save(im.getUrl(), im);

            responseObserver.onNext(StatusResponse
                    .newBuilder()
                    .setError(Error
                            .newBuilder()
                            .setCode("image.upsert.ok")
                            .build())
                    .build());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void delete(DeleteRequest request, StreamObserver<StatusResponse> responseObserver) {
        String url = request.getUrl();
        logger.info(String.format("Delete image url=%s ", url));
        imageRepository.delete(url);
        responseObserver.onNext(StatusResponse
                .newBuilder()
                .setError(Error
                        .newBuilder()
                        .setCode("OK")
                        .build())
                .build());
        responseObserver.onCompleted();
    }
}