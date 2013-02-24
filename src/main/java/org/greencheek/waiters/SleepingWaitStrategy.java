package org.greencheek.waiters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

/**
 * User: dominictootell
 * Date: 24/02/2013
 * Time: 11:13
 */
public class SleepingWaitStrategy implements WaitStrategy {
    private static final int SPIN_TRIES = 200;

    public int dowait(int counter) {
        if (counter > 100)
        {
            --counter;
        }
        else if (counter > 0)
        {
            --counter;
            Thread.yield();
        }
        else
        {
            LockSupport.parkNanos(1L);
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
