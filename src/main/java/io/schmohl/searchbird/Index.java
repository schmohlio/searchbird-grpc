package io.schmohl.searchbird;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.stream.Stream.*;

abstract class Index {
    abstract Optional<String> get(String key);
    abstract String put(String key, String value);
    abstract List<String> search(String query);
}

class ResidentIndex extends Index {
    private static final Logger logger = Logger.getLogger(ResidentIndex.class.getName());

    private static final String tokenizer = Pattern.quote(" ");

    private Map<String, String> forward;
    private Map<String, Set<String>> inverted;

    public ResidentIndex() {
        forward = new ConcurrentHashMap<>();
        inverted = new ConcurrentHashMap<>();
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(forward.get(key));
    }

    public String put(String key, String value) {
        String prev = forward.put(key, value);

        synchronized (this) {
            Arrays.stream(value.split(tokenizer))
                    .forEach( token -> {
                        Set<String> current = inverted.computeIfAbsent( token, k -> new LinkedHashSet<>() );
                        current.add(key);
                        inverted.put(token, current);
                    });
        }

        return prev;
    }

    public List<String> search(String query) {
        Stream<String> tokens = Arrays.stream(query.split(tokenizer));

        Stream<Stream<String>> hits =
                tokens.map ( token -> inverted.computeIfAbsent(token, k -> new LinkedHashSet<>()).stream() );

        // concat 2 streams w/ underlying sets more functionally idiomatic than "Mutable Reduction"
        Stream<String> intersected =
                hits.reduce(empty(), (x, y) -> concat(x, y));

        return intersected.collect(Collectors.toList());
    }
}

// TODO:
abstract class CompositeIndex extends Index {
    private Collection<Index> indices;

    public CompositeIndex(Collection<Index> indices) { this.indices = indices; }
}

abstract class RemoteIndex extends Index {
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
        if (res.getFound())
            return Optional.empty();
        else
            return Optional.of(res.getValue());
    }
}

