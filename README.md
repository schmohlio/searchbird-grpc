# Searchbird for GRPC

Twitter Scala School provides an great introduction to *Scala+Finagle*s with a sample sharded client-server search index, amicably called _Searchbird_

twitter.github.io/scala_school/searchbird.html

This provides a port of the implementation using *Java8+GRPC*. The idea is that the concepts that are well explained in Scala School's tutorial can be applied
to gRPC and Java.

The project also showcases Java 8's *streams* API idioms for porting the Scala code.

## TODO

* finish server (main thread) that is configured as distinguished node or shard i node.
* experiment with futures outside of buffered search results
* tests.
