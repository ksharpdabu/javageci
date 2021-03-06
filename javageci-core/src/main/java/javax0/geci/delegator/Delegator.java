package javax0.geci.delegator;

import javax0.geci.api.Source;
import javax0.geci.tools.AbstractFilteredFieldsGenerator;
import javax0.geci.tools.CompoundParams;
import javax0.geci.tools.GeciReflectionTools;
import javax0.geci.tools.MethodTool;
import javax0.geci.tools.reflection.Selector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Delegator extends AbstractFilteredFieldsGenerator {

    private final Class<? extends Annotation> generatedAnnotation;

    public Delegator() {
        generatedAnnotation = javax0.geci.annotations.Generated.class;
    }

    public Delegator(Class<? extends Annotation> generatedAnnotation) {
        this.generatedAnnotation = generatedAnnotation;
    }

    @Override
    public String mnemonic() {
        return "delegator";
    }

    public void process(Source source, Class<?> klass, CompoundParams params, Field field) throws Exception {
        final var name = field.getName();
        final var methodFilter = params.get("methods", "public & !static");
        final var delClass = field.getType();
        final List<Method> methods = Arrays.stream(GeciReflectionTools.getDeclaredMethodsSorted(delClass))
                .filter(Selector.compile(methodFilter)::match)
                .collect(Collectors.toList());
        try (final var segment = source.safeOpen(params.id())) {
            for (final var method : methods) {
                if (!manuallyCoded(klass, method)) {
                    segment.write("@" + generatedAnnotation.getCanonicalName() + "(\"" + mnemonic() + "\")");
                    segment.write_r(MethodTool.with(method).signature() + " {");
                    if ("void".equals(method.getReturnType().getName())) {
                        segment.write(name + "." + MethodTool.with(method).call() + ";");
                    } else {
                        segment.write("return " + name + "." + MethodTool.with(method).call() + ";");
                    }
                    segment.write_l("}");
                    segment.newline();
                }
            }
        }
    }

    private boolean manuallyCoded(Class<?> klass, Method method) {
        try {
            var localMethod = klass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return localMethod.getDeclaredAnnotation(generatedAnnotation) == null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
