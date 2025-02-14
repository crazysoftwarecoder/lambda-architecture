package org.emitters;

import java.util.Map;

public interface RandomEventCreator<T> {
    Map<T, String> emitEvents();
}