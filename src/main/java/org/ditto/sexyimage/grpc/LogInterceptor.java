package org.ditto.sexyimage.grpc;

import com.google.gson.Gson;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by jamessmith on 9/7/16.
 */
@Slf4j
@Component
public class LogInterceptor implements ServerInterceptor {

    private static final Gson gson = new Gson();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        log.info(String.format(
                "call.getMethodDescriptor().getFullMethodName()=[%s]\n headers.keys=[%s],\n remote=%s",
                call.getMethodDescriptor().getFullMethodName(),
                "-" + gson.toJson(headers.keys()),
                call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)
                )
        );
//TODO: https://stackoverflow.com/questions/40112374/how-do-i-access-request-metadata-for-a-java-grpc-service-i-am-defining
        return next.startCall(call, headers);
    }
}
