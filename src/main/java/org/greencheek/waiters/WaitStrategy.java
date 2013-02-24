package org.greencheek.waiters;

import java.util.List;
import java.util.Map;

/**
 * User: dominictootell
 * Date: 24/02/2013
 * Time: 11:12
 */
public interface WaitStrategy {
    void dowait(Map<List<String>, Object> cache, List<String> key, Object spinMatchingValue);
}
