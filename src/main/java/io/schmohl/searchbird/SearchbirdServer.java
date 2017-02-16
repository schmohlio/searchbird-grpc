package io.schmohl.searchbird;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import io.schmohl.searchbird.index.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Either a "distinguished server" or shard
 **/
public class SearchbirdServer {
    private static final Logger logger = Logger.getLogger(SearchbirdServer.class.getName());

    private final List<String> shards; // addresses of shards. for every service.
    private final Optional<Integer> shard; // if shard, maps to the collection of shards.
    private final Server server;

    private SearchbirdServer(
            List<String> shards,
            Optional<Integer> shard,
            SearchbirdImpl searchbird,
            Server server) {
        this.shards = shards;
        this.shard = shard;
        this.server = server;
    }

    public static class Builder {
        private final List<String> nestedShards; // addresses of shards. for every service.
        private Optional<Integer> nestedShard; // if shard, maps to the collection of shards.
        private int port;

        public Builder(List<String> shards, int distinguishedPort) {
            nestedShards = shards;
            nestedShard = Optional.empty();
            port = distinguishedPort;
        }

        // declaring a shard number overrides the default port.
        public Builder shardNum(int i) {
            nestedShard = Optional.of(i);
            port = Integer.parseInt(nestedShards.get(i).split(":")[1]);
            return this;
        }

        public SearchbirdServer build() {
            Index index;
            if (nestedShard.isPresent())
                index = new ResidentIndex();
            else {
                Collection<Index> remotes = nestedShards.stream()
                        .map(x -> x.split(":"))
                        .map(addr -> new RemoteIndex(addr[0], Integer.parseInt(addr[1])))
                        .collect(Collectors.toList());
                index = new CompositeIndex(remotes);
            }

            SearchbirdImpl searchbird = new SearchbirdImpl(index);
            ServerBuilder sb = ServerBuilder.forPort(port).addService(searchbird);

            return new SearchbirdServer(
                    nestedShards,
                    nestedShard,
                    searchbird,
                    sb.build()
            );
        }
    }

    public void start() throws IOException {
        System.out.println("starting searchbird with node " + shard);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down Searchbird!");
                SearchbirdServer.this.stop();
                System.err.println("*** searchbird shut down!");
            }
        });
    }

    public void stop() {
        if (!server.isShutdown())
            server.shutdown();
    }

    public void blockUntilShutdown() throws InterruptedException { server.awaitTermination(); }

}
