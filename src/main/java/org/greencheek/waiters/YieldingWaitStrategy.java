package org.greencheek.waiters;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Map;

/**
 * User: dominictootell
 * Date: 24/02/2013
 * Time: 11:13
 */
public class YieldingWaitStrategy implements WaitStrategy {
    private static final int SPIN_TRIES = 100;

    public int dowait(int counter) {
        if (0 == counter)
        {
            Thread.yield();
        }
        else
        {
            --counter;
        }
        return counter;
    }

    @Override
    public void dowait(Map<List<String>, Object> cache, List<String> key, Object pendingGenerationMarker) {
        int counter = SPIN_TRIES;
       while(cache.get(key) == pendingGenerationMarker) {
           counter = dowait(counter);
       }
    }
}
