package brig.concord;

import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class SimpleCodeInsightTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/code-insight";
    }

    public void testAnnotator() {
        myFixture.configureByFiles("generic-trigger.concord.yml");
        myFixture.checkHighlighting(false, false, true, true);
    }

//        public void testCompletion() {
//            myFixture.configureByFiles("CompleteTestData.java", "DefaultTestData.simple");
//            myFixture.complete(CompletionType.BASIC, 1);
//            List<String> strings = myFixture.getLookupElementStrings();
//            assertTrue(strings.containsAll(Arrays.asList("key with spaces", "language", "message", "tab", "website")));
//            assertEquals(5, strings.size());
//        }
//    }
}
