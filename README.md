**Table of Contents**  *generated with [DocToc](http://doctoc.herokuapp.com/)*

- [References and Thanks to Many Many very smart people](#references-and-thanks-to-many-many-very-smart-people)
 - [bug relating to the Performance of java.lang.reflect.Proxy](#bug-relating-to-the-performance-of-javalangreflectproxy)
 - [Spring issue 3.1.x that relates to above:](#spring-issue-31x-that-relates-to-above:)
 - [Work being done in Java 8](#work-being-done-in-java-8)
- [Synopsis](#synopsis)
 - [Benchmarks](#benchmarks)
  - [Benchmark Results](#benchmark-results)
   - [8 threads, running the 4 tests.](#8-threads-running-the-4-tests)
   - [1-40 threads, running the getProxyClass](#1-40-threads-running-the-getproxyclass)
- [Usage](#usage)
- [Not a solution to Spring performance in 3.0.x or 3.1.x](#not-a-solution-to-spring-performance-in-30x-or-31x)

## References and Thanks to Many Many very smart people

### bug relating to the Performance of java.lang.reflect.Proxy

This is the report of the slow performance of the java.lang.reflect.Proxy getClass and
various methods.  Which use a synchronized {} blocks and synchronized WeakHashMap

http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7123493
https://bugs.openjdk.java.net/show_bug.cgi?id=100120
https://github.com/finn-no/commons-reflection

### Spring issue 3.1.x that relates to above:

This gist shows the affects of java.lang.reflect.Proxy, and the implications it has on the Spring
 framework.

- https://gist.github.com/twicksell/1894670

Spring 3.2.x has caching in place using a ConcurrentReferenceHashMap, and therefore is not
 hindered as much by the issue.  These fixes are tracked in:

- https://jira.springsource.org/browse/SPR-9166
-- https://jira.springsource.org/browse/SPR-8319
-- https://jira.springsource.org/browse/SPR-9298
-- https://jira.springsource.org/browse/SPR-9748

The new ConcurrentReferenceHashMap is:

- https://github.com/SpringSource/spring-framework/blob/master/spring-core/src/main/java/org/springframework/util/ConcurrentReferenceHashMap.java



### Work being done in Java 8

The thread of work relating to this is:
- http://mail.openjdk.java.net/pipermail/core-libs-dev/2013-January/013970.html


## Synopsis


Java's Proxy class has poor performance under multi-threaded environments.
And could lead to what feels like a complete lock of the jvm up.  The synchronized block and map cause contention, where
a thread dump would show something like:

java.lang.Thread.State: BLOCKED (on object monitor)
	at java.lang.reflect.Proxy.getProxyClass(Proxy.java:417)
	- locked <7f45155b0> (a java.util.HashMap)
	at java.lang.reflect.Proxy.newProxyInstance(Proxy.java:581)

This simple library is a replacement of java.lang.reflect.Proxy, for use the bootclasspath mechanism java affords you.
The Proxy is basically a copy of Proxy but changes the caching mechanism from using a HashMap surrounded with hard
synchronization to a ConcurrentMap, using the ConcurrentWeakHashMap that was part of Doug Lea and Jason T. Greene
work for the jsr166 concurrency work.  It is a drop in replacement for the Proxy class.

Licensed under "GPLv3 with Classpath Exception" so that it remains compatible with OpenJDK
(since it is a derivative of the Proxy class).


The Proxy class is an amalgamation of work that has been done by many others:

- Benchmark taken, slightly adapted, from https://github.com/plevart/jdk8-tl [proxy branch]
- Many thanks to Peter Levart

- ConcurrentWeakHashMap taken from http://viewvc.jboss.org/cgi-bin/viewvc.cgi/jbosscache/experimental/jsr166/src/jsr166y/ and the grizzly project.  Done by Doug Lea and Jason T. Greene

- ConcurrentReferenceHashMap taken from https://raw.github.com/jbossas/jboss-as/master/web/src/main/java/org/jboss/as/web/deployment/ConcurrentReferenceHashMap.java
-- http://viewvc.jboss.org/cgi-bin/viewvc.cgi/jbosscache/experimental/jsr166/src/jsr166y/

- Wait Strategy from the Disruptor : https://github.com/LMAX-Exchange/disruptor

- PaddedAtomicLong from Martin Thompson: http://www.mechanical-sympathy.blogspot.co.uk/2011/09/adventures-with-atomiclong.html
-- (https://github.com/mjpt777/examples/blob/master/src/java/uk/co/real_logic/queues/PaddedAtomicLong.java)

## Benchmarks

The bench mark (ProxyBenchmarkTest) fires off multiple threads and tests:

- getProxyClass
- isProxyClass == true
- isProxyClass == false
- Annotation equals

The benchmark initial loads the max of Math.max(4, Runtime.getRuntime().availableProcessors()).
The results are from a 2.5 GHz Core i7 (I7-2860QM) - http://ark.intel.com/products/53476/Intel-Core-i7-2860QM-Processor-%288M-Cache-2_50-GHz%29
4 CPU, 8 Threads (2 per core).  Where the initial test run 8 threads.  Each running for 5seconds, as
many calls as possible to the methods.

The second set of bench marks, just runs getProxyClass, upto a max of 40 threads.


### Benchmark Results


#### 8 threads, running the 4 tests.

```java
Unpatched j.l.r.Proxy															Patched j.l.r.Proxy:

================
 Proxy_getProxyClass: run duration:  5,000 ms, #of logical CPUS: 8

 Warm up:		# Warm up:
================
           1 threads, Tavg =  6,097.78 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =  6,445.19 ns/op (σ =   0.00 ns/op)
           1 threads, Tavg =  6,101.31 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =  6,461.78 ns/op (σ =   0.00 ns/op)
================
* Measure:		* Measure:
================
           1 threads, Tavg =  6,515.80 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =  6,528.96 ns/op (σ =   0.00 ns/op)
           2 threads, Tavg =  7,146.72 ns/op (σ =   9.45 ns/op)				   2 threads, Tavg =  7,281.33 ns/op (σ =  10.80 ns/op)
           3 threads, Tavg =  7,901.80 ns/op (σ =  48.87 ns/op)				   3 threads, Tavg =  7,778.69 ns/op (σ =  41.32 ns/op)
           4 threads, Tavg =  9,243.23 ns/op (σ = 231.54 ns/op)				   4 threads, Tavg =  9,005.95 ns/op (σ =  91.90 ns/op)
           5 threads, Tavg = 10,563.53 ns/op (σ =  69.84 ns/op)				   5 threads, Tavg = 10,123.46 ns/op (σ = 124.77 ns/op)
           6 threads, Tavg = 12,726.19 ns/op (σ =  91.21 ns/op)				   6 threads, Tavg = 11,343.57 ns/op (σ =  80.99 ns/op)
           7 threads, Tavg = 14,481.17 ns/op (σ =  60.58 ns/op)				   7 threads, Tavg = 12,506.20 ns/op (σ = 115.13 ns/op)
           8 threads, Tavg = 20,627.51 ns/op (σ =  35.11 ns/op)				   8 threads, Tavg = 15,428.95 ns/op (σ = 118.15 ns/op)

================
 Proxy_isProxyClassTrue: run duration:  5,000 ms, #of logical CPUS: 8
		#
 Warm up:		# Warm up:
================
           1 threads, Tavg =    203.36 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =    177.00 ns/op (σ =   0.00 ns/op)
           1 threads, Tavg =    203.19 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =    176.73 ns/op (σ =   0.00 ns/op)
================
 Measure:		# Measure:
================
           1 threads, Tavg =    202.55 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =    175.04 ns/op (σ =   0.00 ns/op)
           2 threads, Tavg =  1,159.55 ns/op (σ =  33.85 ns/op)				   2 threads, Tavg =    178.80 ns/op (σ =   0.13 ns/op)
           3 threads, Tavg =  1,330.17 ns/op (σ =  18.12 ns/op)				   3 threads, Tavg =    193.43 ns/op (σ =   1.01 ns/op)
           4 threads, Tavg =  1,862.01 ns/op (σ =  10.74 ns/op)				   4 threads, Tavg =    213.91 ns/op (σ =   2.30 ns/op)
           5 threads, Tavg =  2,232.63 ns/op (σ =  70.57 ns/op)				   5 threads, Tavg =    229.93 ns/op (σ =   2.41 ns/op)
           6 threads, Tavg =  2,562.33 ns/op (σ =  30.75 ns/op)				   6 threads, Tavg =    241.19 ns/op (σ =   1.91 ns/op)
           7 threads, Tavg =  3,127.27 ns/op (σ =  36.54 ns/op)				   7 threads, Tavg =    272.18 ns/op (σ =   1.44 ns/op)
           8 threads, Tavg =  3,398.88 ns/op (σ =  27.67 ns/op)				   8 threads, Tavg =    311.97 ns/op (σ =   2.51 ns/op)

================
 Proxy_isProxyClassFalse: run duration:  5,000 ms, #of logical CPUS: 8
		#
 Warm up:		# Warm up:
================
           1 threads, Tavg =    189.91 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =     85.90 ns/op (σ =   0.00 ns/op)
           1 threads, Tavg =    203.44 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =     85.14 ns/op (σ =   0.00 ns/op)
================
 Measure:		# Measure:
================
           1 threads, Tavg =    201.88 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =     85.59 ns/op (σ =   0.00 ns/op)
           2 threads, Tavg =  1,083.05 ns/op (σ =  12.88 ns/op)				   2 threads, Tavg =     87.92 ns/op (σ =   0.08 ns/op)
           3 threads, Tavg =  1,240.99 ns/op (σ =   3.50 ns/op)				   3 threads, Tavg =     97.51 ns/op (σ =   1.51 ns/op)
           4 threads, Tavg =  1,614.24 ns/op (σ =  10.12 ns/op)				   4 threads, Tavg =    115.32 ns/op (σ =   1.68 ns/op)
           5 threads, Tavg =  2,128.07 ns/op (σ = 267.87 ns/op)				   5 threads, Tavg =    128.16 ns/op (σ =   1.16 ns/op)
           6 threads, Tavg =  2,686.78 ns/op (σ =  27.52 ns/op)				   6 threads, Tavg =    138.33 ns/op (σ =   1.57 ns/op)
           7 threads, Tavg =  3,016.30 ns/op (σ =  17.26 ns/op)				   7 threads, Tavg =    170.35 ns/op (σ =   1.97 ns/op)
           8 threads, Tavg =  3,417.17 ns/op (σ =  22.64 ns/op)				   8 threads, Tavg =    198.74 ns/op (σ =   1.27 ns/op)

================
 Annotation_equals: run duration:  5,000 ms, #of logical CPUS: 8
		#
 Warm up:		# Warm up:
================
           1 threads, Tavg =    466.12 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =    499.94 ns/op (σ =   0.00 ns/op)
           1 threads, Tavg =    465.00 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =    496.47 ns/op (σ =   0.00 ns/op)
================
 Measure:		# Measure:
================
           1 threads, Tavg =    466.98 ns/op (σ =   0.00 ns/op)				   1 threads, Tavg =    497.04 ns/op (σ =   0.00 ns/op)
           2 threads, Tavg =  1,137.61 ns/op (σ =   4.17 ns/op)				   2 threads, Tavg =    508.86 ns/op (σ =   0.32 ns/op)
           3 threads, Tavg =  1,995.22 ns/op (σ =   5.77 ns/op)				   3 threads, Tavg =    554.92 ns/op (σ =   2.80 ns/op)
           4 threads, Tavg =  3,211.46 ns/op (σ =  21.25 ns/op)				   4 threads, Tavg =    620.02 ns/op (σ =   4.90 ns/op)
           5 threads, Tavg =  4,394.18 ns/op (σ =  30.81 ns/op)				   5 threads, Tavg =    684.68 ns/op (σ =   5.07 ns/op)
           6 threads, Tavg =  5,690.40 ns/op (σ =  37.49 ns/op)				   6 threads, Tavg =    770.63 ns/op (σ =   3.59 ns/op)
           7 threads, Tavg =  7,056.70 ns/op (σ =  42.77 ns/op)				   7 threads, Tavg =    867.29 ns/op (σ =   6.03 ns/op)
           8 threads, Tavg =  7,579.83 ns/op (σ =  46.69 ns/op)				   8 threads, Tavg =    981.51 ns/op (σ =   1.72 ns/op)

================
 Proxy_getProxyClass: run duration:  5,000 ms, #of logical CPUS: 8
		#
 Warm up:		# Warm up:
================
          20 threads, Tavg = 61,327.42 ns/op (σ = 773.74 ns/op)				  20 threads, Tavg = 38,434.58 ns/op (σ = 618.25 ns/op)
          20 threads, Tavg = 61,864.68 ns/op (σ = 990.49 ns/op)				  20 threads, Tavg = 38,953.97 ns/op (σ = 296.18 ns/op)
================
 Measure:		# Measure:
================
          20 threads, Tavg = 62,284.52 ns/op (σ = 986.35 ns/op)				  20 threads, Tavg = 38,408.78 ns/op (σ = 363.68 ns/op)
          21 threads, Tavg = 65,814.18 ns/op (σ = 783.89 ns/op)				  21 threads, Tavg = 40,726.45 ns/op (σ = 846.00 ns/op)
          22 threads, Tavg = 68,900.06 ns/op (σ = 1,110.54 ns/op)			  22 threads, Tavg = 42,392.21 ns/op (σ = 677.61 ns/op)
          23 threads, Tavg = 70,054.36 ns/op (σ = 1,416.82 ns/op)			  23 threads, Tavg = 43,906.97 ns/op (σ = 1,045.91 ns/op)
          24 threads, Tavg = 74,568.58 ns/op (σ = 1,215.74 ns/op)		      24 threads, Tavg = 46,018.68 ns/op (σ = 644.07 ns/op)
          25 threads, Tavg = 75,893.96 ns/op (σ = 2,179.06 ns/op)			  25 threads, Tavg = 48,315.78 ns/op (σ = 731.60 ns/op)

================
 Proxy_isProxyClassTrue: run duration:  5,000 ms, #of logical CPUS: 8
		#
 Warm up:		# Warm up:
================
          20 threads, Tavg =  9,063.70 ns/op (σ = 257.19 ns/op)				  20 threads, Tavg =    806.70 ns/op (σ =   6.13 ns/op)
          20 threads, Tavg =  8,823.53 ns/op (σ = 176.60 ns/op)				  20 threads, Tavg =    809.96 ns/op (σ =   4.70 ns/op)
================
 Measure:		# Measure:
================
          20 threads, Tavg =  8,853.09 ns/op (σ = 488.12 ns/op)				  20 threads, Tavg =    803.11 ns/op (σ =   5.93 ns/op)
          21 threads, Tavg =  9,270.52 ns/op (σ = 210.02 ns/op)				  21 threads, Tavg =    844.50 ns/op (σ =   6.95 ns/op)
          22 threads, Tavg =  9,754.47 ns/op (σ = 354.98 ns/op)				  22 threads, Tavg =    884.71 ns/op (σ =   5.25 ns/op)
          23 threads, Tavg = 10,161.86 ns/op (σ = 269.36 ns/op)				  23 threads, Tavg =    925.42 ns/op (σ =   8.00 ns/op)
          24 threads, Tavg = 10,633.98 ns/op (σ = 263.65 ns/op)				  24 threads, Tavg =    975.15 ns/op (σ =   9.68 ns/op)
          25 threads, Tavg = 11,060.34 ns/op (σ = 279.84 ns/op)				  25 threads, Tavg =  1,001.26 ns/op (σ =   6.71 ns/op)

================
 Proxy_isProxyClassFalse: run duration:  5,000 ms, #of logical CPUS: 8
		#
 Warm up:		# Warm up:
================
          20 threads, Tavg =  8,876.60 ns/op (σ = 177.45 ns/op)				  20 threads, Tavg =    482.21 ns/op (σ =   2.34 ns/op)
          20 threads, Tavg =  8,868.78 ns/op (σ = 152.58 ns/op)				  20 threads, Tavg =    481.62 ns/op (σ =   3.73 ns/op)
================
 Measure:		# Measure:
================
          20 threads, Tavg =  8,907.63 ns/op (σ = 532.81 ns/op)				  20 threads, Tavg =    484.16 ns/op (σ =   3.59 ns/op)
          21 threads, Tavg =  9,294.63 ns/op (σ = 192.21 ns/op)				  21 threads, Tavg =    509.54 ns/op (σ =   2.90 ns/op)
          22 threads, Tavg =  9,820.60 ns/op (σ = 308.00 ns/op)				  22 threads, Tavg =    529.97 ns/op (σ =   2.79 ns/op)
          23 threads, Tavg = 10,168.79 ns/op (σ = 282.45 ns/op)				  23 threads, Tavg =    554.43 ns/op (σ =   2.87 ns/op)
          24 threads, Tavg = 10,680.39 ns/op (σ = 273.07 ns/op)				  24 threads, Tavg =    578.46 ns/op (σ =   4.29 ns/op)
          25 threads, Tavg = 11,107.11 ns/op (σ = 276.82 ns/op)				  25 threads, Tavg =    602.47 ns/op (σ =   4.57 ns/op)

================
 Annotation_equals: run duration:  5,000 ms, #of logical CPUS: 8
		#
 Warm up:		# Warm up:
================
          20 threads, Tavg = 18,672.52 ns/op (σ = 216.24 ns/op)				  20 threads, Tavg =  2,461.13 ns/op (σ =  30.87 ns/op)
          20 threads, Tavg = 18,743.04 ns/op (σ = 135.25 ns/op)				  20 threads, Tavg =  2,474.52 ns/op (σ =  29.03 ns/op)
================
 Measure:		# Measure:
================
          20 threads, Tavg = 18,906.74 ns/op (σ = 175.28 ns/op)				  20 threads, Tavg =  2,471.36 ns/op (σ =  31.31 ns/op)
          21 threads, Tavg = 19,964.27 ns/op (σ = 257.05 ns/op)				  21 threads, Tavg =  2,600.50 ns/op (σ =  34.27 ns/op)
          22 threads, Tavg = 20,571.67 ns/op (σ = 264.78 ns/op)				  22 threads, Tavg =  2,724.40 ns/op (σ =  45.29 ns/op)
          23 threads, Tavg = 21,823.36 ns/op (σ = 273.56 ns/op)				  23 threads, Tavg =  2,886.34 ns/op (σ =  88.89 ns/op)
          24 threads, Tavg = 22,852.14 ns/op (σ = 223.03 ns/op)				  24 threads, Tavg =  2,969.93 ns/op (σ =  34.20 ns/op)
          25 threads, Tavg = 23,698.01 ns/op (σ = 312.63 ns/op)				  25 threads, Tavg =  3,095.16 ns/op (σ =  56.64 ns/op)
```



#### 1-40 threads, running the getProxyClass

We see that around 18 threads, that the times just quite a bit; as more load is put on the system.
You can see that as the number of threads grow the contention on the JDK java.lang.reflect.Proxy
is high.  With the patched version as the number of threads grows you can see the how the NON BLOCKING
mechanisms truely assist in performance.  With sometimes a 50% benefit.  The isProxyClass methods are
vastly improved on the orginal


Unpatched j.l.r.Proxy																	Patched j.l.r.Proxy:


```java
================
 Proxy_getProxyClass: run duration:  5,000 ms, #of logical CPUS: 8# Proxy_getProxyClass: run duration:  5,000 ms, #of logical CPUS: 8

 Warm up:# Warm up:
================
           1 threads (928812 ops), Tavg =  5,383.50 ns/op (σ =   0.00 ns/op)           1 threads (830396 ops), Tavg =  6,022.33 ns/op (σ =   0.00 ns/op)
           1 threads (927876 ops), Tavg =  5,388.64 ns/op (σ =   0.00 ns/op)           1 threads (830441 ops), Tavg =  6,022.53 ns/op (σ =   0.00 ns/op)
================
 Measure:# Measure:
================
           1 threads (932179 ops), Tavg =  5,364.25 ns/op (σ =   0.00 ns/op)           1 threads (834141 ops), Tavg =  5,994.76 ns/op (σ =   0.00 ns/op)
           2 threads (1648504 ops), Tavg =  6,066.05 ns/op (σ =   0.63 ns/op)           2 threads (1600323 ops), Tavg =  6,249.88 ns/op (σ =  12.37 ns/op)
           3 threads (2161399 ops), Tavg =  6,940.83 ns/op (σ = 117.10 ns/op)           3 threads (2198414 ops), Tavg =  6,824.34 ns/op (σ =  15.54 ns/op)
           4 threads (2519672 ops), Tavg =  7,938.42 ns/op (σ = 163.34 ns/op)           4 threads (2559386 ops), Tavg =  7,815.65 ns/op (σ =  33.90 ns/op)
           5 threads (2447484 ops), Tavg = 10,215.47 ns/op (σ = 203.94 ns/op)           5 threads (2757558 ops), Tavg =  9,067.65 ns/op (σ =  60.98 ns/op)
           6 threads (1306845 ops), Tavg = 22,957.79 ns/op (σ = 141.36 ns/op)           6 threads (2923841 ops), Tavg = 10,262.35 ns/op (σ =  85.98 ns/op)
           7 threads (1195668 ops), Tavg = 29,272.74 ns/op (σ =  81.00 ns/op)           7 threads (2884863 ops), Tavg = 12,134.44 ns/op (σ = 107.62 ns/op)
           8 threads (1143387 ops), Tavg = 34,990.05 ns/op (σ = 162.04 ns/op)           8 threads (2917990 ops), Tavg = 13,710.49 ns/op (σ =  47.62 ns/op)
           9 threads (1149053 ops), Tavg = 39,169.57 ns/op (σ = 210.69 ns/op)           9 threads (2918041 ops), Tavg = 15,424.74 ns/op (σ = 143.40 ns/op)
          10 threads (1147565 ops), Tavg = 43,578.21 ns/op (σ = 269.21 ns/op)          10 threads (2918002 ops), Tavg = 17,136.86 ns/op (σ = 306.18 ns/op)
          11 threads (1149051 ops), Tavg = 47,874.27 ns/op (σ = 263.05 ns/op)          11 threads (2902755 ops), Tavg = 18,951.06 ns/op (σ = 285.28 ns/op)
          12 threads (1142920 ops), Tavg = 52,504.67 ns/op (σ = 405.44 ns/op)          12 threads (2895425 ops), Tavg = 20,719.07 ns/op (σ = 454.55 ns/op)
          13 threads (1137423 ops), Tavg = 57,148.28 ns/op (σ = 373.94 ns/op)          13 threads (2876067 ops), Tavg = 22,606.06 ns/op (σ = 389.89 ns/op)
          14 threads (1143865 ops), Tavg = 61,206.54 ns/op (σ = 379.43 ns/op)          14 threads (2880530 ops), Tavg = 24,291.31 ns/op (σ = 318.84 ns/op)
          15 threads (1141579 ops), Tavg = 65,698.40 ns/op (σ = 258.78 ns/op)          15 threads (2885079 ops), Tavg = 25,997.70 ns/op (σ = 441.52 ns/op)
          16 threads (1145055 ops), Tavg = 69,895.77 ns/op (σ = 473.89 ns/op)          16 threads (2882856 ops), Tavg = 27,758.64 ns/op (σ = 584.98 ns/op)
          17 threads (1142779 ops), Tavg = 74,380.43 ns/op (σ = 435.29 ns/op)          17 threads (2898058 ops), Tavg = 29,367.04 ns/op (σ = 514.11 ns/op)
          18 threads (1135162 ops), Tavg = 79,295.86 ns/op (σ = 473.70 ns/op)          18 threads (2877105 ops), Tavg = 31,303.53 ns/op (σ = 676.53 ns/op)
          19 threads (1134896 ops), Tavg = 83,723.65 ns/op (σ = 456.59 ns/op)          19 threads (2882766 ops), Tavg = 32,966.66 ns/op (σ = 1,204.27 ns/op)
          20 threads (1135556 ops), Tavg = 88,078.31 ns/op (σ = 786.68 ns/op)          20 threads (2880366 ops), Tavg = 34,750.20 ns/op (σ = 1,002.44 ns/op)
          21 threads (1131559 ops), Tavg = 92,801.88 ns/op (σ = 550.98 ns/op)          21 threads (2880059 ops), Tavg = 36,456.80 ns/op (σ = 957.68 ns/op)
          22 threads (1127247 ops), Tavg = 97,583.46 ns/op (σ = 817.98 ns/op)          22 threads (2886311 ops), Tavg = 38,149.09 ns/op (σ = 1,482.38 ns/op)
          23 threads (1127149 ops), Tavg = 102,045.78 ns/op (σ = 895.21 ns/op)          23 threads (2886876 ops), Tavg = 39,836.85 ns/op (σ = 1,681.14 ns/op)
          24 threads (1121490 ops), Tavg = 107,007.55 ns/op (σ = 642.68 ns/op)          24 threads (2875551 ops), Tavg = 41,582.89 ns/op (σ = 1,404.16 ns/op)
          25 threads (1127990 ops), Tavg = 110,823.77 ns/op (σ = 884.78 ns/op)          25 threads (2865402 ops), Tavg = 43,586.20 ns/op (σ = 1,486.28 ns/op)
          26 threads (1128499 ops), Tavg = 115,217.89 ns/op (σ = 711.99 ns/op)          26 threads (2871939 ops), Tavg = 45,245.72 ns/op (σ = 1,693.46 ns/op)
          27 threads (1121394 ops), Tavg = 120,401.80 ns/op (σ = 944.38 ns/op)          27 threads (2873698 ops), Tavg = 46,919.30 ns/op (σ = 2,240.05 ns/op)
          28 threads (1115892 ops), Tavg = 125,469.95 ns/op (σ = 1,000.48 ns/op)          28 threads (2870809 ops), Tavg = 48,761.83 ns/op (σ = 2,094.05 ns/op)
          29 threads (1115565 ops), Tavg = 130,001.77 ns/op (σ = 1,226.17 ns/op)          29 threads (2869103 ops), Tavg = 50,600.17 ns/op (σ = 2,883.79 ns/op)
          30 threads (1112908 ops), Tavg = 134,801.29 ns/op (σ = 1,388.39 ns/op)          30 threads (2902183 ops), Tavg = 52,655.38 ns/op (σ = 3,221.90 ns/op)
          31 threads (1119333 ops), Tavg = 138,498.38 ns/op (σ = 1,221.30 ns/op)          31 threads (2854490 ops), Tavg = 54,220.11 ns/op (σ = 2,152.95 ns/op)
          32 threads (1114786 ops), Tavg = 143,550.72 ns/op (σ = 1,289.04 ns/op)          32 threads (2850142 ops), Tavg = 56,102.53 ns/op (σ = 2,698.40 ns/op)
          33 threads (1121503 ops), Tavg = 147,148.98 ns/op (σ = 1,112.64 ns/op)          33 threads (2859882 ops), Tavg = 57,741.56 ns/op (σ = 3,018.69 ns/op)
          34 threads (1111081 ops), Tavg = 153,032.39 ns/op (σ = 1,248.04 ns/op)          34 threads (2848173 ops), Tavg = 59,657.79 ns/op (σ = 2,741.49 ns/op)
          35 threads (1107354 ops), Tavg = 158,063.23 ns/op (σ = 1,649.16 ns/op)          35 threads (2813222 ops), Tavg = 62,334.87 ns/op (σ = 3,536.37 ns/op)
          36 threads (1112111 ops), Tavg = 161,881.87 ns/op (σ = 1,856.75 ns/op)          36 threads (2830007 ops), Tavg = 63,588.24 ns/op (σ = 3,913.32 ns/op)
          37 threads (1114256 ops), Tavg = 166,057.42 ns/op (σ = 1,415.15 ns/op)          37 threads (2844863 ops), Tavg = 65,124.33 ns/op (σ = 3,660.36 ns/op)
          38 threads (1104628 ops), Tavg = 172,001.33 ns/op (σ = 1,495.96 ns/op)          38 threads (2839929 ops), Tavg = 67,023.47 ns/op (σ = 4,453.24 ns/op)
          39 threads (1108323 ops), Tavg = 175,972.45 ns/op (σ = 1,968.20 ns/op)          39 threads (2836518 ops), Tavg = 68,968.79 ns/op (σ = 4,847.94 ns/op)
          40 threads (1112080 ops), Tavg = 179,875.71 ns/op (σ = 1,757.78 ns/op)          40 threads (2831035 ops), Tavg = 70,610.07 ns/op (σ = 3,848.45 ns/op)
```


## Usage

```
-Xbootclasspath/p:<absolute-path-to>/concurrent-reflect-proxy-1.1.jar
```

There's several wait strategies for when there is a busy loop, i.e. the following bit.  The choice of busy spin strategy
is really really dependent on you hardware.  The default is "SleepingWaitStrategy":

```java
       do {
            Object value = cache.get(key);
            if (value instanceof Reference) {
                proxyClass = (Class<?>) ((Reference) value).get();
            }

            if (proxyClass != null) {
                // proxy class already generated: return it
                return proxyClass;
            } else if (value == pendingGenerationMarker) {
                // proxy class being generated: wait for it
                // spin and check if created
                spinningWaitStrategy.dowait(cache,key,pendingGenerationMarker);
                continue;
            } else {
                    /*
                     * No proxy class for this list of interfaces has been
                     * generated or is being generated, so we will go and
                     * generate it now.  Mark it as pending generation.
                     */
                value = cache.putIfAbsent(key, pendingGenerationMarker);
                if(value==null) {
                    break;
                }
            }
        } while (true);
```

The wait strategy can be changed via the system property: -DPROXY.WAIT.STRATEGY=SleepingWaitStrategy
Which takes one of the values:    SleepingWaitStrategy, OneNanoSleepWaitStrategy, BusySpinWaitStrategy, YieldingWaitStrategy:

```
-Xbootclasspath/p:<absolute-path-to>/concurrent-reflect-proxy-1.1.jar -DPROXY.WAIT.STRATEGY=BusySpinWaitStrategy
```


### Not a solution to Spring performance in 3.0.x or 3.1.x

Above we mentioned that the Spring framework is hindered by the performance due to it's heavy use of Proxies and
annotations:

This gist shows the affects of java.lang.reflect.Proxy, and the implications it has on the Spring
 framework.

- https://gist.github.com/twicksell/1894670


Whilst this class would assist, it would not complete solve the blocking issues.  If you were to take the gist above
and run the test with the bootclasspath switch enabled.  The BLOCKED on the synchronize sections of the Proxy class would
disappear.  However, you'd still see the same type of performance issue (execution time of 18529ms, rather than ~2000ms).
The reason for this is that you hit the following bottleneck, and further synchronization issue:


```java
"pool-2-thread-21" prio=5 tid=7fd609a6b800 nid=0x117624000 waiting for monitor entry [117623000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at sun.reflect.annotation.AnnotationType.getInstance(AnnotationType.java:63)
	- waiting to lock <7fb349c48> (a java.lang.Class for sun.reflect.annotation.AnnotationType)
	at sun.reflect.annotation.AnnotationParser.parseAnnotation(AnnotationParser.java:202)
	at sun.reflect.annotation.AnnotationParser.parseAnnotations2(AnnotationParser.java:69)
	at sun.reflect.annotation.AnnotationParser.parseAnnotations(AnnotationParser.java:52)
	at java.lang.reflect.Field.declaredAnnotations(Field.java:1014)
	- locked <7f5c94700> (a java.lang.reflect.Field)
	at java.lang.reflect.Field.getDeclaredAnnotations(Field.java:1007)
	at java.lang.reflect.AccessibleObject.getAnnotations(AccessibleObject.java:175)
	at org.springframework.core.convert.Property.resolveAnnotations(Property.java:189)
	at org.springframework.core.convert.Property.<init>(Property.java:64)
	at ThreadedPropertyPerformanceTest$TestThread.run(ThreadedPropertyPerformanceTest.java:93)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:441)
	at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
	at java.util.concurrent.FutureTask.run(FutureTask.java:138)
	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
	at java.lang.Thread.run(Thread.java:680)
```

This is an instance of the following, which should be addressed in java 8.

http://mail.openjdk.java.net/pipermail/core-libs-dev/2012-November/012171.html
http://bugs.sun.com/view_bug.do?bug_id=7122142
