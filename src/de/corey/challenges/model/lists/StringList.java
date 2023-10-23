package de.corey.challenges.model.lists;

import lombok.*;

import java.util.ArrayList;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public abstract class StringList<T> extends ArrayList<String> {

    private final int limit;

    public StringList() {
        this.limit = Integer.MAX_VALUE;
    }

    public abstract Stream<String> specification();

    public abstract Stream<T> tStream();
}
