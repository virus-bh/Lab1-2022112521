import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    private static Map<String, Map<String, Integer>> testGraph;

    @BeforeEach
    void setUp() {
        testGraph = new HashMap<>();
        Main.graph = testGraph; // 假设 graph 是 Main 类的静态变量
    }
    // 基本路径1：163-164-185
    @Test
    void testPath1_WordNotInGraph() {
        testGraph.put("A", new HashMap<>());
        assertEquals("No X or A in the graph!", Main.queryBridgeWords("X", "A"));
        assertEquals("No A or Y in the graph!", Main.queryBridgeWords("A", "Y"));
    }

    // 基本路径2：163-167-172-173-185
    @Test
    void testPath2_NoBridgeWords() {
        testGraph.put("A", new HashMap<>());
        testGraph.put("B", new HashMap<>());
        assertEquals("No bridge words from 'A' to 'B'!",
                Main.queryBridgeWords("A", "B"));
    }

    // 基本路径3：163-167-172-174-178-185（单个桥接词）

    // 基本路径4：163-167-172-174-179-185（多个桥接词）

    // 基本路径5：164-167-168-167-172-174-179-185
    @Test
    void testPath3_SingleBridgeWord() {
        testGraph.put("A", Map.of("B", 1));
        testGraph.put("B", Map.of("C", 1));
        testGraph.put("C", new HashMap<>());
        assertEquals("The bridge word from 'A' to 'C' is: B.",
                Main.queryBridgeWords("A", "C"));
    }

    // 基本路径6：164-167-168-169-167-172-174-179-185
    @Test
    void testPath4_MultipleBridgeWords() {
        testGraph.put("A", Map.of("B", 1, "C", 1));
        testGraph.put("B", Map.of("D", 1));
        testGraph.put("C", Map.of("D", 1));
        testGraph.put("D", new HashMap<>());

        String result = Main.queryBridgeWords("A", "D");
        assertTrue(result.startsWith("The bridge words from 'A' to 'D' are: "));
        assertTrue(result.contains("B") && result.contains("C"));
        assertTrue(result.endsWith("."));
    }
}