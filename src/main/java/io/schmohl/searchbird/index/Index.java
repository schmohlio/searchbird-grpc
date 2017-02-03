package io.schmohl.searchbird.index;

import java.util.List;
import java.util.Optional;

/*
 * The Index interface.
 * ... We could have used an interface, but probably ok to just have single inheritance.
 */
abstract public class Index {
    abstract public Optional<String> get(String key);
    abstract public String put(String key, String value);
    abstract public List<String> search(String query);
}
