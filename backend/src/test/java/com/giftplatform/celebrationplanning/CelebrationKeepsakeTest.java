package com.giftplatform.celebrationplanning;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class CelebrationKeepsakeTest {
 @Test void wishAndNameCannotInjectExecutableMarkup(){String html=CelebrationKeepsake.render("<script>bad()</script>",null,List.of(new CelebrationKeepsake.Message("<img src=x onerror=alert(1)>","<script>alert(1)</script>& hello")),false);assertFalse(html.contains("<script>"));assertFalse(html.contains("<img"));assertTrue(html.contains("&lt;script&gt;"));assertTrue(html.contains("&amp; hello"));assertFalse(html.contains("http://"));}
}
