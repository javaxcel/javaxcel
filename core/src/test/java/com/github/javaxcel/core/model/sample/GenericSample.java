package com.github.javaxcel.core.model.sample;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Deque;

@ToString
@EqualsAndHashCode
public class GenericSample<
        ID extends Long,
        TITLE extends String,
        NUMBER extends BigDecimal,
        SUBTITLES extends Deque<TITLE>,
        ARRAY extends Iterable<? extends ID[]>> {

    private ID id;

    private TITLE title;

    private NUMBER number;

    private TITLE[] titleArray;

    private SUBTITLES subtitles;

    private SUBTITLES[][] subtitles2DArray;

    private Iterable<ARRAY> arrays;

}
