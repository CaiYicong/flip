package org.alibaba;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WordGraphTest {
    private final String wordsFileStr = WordGraphTest.class.getResource("/linux.words").getFile();
    private final String parsedFileStr = wordsFileStr + ".graph";
    private WordGraph wordGraph;

    @Before
    public void setUp() throws Exception {
        wordGraph = new WordGraph();
        File parsed = new File(parsedFileStr);
        if (parsed.exists()) {
            wordGraph.loadParsed(parsedFileStr);
        } else {
            wordGraph.parseSource(wordsFileStr, parsedFileStr);
        }
    }

    @Test
    public void getFlipPath() {
        String[] expectList;
        List<String> realList;

        realList = wordGraph.getFlipPath("", "pig");
        assertNull(realList);

        realList = wordGraph.getFlipPath(null, "pig");
        assertNull(realList);

        realList = wordGraph.getFlipPath("big", null);
        assertNull(realList);

        expectList = new String[]{"cat", "bat", "bag", "big", "pig"};
        realList = wordGraph.getFlipPath("cat", "pig");
        for (int i = 0; i < expectList.length; i++) {
            assertEquals("From cat To pig", expectList[i], realList.get(i));
        }

        realList = wordGraph.getFlipPath("qqv", "pig");
        assertNull(realList);

        expectList = new String[]{"cat", "bat"};
        realList = wordGraph.getFlipPath("cat", "bat");
        for (int i = 0; i < expectList.length; i++) {
            assertEquals("From cat To bat", expectList[i], realList.get(i));
        }
    }
}