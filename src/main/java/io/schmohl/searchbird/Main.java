package io.schmohl.searchbird;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// Example boot of server running on localhost with different ports
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        // the port that the distinguished node will run on.
        int distinguishedPort = 50051;

        // the shards addresses
        // the global config, probably pulled from somewhere like Consul;
        List<String> shards = Arrays.asList("localhost:50052", "localhost:50053");

        SearchbirdServer.Builder builder;

        // launch the distinguished node
        builder = new SearchbirdServer.Builder(shards, distinguishedPort);
        final SearchbirdServer distinguishedNode = builder.build();

        // launch remote0
        builder = new SearchbirdServer.Builder(shards, distinguishedPort);
        builder.shardNum(0);
        final SearchbirdServer remote0 = builder.build();

        // launch remote1
        builder = new SearchbirdServer.Builder(shards, distinguishedPort);
        builder.shardNum(1);
        final SearchbirdServer remote1 = builder.build();

        System.out.println("start searchbird!");
        distinguishedNode.start();
        remote0.start();
        remote1.start();

        distinguishedNode.blockUntilShutdown();
    }
}
