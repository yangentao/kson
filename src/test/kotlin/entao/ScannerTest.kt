package entao

import io.github.yangentao.kson.TextScanner
import io.github.yangentao.text.CharCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ScannerTest {
    val text: String = """
  {
  name:"entao",
  male: true,
  age: 44;
  ls:[1,2,3];
  }
  """;

    @Test
    fun scanner() {
        val ts: TextScanner = TextScanner(text);
        ts.skipSpaceTabCrLf();
        ts.tryExpectChar(CharCode.LCUB); // {
        ts.printLastBuf();
        assertEquals("{", ts.lastMatch)

        ts.skipSpaceTabCrLf();

        ts.expectIdent(); // name
        ts.printLastBuf();
        assertEquals("name", ts.lastMatch)

        ts.skipSpaceTab();
        ts.tryExpectChar(CharCode.COLON); // :
        ts.skipSpaceTab();
        ts.tryExpectChar(CharCode.QUOTE); // "
        // ts.skip();
        ts.moveNext(terminator = { it == CharCode.QUOTE })
        ts.skip();
        ts.printLastBuf();
        assertEquals("entao", ts.lastMatch)

        ts.skipChars(CharCode.SpTabCrLf + listOf(CharCode.COMMA, CharCode.SEMI))
        ts.skipSpaceTabCrLf();

        ts.tryExpectString("male");
        ts.printLastBuf();
    }
}