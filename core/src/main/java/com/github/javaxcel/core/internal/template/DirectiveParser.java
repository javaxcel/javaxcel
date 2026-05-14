/*
 * Copyright 2026 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.core.internal.template;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.util.CellAddress;
import org.jspecify.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;

/**
 * Parses {@code jxc:} directives from cell comment text into {@link DirectiveSpec}.
 *
 * <p>The parser is lenient about whitespace and case for the directive prefix
 * but strict on grammar — malformed directives raise
 * {@link IllegalArgumentException} so the user sees an actionable error
 * instead of a silently ignored cell comment.
 *
 * <p>Grammar:
 * <pre>{@code
 * DIRECTIVE   := "jxc:" WS COMMAND
 * COMMAND     := "each" WS COLLECTION_EXPR WS "as" WS VAR_NAME ("," WS "until:" WS CELL_REF)?
 *              | "if"   WS BOOLEAN_EXPR
 * }</pre>
 *
 * @since 0.x
 */
public final class DirectiveParser {

    private static final String PREFIX = "jxc:";

    /** Splits "&lt;collection&gt; as &lt;var&gt; [, until: &lt;ref&gt;]" preserving whitespace handling. */
    private static final Pattern EACH_PATTERN = Pattern.compile(
            "^(?<collection>.+?)\\s+as\\s+(?<var>[A-Za-z_$][A-Za-z0-9_$]*)" +
                    "(?:\\s*,\\s*until\\s*:\\s*(?<until>[A-Za-z]+\\d+))?\\s*$");

    private DirectiveParser() {
    }

    /**
     * Parses the given cell-comment text. Returns {@link Optional#empty()} when
     * the text is null, blank, or does not start with the {@code jxc:} prefix.
     *
     * @throws IllegalArgumentException when the directive prefix is present but
     *                                  the body cannot be parsed.
     */
    public static Optional<DirectiveSpec> parse(@Nullable String text) {
        if (text == null) {
            return Optional.empty();
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty() || !trimmed.toLowerCase().startsWith(PREFIX)) {
            return Optional.empty();
        }

        String body = trimmed.substring(PREFIX.length()).trim();
        Asserts.that(body)
                .describedAs("Directive body is empty: {0}", text)
                .isNotEmpty();

        if (matchesCommand(body, "each")) {
            return Optional.of(parseEach(body.substring("each".length()).trim(), text));
        }
        if (matchesCommand(body, "if")) {
            return Optional.of(parseIf(body.substring("if".length()).trim(), text));
        }
        throw new IllegalArgumentException("Unknown jxc directive command in: " + text);
    }

    private static boolean matchesCommand(String body, String command) {
        if (body.length() <= command.length()) {
            return false;
        }
        if (!body.regionMatches(true, 0, command, 0, command.length())) {
            return false;
        }
        char next = body.charAt(command.length());
        return Character.isWhitespace(next);
    }

    private static DirectiveSpec.Each parseEach(String body, String original) {
        Asserts.that(body)
                .describedAs("Empty body for jxc:each in: {0}", original)
                .isNotEmpty();

        Matcher matcher = EACH_PATTERN.matcher(body);
        Asserts.that(matcher.matches())
                .describedAs(
                        "Invalid jxc:each syntax: \"{0}\" — expected \"each <collection> as <var> [, until: <cell>]\"",
                        original)
                .isTrue();

        String collectionExpr = matcher.group("collection").trim();
        String varName = matcher.group("var");
        String untilGroup = matcher.group("until");

        @Nullable CellAddress untilAddress = untilGroup == null ? null : new CellAddress(untilGroup.trim());
        return new DirectiveSpec.Each(collectionExpr, varName, untilAddress);
    }

    private static DirectiveSpec.If parseIf(String body, String original) {
        Asserts.that(body)
                .describedAs("Empty body for jxc:if in: {0}", original)
                .isNotEmpty();
        return new DirectiveSpec.If(body);
    }

}
