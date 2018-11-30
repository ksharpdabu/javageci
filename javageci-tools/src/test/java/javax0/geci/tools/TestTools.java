package javax0.geci.tools;

import javax0.geci.annotations.Geci;
import javax0.geci.api.Source;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestTools {
    @Geci("aaa a='b' b='c' c='d' a$='dollared' b3='bthree' _='-'")
    @Geci("xxx x='x' y='y' z='z'")
    private static Object something;
    private HashMap<Map<String, Integer>, Object> b;

    @Test
    public void testParameterParser() {
        var map = Tools.getParameters("a='b' b='c' c='d'");
        assertEquals(map.size(), 3);
        assertEquals(map.get("a"), "b");
        assertEquals(map.get("b"), "c");
        assertEquals(map.get("c"), "d");
    }

    @Test
    public void testParameterFetcher() throws NoSuchFieldException {
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
    public void testTypeGetting() throws NoSuchMethodException, NoSuchFieldException {
        assertEquals(
                "void",
                Tools.typeAsString(this.getClass().getDeclaredMethod("testTypeGetting")));
        assertEquals(
                "java.util.HashMap<java.util.Map<String,Integer>,Object>",
                Tools.typeAsString(this.getClass().getDeclaredField("b")));
    }

    @Test
    public void normalizesGenericNames() {
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
    public void normalizeType() {
        Assertions.assertAll(
                () -> assertEquals("java.util.Set<java.util.Map.Entry<K,V>>", Tools.getGenericTypeName(Map.class.getDeclaredMethod("entrySet").getGenericReturnType())),
                () -> assertEquals("java.util.Map.Entry", Tools.getGenericTypeName(java.util.Map.Entry.class)),
                () -> assertEquals("java.util.Map.Entry<String,Integer>[]", Tools.getGenericTypeName(this.getClass().getDeclaredMethod("m1").getGenericReturnType())),
                () -> assertEquals("java.util.Map.Entry<? extends String,? super Integer>[]", Tools.getGenericTypeName(this.getClass().getDeclaredMethod("m2").getGenericReturnType()))
        );

    }

    @Test
    public void getParametersFromSource(){
        Source testSource = new AbstractTestSource() {
            @Override
            public List<String> getLines() {
                return List.of("    // @Geci(\"aaa a='b' b='c' c='d' a$='dollared' b3='bthree' _='-'\")" ,
                        "    // @Geci(\"xxx x='x' y='y' z='z'\")" ,
                        "    private static Object something;" ,
                        "    private HashMap<Map<String, Integer>, Object> b;");
            }
        };

        var map = Tools.getParameters(testSource, "aaa","//",null, Pattern.compile(".*something;.*"));
        assertNotNull(map);
        assertEquals(map.get("a"), "b");
        assertEquals(map.get("b"), "c");
        assertEquals(map.get("c"), "d");
        assertEquals(map.get("a$"), "dollared");
        assertEquals(map.get("b3"), "bthree");
        assertEquals(map.get("_"), "-");

    }

}
