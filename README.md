# Searchbird for gRPC and Java

Twitter Scala School provides an great introduction to *Scala + Finagle + Thrift* by walking through the design of a simple service amicably named _Searchbird_. This sharded, client-server search index is remniscent of memcached.

I highly recommend reading it [here](https://twitter.github.io/scala_school/searchbird.html Scala School Finagle Example) first.

This provides a port of the implementation using *Java8+GRPC*. The idea is that the concepts that are well explained in Scala School's tutorial can be applied
to gRPC and Java.

The project also showcases Java 8's *streams* API idioms for porting the Scala code.

Courtesy of Twitter Scala School, here is a diagram that depicts the architecture:
![alt text](https://twitter.github.io/scala_school/searchbird-3.svg "Searchbird Architecture")

## Run Sample Application

The sample application creates a few different servers on various ports on localhost, whereby 1 server is the "distinguished node." 

`gradle run`

_TODO: add example clients connecting and interacting_

## Tests

_todo_
