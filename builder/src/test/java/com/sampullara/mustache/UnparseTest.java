package com.sampullara.mustache;

import com.google.common.base.Function;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Given a template and text, determine the data used to generate them.
 * <p/>
 * User: sam
 * Date: 9/5/11
 * Time: 10:49 AM
 */
public class UnparseTest {

  private static File root;

  @Test
  public void testReallySimpleUnparse() throws MustacheException, IOException {
    MustacheJava c = init();
    Mustache m = c.parseFile("reallysimple.html");
    StringWriter sw = new StringWriter();
    m.execute(sw, new Scope(new Object() {
      String name = "Chris";
      int value = 10000;
    }));
    assertEquals(getContents(root, "reallysimple.txt"), sw.toString());

    Scope scope = m.unexecute(sw.toString());
    assertEquals("Chris", scope.get("name"));
    assertEquals("10000", scope.get("value"));
  }

  @Test
  public void testSimpleUnparse() throws MustacheException, IOException {
    MustacheJava c = init();
    Mustache m = c.parseFile("unambiguoussimple.html");
    StringWriter sw = new StringWriter();
    m.execute(sw, new Scope(new Object() {
      String name = "Chris";
      int value = 10000;

      int taxed_value() {
        return (int) (this.value - (this.value * 0.4));
      }

      boolean in_ca = true;
    }));
    assertEquals(getContents(root, "unambiguoussimple.txt"), sw.toString());

    Scope scope = m.unexecute(sw.toString());
    assertEquals("Chris", scope.get("name"));
    assertEquals("10000", scope.get("value"));
    Scope in_ca = new Scope();
    in_ca.put("taxed_value", "6000");
    assertEquals(Arrays.asList(in_ca), scope.get("in_ca"));

    sw = new StringWriter();
    m.execute(sw, scope);
    assertEquals(getContents(root, "unambiguoussimple.txt"), sw.toString());
  }

  @Test
  public void testComplexUnparse() throws MustacheException, IOException {
    Scope scope = new Scope(new Object() {
      String header = "Colors";
      List item = Arrays.asList(
              new Object() {
                String name = "red";
                boolean current = true;
                String url = "#Red";
              },
              new Object() {
                String name = "green";
                boolean current = false;
                String url = "#Green";
              },
              new Object() {
                String name = "blue";
                boolean current = false;
                String url = "#Blue";
              }
      );

      boolean link(Scope s) {
        return !((Boolean) s.get("current"));
      }

      boolean list(Scope s) {
        return ((List) s.get("item")).size() != 0;
      }

      boolean empty(Scope s) {
        return ((List) s.get("item")).size() == 0;
      }
    });

    MustacheJava c = init();
    Mustache m = c.parseFile("complex.html");
    StringWriter sw = new StringWriter();
    m.execute(sw, scope);
    assertEquals(getContents(root, "complex.txt"), sw.toString());

    scope = m.unexecute(sw.toString());
    sw = new StringWriter();
    m.execute(sw, scope);
    assertEquals(getContents(root, "complex.txt"), sw.toString());

    System.out.println(scope);
  }

  @Test
  public void testPartial() throws MustacheException, IOException {
    MustacheBuilder c = init();
    Mustache m = c.parseFile("template_partial.html");
    StringWriter sw = new StringWriter();
    Scope scope = new Scope();
    scope.put("title", "Welcome");
    scope.put("template_partial_2", new Object() {
      String again = "Goodbye";
    });
    m.execute(sw, scope);
    assertEquals(getContents(root, "template_partial.txt"), sw.toString());

    scope = m.unexecute(sw.toString());
    sw = new StringWriter();
    m.execute(sw, scope);
    System.out.println(scope);
    assertEquals(getContents(root, "template_partial.txt"), sw.toString());
  }

  @Test
  public void testSimpleLamda() throws MustacheException, IOException {
    MustacheBuilder c = new MustacheBuilder(root);
    Mustache m = c.parseFile("explicitlambda.html");
    StringWriter sw = new StringWriter();
    m.execute(sw, new Scope(new Object() {
      Function<String, String> translate = new Function<String, String>() {
        @Override
        public String apply(String input) {
          if (input.equals("Hello")) {
            return "Hola";
          } if (input.equals("Hola")) {
            return "Hello";
          }
          return null;
        }
      };
    }));
    assertEquals(getContents(root, "explicitlambda.txt"), sw.toString());

    Scope scope = m.unexecute(sw.toString());
    sw = new StringWriter();
    m.execute(sw, scope);
    System.out.println(scope);
    assertEquals(getContents(root, "explicitlambda.txt"), sw.toString());
  }

  private MustacheBuilder init() {
    return new MustacheBuilder(root);
  }

  protected String getContents(File root, String file) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(root, file)),"UTF-8"));
    StringWriter capture = new StringWriter();
    char[] buffer = new char[8192];
    int read;
    while ((read = br.read(buffer)) != -1) {
      capture.write(buffer, 0, read);
    }
    return capture.toString();
  }

  @BeforeClass
  public static void setUp() throws Exception {
    File file = new File("src/test/resources");
    root = new File(file, "simple.html").exists() ? file : new File("../src/test/resources");
  }

}
