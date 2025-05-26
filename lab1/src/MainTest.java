import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MainTest {
    private static final Map<String, Map<String, Integer>> testGraph = new HashMap<>();
    @BeforeEach
    void setUp() {
        // ��ʼ������ͼ��ÿ����������ǰ���ã�
        testGraph.clear();
        Main.graph = testGraph; // ���� graph �� Main ��ľ�̬����
    }
    @Test
    void testRandomWalk_EmptyGraph() {
        String result = Main.randomWalk();
        assertEquals("ͼ���޽ڵ㣬�޷�����������ߡ�", result);
    }
    @Test
    void testRandomWalk_SingleNodeNoEdges() {
        testGraph.put("A", new HashMap<>());
        String result = Main.randomWalk();
        assertTrue(result.startsWith("�������·��: A����ǰ�ڵ��޳��ߣ�"));
    }
    @Test
    void testRandomWalk_SingleNodeWithSelfLoop() {
        testGraph.put("A", new HashMap<>(Map.of("A", 1)));
        String result = Main.randomWalk();
        assertTrue(result.startsWith("�������·��: A A�����г����ѷ��ʣ�"));
    }
    @Test
    void testRandomWalk_MultiNodeWithNoCycle() {
        testGraph.put("A", new HashMap<>(Map.of("B", 1)));
        String result = Main.randomWalk();
        assertTrue(result.startsWith("�������·��: A B����ǰ�ڵ��޳��ߣ�")|| result.startsWith("�������·��: B����ǰ�ڵ��޳��ߣ�"));
    }
    @Test
    void testRandomWalk_MultiNodeWithCycle() {
        testGraph.put("A", new HashMap<>(Map.of("B", 1)));
        testGraph.put("B", new HashMap<>(Map.of("A", 1)));
        String result = Main.randomWalk();
        assertTrue(result.startsWith("�������·��: A B A�����г����ѷ��ʣ�")|| result.startsWith("�������·��: B A B�����г����ѷ��ʣ�"));
    }

}