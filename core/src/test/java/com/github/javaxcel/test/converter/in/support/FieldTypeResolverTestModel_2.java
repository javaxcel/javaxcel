package com.github.javaxcel.test.converter.in.support;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "rawtypes"})
public class FieldTypeResolverTestModel_2<
        A,
        B extends BigInteger,
        C extends Queue<String>,
        D extends Collection<B>,
        E extends Iterable<? super UUID>,
        F extends Iterable<? extends Locale[]>> {

    private Integer integer;
    private A object;
    private B bigInteger;
    private FieldTypeResolverTestModel_2<String, B, C, List<B>, Set<UUID>, F> self;

    private String[] array_string;
    private B[][] array_array_bigInteger;
    private C[] array_queue_string;
    private Iterable<? extends F[]>[] array_iterable_array_iterable_array_locale;

    private Iterable iterable;
    private Iterable<Long> iterable_long;
    private Deque<?> deque_object;
    private List<A> list_object;
    private Set<B> set_bigInteger;
    private D collection_bigInteger;
    private Iterable<E> iterable_iterable_uuid;

}
