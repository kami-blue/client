package team.stiff.pomelo.impl.annotated.handler.scan;

import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class AnnotatedListenerPredicate implements Predicate<Method> {

    @Override
    public boolean test(final Method method) {
        return method.isAnnotationPresent(Listener.class) &&
                method.getParameterCount() == 1;
    }
}
