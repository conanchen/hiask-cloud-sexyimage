package org.ditto.sexyimage.grpc;

import com.google.gson.Gson;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class SexyGRpcServerBuilderConfigurer extends GRpcServerBuilderConfigurer {
    private final static Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(SexyGRpcServerBuilderConfigurer.class.getName());

    @Override
    public void configure(ServerBuilder<?> serverBuilder) {
        if (serverBuilder instanceof NettyServerBuilder) {
            NettyServerBuilder nettyServerBuilder = (NettyServerBuilder) serverBuilder;
            nettyServerBuilder.maxConcurrentCallsPerConnection(3);
//            nettyServerBuilder.permitKeepAliveTime(1, TimeUnit.MINUTES);
            logger.info(String.format("configure done"));
        }
//            serverBuilder
//                .executor(YOUR EXECUTOR INSTANCE)
//                .compressorRegistry(YOUR COMPRESSION REGISTRY)
//                .decompressorRegistry(YOUR DECOMPRESSION REGISTRY)
//                .useTransportSecurity(YOUR TRANSPORT SECURITY SETTINGS);

    }
}