package io.schmohl.searchbird.index;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        Stream<String> hits = tokens
                .flatMap ( token -> inverted.computeIfAbsent(token, k -> new LinkedHashSet<>()).stream() );

        // "mutable reduction"
        Set<String> intersected = hits
                .collect(HashSet::new, (acc, n) -> { if (!acc.contains(n)) acc.add(n); }, (s1, s2) -> s1.addAll(s2));

        return intersected.stream().collect(Collectors.toList());
    }
}
