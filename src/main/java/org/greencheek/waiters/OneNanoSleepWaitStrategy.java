package org.greencheek.waiters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

/**
 * User: dominictootell
 * Date: 24/02/2013
 * Time: 12:58
 */
public class OneNanoSleepWaitStrategy implements WaitStrategy
{
    @Override
    public void dowait(Map<List<String>, Object> cache, List<String> key, Object spinMatchingValue) {
        while(cache.get(key) == spinMatchingValue) {
            LockSupport.parkNanos(1l);
        }
    }
}
