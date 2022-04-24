package ru.ckateptb.commons.ioc.guava.base;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class Joiner {
    private final String separator;

    public static Joiner on(String separator) {
        return new Joiner(separator);
    }

    private Joiner(String separator) {
        this.separator = (String)Preconditions.checkNotNull(separator);
    }

    public <A extends Appendable> A appendTo(A appendable, Iterator<? extends Object> parts) throws IOException {
        Preconditions.checkNotNull(appendable);
        if (parts.hasNext()) {
            appendable.append(this.toString(parts.next()));

            while(parts.hasNext()) {
                appendable.append(this.separator);
                appendable.append(this.toString(parts.next()));
            }
        }

        return appendable;
    }

    public final StringBuilder appendTo(StringBuilder builder, Iterable<? extends Object> parts) {
        return this.appendTo(builder, parts.iterator());
    }

    public final StringBuilder appendTo(StringBuilder builder, Iterator<? extends Object> parts) {
        try {
            this.appendTo((Appendable)builder, (Iterator)parts);
            return builder;
        } catch (IOException var4) {
            throw new AssertionError(var4);
        }
    }

    CharSequence toString(Object part) {
        Objects.requireNonNull(part);
        return (CharSequence)(part instanceof CharSequence ? (CharSequence)part : part.toString());
    }
}