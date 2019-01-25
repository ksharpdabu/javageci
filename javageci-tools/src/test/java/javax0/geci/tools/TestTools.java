package javax0.geci.tools;

import javax0.geci.api.Source;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestTools {
    @javax0.geci.annotations.Geci("aaa a='b' b='c' c='d' a$='dollared' b3='bthree' _='-'")
    @javax0.geci.annotations.Geci("xxx x='x' y='y' z='z'")
    private static Object something;
    private HashMap<Map<String, Integer>, Object> b;

    @Test
    void testParameterParser() {
        var map = Tools.getParameters("a='b' b='c' c='d'");
        assertEquals(map.size(), 3);
        assertEquals(map.get("a"), "b");
        assertEquals(map.get("b"), "c");
        assertEquals(map.get("c"), "d");
    }

    @Test
    void testParameterFetcher() throws NoSuchFieldException {
        Field f = this.getClass().getDeclaredField("something");
        var map = Tools.getParameters(f, "aaa");
        assertNotNull(map);
        assertEquals(map.get("a"), "b");
        assertEquals(map.get("b"), "c");
        assertEquals(map.get("c"), "d");
        assertEquals(map.get("a$"), "dollared");
        assertEquals(map.get("b3"), "bthree");
        assertEquals(map.get("_"), "-");

    }

    @Test
    void testTypeGetting() throws NoSuchMethodException, NoSuchFieldException {
        assertEquals(
                "void",
                Tools.typeAsString(this.getClass().getDeclaredMethod("testTypeGetting")));
        assertEquals(
                "java.util.HashMap<java.util.Map<String,Integer>,Object>",
                Tools.typeAsString(this.getClass().getDeclaredField("b")));
    }

    @Test
    void normalizesGenericNames() {
        Assertions.assertAll(
                () -> assertEquals("String", Tools.normalizeTypeName("java.lang.String")),
                () -> assertEquals("java.util.Map", Tools.normalizeTypeName("java.util.Map")),
                () -> assertEquals("java.util.Map<Integer,String>",
                        Tools.normalizeTypeName("java.util.Map<java.lang.Integer,java.lang.String>")),
                () -> assertEquals("java.util.Map<java.util.Set<Integer>,String>",
                        Tools.normalizeTypeName("java.util.Map<java.util.Set<java.lang.Integer>,java.lang.String>")),
                () -> assertEquals("java.util.Map<java.util.Set<Integer>,String>",
                        Tools.normalizeTypeName("java.util.Map<java.util.Set< java.lang.Integer>,java.lang.String>")),
                () -> assertEquals("java.util.Map<java.util.Set<com.java.lang.Integer>,String>",
                        Tools.normalizeTypeName("java.util.Map<java.util.Set< com. java.lang.Integer>,java.lang.String>")),
                () -> assertEquals("java.util.Map<java.util.Set<? extends com.java.lang.Integer>,String>",
                        Tools.normalizeTypeName("java.util.Map<java.util.Set<? extends    com. java.lang.Integer> , java.lang.String>"))
        );
    }

    private static java.util.Map.Entry<String, Integer>[] m1() {
        return null;
    }

    private static java.util.Map.Entry<? extends String, ? super Integer>[] m2() {
        return null;
    }

    @Test
    void normalizeType() {
        Assertions.assertAll(
                () -> assertEquals("java.util.Set<java.util.Map.Entry<K,V>>", Tools.getGenericTypeName(Map.class.getDeclaredMethod("entrySet").getGenericReturnType())),
                () -> assertEquals("java.util.Map.Entry", Tools.getGenericTypeName(java.util.Map.Entry.class)),
                () -> assertEquals("java.util.Map.Entry<String,Integer>[]", Tools.getGenericTypeName(this.getClass().getDeclaredMethod("m1").getGenericReturnType())),
                () -> assertEquals("java.util.Map.Entry<? extends String,? super Integer>[]", Tools.getGenericTypeName(this.getClass().getDeclaredMethod("m2").getGenericReturnType()))
        );

    }

    @Test
    void getParametersFromSource() {
        Source testSource = new AbstractTestSource() {
            @Override
            public List<String> getLines() {
                return List.of("    // @Geci(\"aaa a='b' b='c' c='d' a$='dollared' b3='bthree' _='-'\")",
                        "    // @Geci(\"xxx x='x' y='y' z='z'\")",
                        "    private static Object something;",
                        "    private HashMap<Map<String, Integer>, Object> b;");
            }
        };

        var map = Tools.getParameters(testSource, "aaa", "//", null, Pattern.compile(".*something;.*"));
        assertNotNull(map);
        assertEquals(map.get("a"), "b");
        assertEquals(map.get("b"), "c");
        assertEquals(map.get("c"), "d");
        assertEquals(map.get("a$"), "dollared");
        assertEquals(map.get("b3"), "bthree");
        assertEquals(map.get("_"), "-");

    }


    @Test
    @DisplayName("Get the gecis from the standard annotations")
    @javax0.geci.annotations.Geci("barbarumba k1='v1' k2='v2'")
    void getGecisFromAnnotation() throws NoSuchMethodException {
        final var gecis = Tools.getGecis(this.getClass().getDeclaredMethod("getGecisFromAnnotation"));
        Assertions.assertEquals(1, gecis.length);
        Assertions.assertEquals("barbarumba k1='v1' k2='v2'", gecis[0]);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Geci {
        String value();

        String k1() default "";
    }

    @Test
    @DisplayName("Get the gecis from own annotations")
    @TestTools.Geci("barbarumba k1='v1' k2='v2'")
    void getGecisFromOwnAnnotation() throws NoSuchMethodException {
        final var gecis = Tools.getGecis(this.getClass().getDeclaredMethod("getGecisFromOwnAnnotation"));
        Assertions.assertEquals(1, gecis.length);
        Assertions.assertEquals("barbarumba k1='v1' k2='v2'", gecis[0]);
    }

    @Test
    @DisplayName("Get the gecis from own annotations with annotation parameter")
    @TestTools.Geci(value = "barbarumba k2='v2'", k1 = "v1")
    void getGecisFromOwnAnnotationParams() throws NoSuchMethodException {
        final var gecis = Tools.getGecis(this.getClass().getDeclaredMethod("getGecisFromOwnAnnotationParams"));
        Assertions.assertEquals(1, gecis.length);
        Assertions.assertEquals("barbarumba k2='v2' k1='v1'", gecis[0]);
    }
}
