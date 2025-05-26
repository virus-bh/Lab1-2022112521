import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MainTest {
    private static final Map<String, Map<String, Integer>> testGraph = new HashMap<>();
    @BeforeEach
    void setUp() {
        // 初始化测试图（每个测试用例前重置）
        testGraph.clear();
        Main.graph = testGraph; // 假设 graph 是 Main 类的静态变量
    }
    @Test
    void testRandomWalk_MultiNodeWithCycle() {
        testGraph.put("A", new HashMap<>(Map.of("B", 1)));
        testGraph.put("B", new HashMap<>(Map.of("A", 1)));
        String result = Main.randomWalk();
        assertTrue(result.startsWith("随机游走路径: A B A（所有出边已访问）")|| result.startsWith("随机游走路径: B A B（所有出边已访问）"));
    }
}