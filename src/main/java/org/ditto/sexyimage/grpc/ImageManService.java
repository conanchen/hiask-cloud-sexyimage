package org.ditto.sexyimage.grpc;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import net.intellij.plugins.sexyeditor.image.ImageOuterClass;
import org.apache.commons.validator.routines.UrlValidator;
import org.ditto.sexyimage.model.Image;
import org.ditto.sexyimage.repository.ImageRepository;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

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
    public void list(Imageman.ListRequest request, StreamObserver<Common.ImageResponse> responseObserver) {
        List<Image> imageList = imageRepository.getAllBy(request.getType(), request.getLastUpdated(), new PageRequest(0, 10));
        if (imageList != null) {
            logger.info(String.format("send imageList.size()=%d", imageList.size()));
            for (Image im : imageList) {

                Common.ImageResponse response = Common.ImageResponse
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
                logger.info(String.format("send image.url=%s image=[%s]", im.getUrl(),gson.toJson(im)));
            }
            responseObserver.onCompleted();
        }
    }

    @Override
    public void upsert(Imageman.UpsertRequest request, StreamObserver<Common.StatusResponse> responseObserver) {
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
                if (request.getInfoUrl()!=null) im.setInfoUrl(request.getInfoUrl());
                if (request.getTitle()!=null) im.setTitle(request.getTitle());
                if (request.getDesc()!=null) im.setDesc(request.getDesc());

                im.setType(request.getType());
                im.setLastUpdated(System.currentTimeMillis());
                im.setActive(request.getActive());
                im.setToprank(request.getToprank());
            }
            logger.info(String.format("Upsert image url=%s visitCount=%d", im.getUrl(), im.getVisitCount()));
            im = imageRepository.save(im.getUrl(), im);

            responseObserver.onNext(Common.StatusResponse
                    .newBuilder()
                    .setError(Common.Error
                            .newBuilder()
                            .setCode("image.upsert.ok")
                            .build())
                    .build());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void delete(Imageman.DeleteRequest request, StreamObserver<Common.StatusResponse> responseObserver) {
        String url = request.getUrl();
        logger.info(String.format("Delete image url=%s ", url));
        imageRepository.delete(url);
        responseObserver.onNext(Common.StatusResponse
                .newBuilder()
                .setError(Common.Error
                        .newBuilder()
                        .setCode("OK")
                        .build())
                .build());
        responseObserver.onCompleted();
    }
}