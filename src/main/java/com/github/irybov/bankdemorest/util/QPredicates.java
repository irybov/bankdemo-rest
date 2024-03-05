package com.github.irybov.bankdemorest.util;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QPredicates {

    private List<Predicate> predicates = new ArrayList<>();

    public <T> QPredicates add(T object, Function<T, Predicate> function) {
        if (object != null) {
            predicates.add(function.apply(object));
        }
        return this;
    }
    
    public <T, U> QPredicates add(T first, U second, BiFunction<T, U, Predicate> function) {
        if (first != null && second != null ) {
            predicates.add(function.apply(first, second));
        }
        return this;
    }

    public Predicate buildAnd() {
        return ExpressionUtils.allOf(predicates);
    }

    public Predicate buildOr() {
        return ExpressionUtils.anyOf(predicates);
    }

    public static QPredicates builder() {
        return new QPredicates();
    }

}
