package org.ditto.sexyimage.data;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.ditto.sexyimage.common.grpc.ImageType;
import org.ditto.sexyimage.model.Image;
import org.ditto.sexyimage.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 服务启动执行
 */
@Component
@Slf4j
public class SexyImageDataImportRunner implements CommandLineRunner {
    private final static Gson gson = new Gson();
    @Autowired
    private Ignite ignite;


    @Autowired
    private ImageRepository imageRepository;


    @Override
    public void run(String... args) throws Exception {
        log.info("Start SexyImageDataImportRunner 服务启动执行，执行Image数据导入");
        Image image;

        for (int i = 0; i < 10; i++) {
            image = Image
                    .builder()
                    .setUrl("http://n.7k7kimg.cn/2013/0316/1363403616970.jpg?" + i)
                    .setInfoUrl("http://www.baidu.com")
                    .setTitle(String.format("%d NORMAL image",i))
                    .setType(ImageType.NORMAL)
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
            image = imageRepository.save(image.getUrl(), image);
            log.info(String.format("imageRepository.save(%s), image=[%s])", image.getUrl(), gson.toJson(image)));

            image = Image
                    .builder()
                    .setUrl("https://imgcache.cjmx.com/star/201512/20151201213056390.jpg?" + i)
                    .setInfoUrl("http://www.qq.com")
                    .setTitle(String.format("%d POSTER image",i))
                    .setType(ImageType.POSTER)
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
            image = imageRepository.save(image.getUrl(), image);
            log.info(String.format("imageRepository.save(%s), image=[%s])", image.getUrl(), gson.toJson(image)));

            image = Image
                    .builder()
                    .setUrl("http://n.7k7kimg.cn/2013/0316/1363403583271.jpg?" + i)
                    .setInfoUrl("http://www.sohu.com")
                    .setTitle(String.format("%d SEXY image",i))
                    .setType(ImageType.SEXY)
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
            image = imageRepository.save(image.getUrl(), image);
            log.info(String.format("imageRepository.save(%s), image=[%s])", image.getUrl(), gson.toJson(image)));

            image = Image
                    .builder()
                    .setUrl("http://www.zjol.com.cn/pic/0/01/35/25/1352581_955017.jpg?" + i)
                    .setInfoUrl("http://www.163.com")
                    .setTitle(String.format("%d PORN image",i))
                    .setType(ImageType.PORN)
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
            image = imageRepository.save(image.getUrl(), image);
            log.info(String.format("imageRepository.save(%s), image=[%s])", image.getUrl(), gson.toJson(image)));

            image = Image
                    .builder()
                    .setUrl("https://in.bookmyshow.com/entertainment/wp-content/uploads/Tamanna-feature.jpg?" + i)
                    .setInfoUrl("http://www.ifeng.com")
                    .setTitle(String.format("%d SECRET image",i))
                    .setType(ImageType.SECRET)
                    .setLastUpdated(System.currentTimeMillis())
                    .build();
            image = imageRepository.save(image.getUrl(), image);
            log.info(String.format("imageRepository.save(%s), image=[%s])", image.getUrl(), gson.toJson(image)));
        }
        log.info("End   SexyImageDataImportRunner 服务启动执行，执行Image数据导入");
    }
}
