package org.greencheek;

import java.util.concurrent.atomic.AtomicLong;

public class PaddedAtomicLong extends AtomicLong
{
    public PaddedAtomicLong()
    {
    }

    public PaddedAtomicLong(final long initialValue)
    {
        super(initialValue);
    }

    public volatile long p1, p2, p3, p4, p5, p6 = 7;
}