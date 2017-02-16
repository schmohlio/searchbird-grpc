package io.schmohl.searchbird.index;

import java.util.List;
import java.util.Optional;

/*
 * The Index interface.
 */
public interface Index {
    Optional<String> get(String key);
    String put(String key, String value);
    List<String> search(String query);
}
