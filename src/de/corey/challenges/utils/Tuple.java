package de.corey.challenges.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tuple<T, U> {

    private T t;

    private U u;
}
