package javax0.geci.fluent;

import javax0.geci.api.GeciException;
import javax0.geci.api.Source;
import javax0.geci.fluent.internal.ClassBuilder;
import javax0.geci.fluent.internal.FluentBuilderImpl;
import javax0.geci.fluent.internal.StructureOptimizer;
import javax0.geci.fluent.tree.Node;
import javax0.geci.fluent.tree.Tree;
import javax0.geci.log.Logger;
import javax0.geci.log.LoggerFactory;
import javax0.geci.tools.AbstractGenerator;
import javax0.geci.tools.CompoundParams;
import javax0.geci.tools.Tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Fluent extends AbstractGenerator {
    private static final Logger LOG = LoggerFactory.getLogger();

    @Override
    public void process(Source source, Class<?> klass, CompoundParams global) throws Exception {
        var definingMethod = getDefiningMethod(global.get("definedBy"), klass);
        try {
            var builder = (FluentBuilderImpl) definingMethod.invoke(null);

            LOG.info("Node structure before optimization.");
            LOG.info("" + new Tree(Node.ONCE, builder.getNodes()));
            new StructureOptimizer(builder).optimize();
            LOG.info("Node structure after optimization.");
            LOG.info("" + new Tree(Node.ONCE, builder.getNodes()));
            var generatedCode = new ClassBuilder(builder).build();
            try (var segment = source.open(global.get("id"))) {
                segment.write(generatedCode);
            }
        } catch (InvocationTargetException ite) {
            throw (Exception) ite.getCause();
        }
    }

    private Method getDefiningMethod(String s, Class<?> forClass) {
        var sepPos = s.indexOf("::");
        if (sepPos == -1) {
            throw new GeciException("Fluent structure definedBy has to have 'className::methodName' format for class '" + forClass + "'");
        }
        var className = s.substring(0, sepPos);
        var methodName = s.substring(sepPos + 2);
        final Class<?> klass;
        try {
            klass = Tools.classForName(className);
        } catch (ClassNotFoundException e) {
            throw new GeciException("definedBy class '" + className + "' can not be found");
        }
        final Method method;
        try {
            method = klass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new GeciException("definedBy method '" + methodName +
                    "' can not be found in the class '" + className + "'");
        }
        if ((method.getModifiers() & Modifier.STATIC) == 0) {
            throw new GeciException("definedBy method '" + methodName +
                    "' from the class '" + className + "' should be static");
        }
        if (!FluentBuilder.class.isAssignableFrom(method.getReturnType())) {
            throw new GeciException("definedBy method '" + methodName +
                    "' from the class '" + className + "' should return type " +
                    FluentBuilderImpl.class.getName());
        }
        return method;
    }

    @Override
    public String mnemonic() {
        return "fluent";
    }
}
