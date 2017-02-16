package io.schmohl.searchbird.index;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Composite Index is an index the routes requests to multiple remote indices;
 */
public class CompositeIndex implements Index {
    private Collection<Index> indices;

    public CompositeIndex(Collection<Index> indices) {
        this.indices = indices;
        assert(indices.size() > 0);
    }

    public Optional<String> get(String key) {
        return indices.stream()
            .map( idx -> idx.get(key) )
            .flatMap( o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty()) // monadic bind.... this will be cleaner in JDK 9.
            .findFirst();
    }

    public String put(String key, String value) {
        throw new UnsupportedOperationException("'put' not supported by CompositeIndex");
    }

    public List<String> search(String query) {
        Set<String> results = indices.stream()
                .flatMap(idx -> idx.search(query).stream())
                .collect(
                        HashSet::new, // mutable reduction... get used to it!
                        (acc, n) -> {
                            if (!acc.contains(n)) acc.add(n);
                        },
                        HashSet::addAll);

        return results.stream().collect(Collectors.toList());
    }

}
