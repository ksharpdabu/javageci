# Accessor Code Generator

The accessor (setter/getter) generator is part of Java::Geci core. It is
implemented in the class `javax0.geci.accessor.Accessor` and can be used
to generate setters and getters. To use it you should create a test

<!-- USE SNIPPET */TestAccessor -->
```java
package javax0.geci.tests.accessor;

import javax0.geci.accessor.Accessor;
import javax0.geci.engine.Geci;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static javax0.geci.api.Source.maven;

public class TestAccessor {

    @Test
    public void testAccessor() throws Exception {
        Assertions.assertFalse(new Geci().source(maven()
                        .module("javageci-examples").mainSource())
                        .register(new Accessor()).generate(),
                Geci.FAILED);
    }
}
```

that will generate the code during test time.

To ignite the code generation for a specific class you have to annotate
the class as

```java
@Geci("accessor ... parameters ...")
```

The mnemonic of the generator is `accessor`.

The generator is implemented as a filtered fields generator thus on the
class level you can define the `filter` parameter to specify which
fields need setters and getters.

The editor-fold segment identifier can also be define using the `id`
parameter or else the value `accessor` will be used as usual, which is
the mnemonic of the generator.

The other parameters usually make sense on the field level. When not
specified on the field level they are inherited from the class
annotation of from the editor-fold parameters if defined there. The
individual fields can also be annotated with

```java
@Geci("accessor ... parameters ...")
```

The following parameters can be used on the field level:

* `filter` can signal that the field does not need setter and getter.
  The value of the parameter in this case should be `false`. Any other
  value will also be interpreted and matched against the current field,
  but any such complexity only decreases the readability of the code.
  You can also use `filter` on the field with the value `true` if the
  global `filter` expression would otherwise exclude the field, but the
  specific field needs an accessor.
    
* `access` can define the access modifier of the setter and the getter.
  The default value is `public`. The value of the parameter will be used
  in front of the setter/getter. The values you can use are `public`,
  `protected`, `private` or `package`. If you use the value `package`
  then there will be no modifier inserted in front of the setter and the
  getter.
  
  In some rare cases you may want to insert something complex like
  `public synchronized` in front of the accessors. In that case you can
  escape from the checking appending a `!` after the the modifier
  expression. That way you should write `access='public synchronized!'`.
  The code generator will remove the trailing `!` and will not check the
  syntax and the correctness of the modifier string. It will be inserted
  into the code exactly as you typed.
  
* `only` can have the value `setter` or `getter`. If it is defined only
  the setter or only the getter will be generated. You can define this
  parameter on the class level if all or most of the fields need only
  setters or only getters. If there are some fields that need both then
  you can specify on the field level `only=''` (empty string) which will
  not limit the generation on that specific field either to getter or to
  setter only but overrides the global setting.
  
* `id` can be defined to use a different segment for the specific field.

*  `setter` can be used to define the name of the setter. If it is not
  defined then the name of the setter will be `set` and the name of the
  field with the first letter upper cased.
 
* `getter` can be used to define the name of the getter. If it is not
  defined then the name of the getter will always be `get` and the name
  of the field with the first letter upper cased.
