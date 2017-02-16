package io.schmohl.searchbird.index;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import io.schmohl.searchbird.*;


public class RemoteIndex implements Index {
    private static final Logger logger = Logger.getLogger(RemoteIndex.class.getName());

    private String host;
    private int port;
    private SearchbirdGrpc.SearchbirdBlockingStub blockingStub;

    public RemoteIndex(String host, int port) {
        this.host = host;
        this.port = port;
        blockingStub = SearchbirdGrpc.newBlockingStub(ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext(true)
            .build()
        );
    }

    public Optional<String> get(String key) {
        GetRequest req = GetRequest.newBuilder().setKey(key).build();
        GetResponse res = null;
        try {
            res = blockingStub.get(req);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return Optional.empty();
        }
        if (!res.getFound())
            return Optional.empty();
        else
            return Optional.of(res.getValue());
    }

    public String put(String key, String value) {
        PutRequest req = PutRequest.newBuilder().setKey(key).setValue(value).build();
        PutResponse res = null;
        try {
            res = blockingStub.put(req);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }
        return res.getPrev();
    }

    public List<String> search(String query) {
        List<String> results = new LinkedList<>();

        try {
            SearchRequest req = SearchRequest.newBuilder().setQuery(query).build();
            Iterator<SearchResponseBatch> res = blockingStub.search(req);
            while (res.hasNext()) {
                results.addAll(res.next().getKeyList());
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return results;
        }
        return results;
    }
}

