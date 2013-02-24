

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Proxy;


/**
 */
@ProxyBenchmarkTest.Ann0()
@ProxyBenchmarkTest.Ann1()
@ProxyBenchmarkTest.Ann2()
@ProxyBenchmarkTest.Ann3()
@ProxyBenchmarkTest.Ann4()
@ProxyBenchmarkTest.Ann5()
@ProxyBenchmarkTest.Ann6()
@ProxyBenchmarkTest.Ann7()
public class ProxyBenchmarkTest extends TestRunner {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann0 {
        String value() default "0";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann1 {
        String value() default "1";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann2 {
        String value() default "2";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann3 {
        String value() default "3";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann4 {
        String value() default "4";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann5 {
        String value() default "5";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann6 {
        String value() default "6";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ann7 {
        String value() default "7";
    }



    public static class Proxy_getProxyClass extends TestRunner.Test {
        private static final ClassLoader cl = Ann0.class.getClassLoader();
        @Override
        protected void doOp() {
            consume(Proxy.getProxyClass(cl, Ann0.class));
            consume(Proxy.getProxyClass(cl, Ann1.class));
            consume(Proxy.getProxyClass(cl, Ann2.class));
            consume(Proxy.getProxyClass(cl, Ann3.class));
            consume(Proxy.getProxyClass(cl, Ann4.class));
            consume(Proxy.getProxyClass(cl, Ann5.class));
            consume(Proxy.getProxyClass(cl, Ann6.class));
            consume(Proxy.getProxyClass(cl, Ann7.class));
        }

        @Override
        protected void checkConsumeCounts(long ops, long defaultValuesConsumed, long nonDefaultValuesConsumed) {
            if (!(defaultValuesConsumed == 0 && nonDefaultValuesConsumed == ops * 8L))
                throw new AssertionError();
        }
    }

    public static class Proxy_isProxyClassTrue extends TestRunner.Test {

        static final Annotation[] anns = ProxyBenchmarkTest.class.getDeclaredAnnotations();
        static final Annotation ann0 = anns[0];
        static final Annotation ann1 = anns[1];
        static final Annotation ann2 = anns[2];
        static final Annotation ann3 = anns[3];
        static final Annotation ann4 = anns[4];
        static final Annotation ann5 = anns[5];
        static final Annotation ann6 = anns[6];
        static final Annotation ann7 = anns[7];

        @Override
        protected void doOp() {
            consume(Proxy.isProxyClass(ann0.getClass()));
            consume(Proxy.isProxyClass(ann1.getClass()));
            consume(Proxy.isProxyClass(ann2.getClass()));
            consume(Proxy.isProxyClass(ann3.getClass()));
            consume(Proxy.isProxyClass(ann4.getClass()));
            consume(Proxy.isProxyClass(ann5.getClass()));
            consume(Proxy.isProxyClass(ann6.getClass()));
            consume(Proxy.isProxyClass(ann7.getClass()));
        }

        @Override
        protected void checkConsumeCounts(long ops, long defaultValuesConsumed, long nonDefaultValuesConsumed) {
            if (!(defaultValuesConsumed == 0 && nonDefaultValuesConsumed == ops * 8L))
                throw new AssertionError();
        }
    }

    public static class Proxy_isProxyClassFalse extends TestRunner.Test {
        @Override
        protected void doOp() {
            consume(Proxy.isProxyClass(Ann0.class));
            consume(Proxy.isProxyClass(Ann1.class));
            consume(Proxy.isProxyClass(Ann2.class));
            consume(Proxy.isProxyClass(Ann3.class));
            consume(Proxy.isProxyClass(Ann4.class));
            consume(Proxy.isProxyClass(Ann5.class));
            consume(Proxy.isProxyClass(Ann6.class));
            consume(Proxy.isProxyClass(Ann7.class));
        }

        @Override
        protected void checkConsumeCounts(long ops, long defaultValuesConsumed, long nonDefaultValuesConsumed) {
            if (!(defaultValuesConsumed == ops * 8L && nonDefaultValuesConsumed == 0L))
                throw new AssertionError();
        }
    }


    public static class Annotation_equals extends TestRunner.Test {
        static final Annotation[] anns = ProxyBenchmarkTest.class.getDeclaredAnnotations();
        static final Annotation ann0 = anns[0];
        static final Annotation ann1 = anns[1];
        static final Annotation ann2 = anns[2];
        static final Annotation ann3 = anns[3];
        static final Annotation ann4 = anns[4];
        static final Annotation ann5 = anns[5];
        static final Annotation ann6 = anns[6];
        static final Annotation ann7 = anns[7];
        @Override
        protected void doOp() {
            consume(ann0.equals(ann0));
            consume(ann1.equals(ann1));
            consume(ann2.equals(ann3));
            consume(ann3.equals(ann2));
            consume(ann4.equals(ann4));
            consume(ann5.equals(ann5));
            consume(ann6.equals(ann7));
            consume(ann7.equals(ann6));
        }

        @Override
        protected void checkConsumeCounts(long ops, long defaultValuesConsumed, long nonDefaultValuesConsumed) {
            if (!(defaultValuesConsumed == ops * 4L && nonDefaultValuesConsumed == ops * 4L))
                throw new AssertionError();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int maxThreads = Math.max(4, Runtime.getRuntime().availableProcessors());
        doTest(Proxy_getProxyClass.class, 5000L, 1, maxThreads, 1);
        doTest(Proxy_isProxyClassTrue.class, 5000L, 1, maxThreads, 1);
        doTest(Proxy_isProxyClassFalse.class, 5000L, 1, maxThreads, 1);
        doTest(Annotation_equals.class, 5000L, 1, maxThreads, 1);

        maxThreads = 40;
        doTest(Proxy_getProxyClass.class, 5000L, 1, maxThreads, 1);

    }
}
