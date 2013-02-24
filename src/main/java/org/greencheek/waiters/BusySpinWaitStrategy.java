package org.greencheek.waiters;

import java.util.List;
import java.util.Map;

/**
 * User: dominictootell
 * Date: 24/02/2013
 * Time: 12:52
 */
public class BusySpinWaitStrategy implements WaitStrategy {
    @Override
    public void dowait(Map<List<String>, Object> cache, List<String> key, Object spinMatchingValue) {
        while(cache.get(key) == spinMatchingValue) {
            // wait busy spin
        }
    }
}
