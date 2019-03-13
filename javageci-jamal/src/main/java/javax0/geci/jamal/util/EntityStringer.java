package javax0.geci.jamal.util;

import javax0.geci.tools.Tools;
import javax0.jamal.api.BadSyntax;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EntityStringer {

    /**
     * Creates a proprietary string representation of a method. This representation is passed back as a string by
     * macros that search methods. Later this string can be passed to other macros that work with methods.
     * <p>
     * The representation is
     * <pre>
     *     class canonical name|method name| arg1 type | arg2 type | ... | argN type
     * </pre>
     * <p>
     * The representation was created so that this is easy to parse (just split along the | characters), does not
     * contain comma, so it can be used as a member of comma separated list in a "for" macro and can also be readable
     * and last but not least have all information needed to look up the method via reflection.
     *
     * @param method to convert to a fingerprint
     * @return
     */
    public static String method2Fingerprint(Method method) {
        final var className = method.getDeclaringClass().getCanonicalName();
        final var argList = Arrays.stream(method.getGenericParameterTypes())
                .map(Type::getTypeName)
                .collect(Collectors.joining("|"));
        return className + "|" + method.getName() + (argList.length() > 0 ? "|" + argList:"");
    }

    private static Class<?> forName(String klassName){
        try {
            return Tools.classForName(klassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Method fingerprint2Method(String fingerprint) throws BadSyntax {
        final var parts = fingerprint.split("\\|", -1);
        if (parts.length < 2) {
            throw new BadSyntax("Method fingerprint '" + fingerprint +
                    "' is badly formatted, does not have 'class|name' parts.");
        }
        try {
            final var klass = Tools.classForName(parts[0]);
            final var name = parts[1];
            if (parts.length > 2) {
                final var argClasses = Arrays.stream(parts).skip(2).map(EntityStringer::forName).toArray( Class[]::new);
                return Tools.getMethod(klass,name,argClasses);
            } else {
                return Tools.getMethod(klass,name);
            }
        } catch (ClassNotFoundException e) {
            throw new BadSyntax("Class in fingerprint '" + fingerprint + "' cannot be found.", e);
        } catch (NoSuchMethodException e) {
            throw new BadSyntax("Method in fingerprint '" + fingerprint + "' cannot be found.", e);
        }
    }
}
