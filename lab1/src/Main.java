import java.io.*;
import java.util.*;

public class Main {
    // ͼ�����ݽṹ���ڽӱ��ʾ�����洢����
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();

    // �������·���Ľṹ����
    private static class AllPathsResult {
        List<List<String>> allPaths;
        int distance;

        public AllPathsResult(List<List<String>> allPaths, int distance) {
            this.allPaths = allPaths;
            this.distance = distance;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("������Ҫ���ص��ı��ļ��������� input.txt��: ");
        String filename = scanner.nextLine();
        if (!loadGraphFromFile(filename)) {
            System.out.println("�޷������ļ��������ļ����Ƿ���ȷ��");
            return;
        }

        while (true) {
            System.out.println("\n��ѡ�������");
            System.out.println("1. ��ʾ����ͼ");
            System.out.println("2. ��ѯ�ŽӴ�");
            System.out.println("3. �����ŽӴ��������ı�");
            System.out.println("4. ������������֮������·��");
            System.out.println("5. ����ĳ�����ʵ�PageRankֵ");
            System.out.println("6. ִ���������");
            System.out.println("7. ����ͼΪGraphviz (.dot �ļ�)");
            System.out.println("8. �˳�");
            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("��Ч���룬���������֡�");
                continue;
            }

            switch (choice) {
                case 1:
                    showDirectedGraph(graph);
                    break;
                case 2:
                    System.out.print("�������һ������: ");
                    String word1 = scanner.nextLine().trim().toLowerCase();
                    System.out.print("������ڶ�������: ");
                    String word2 = scanner.nextLine().trim().toLowerCase();
                    System.out.println(queryBridgeWords(word1, word2));
                    break;
                case 3:
                    System.out.print("������ԭʼ�ı�: ");
                    String inputText = scanner.nextLine();
                    System.out.println(generateNewText(inputText));
                    break;
                case 4:
                    System.out.print("��������㵥��: ");
                    String startWord = scanner.nextLine().trim().toLowerCase();
                    System.out.print("�������յ㵥��: ");
                    String endWord = scanner.nextLine().trim().toLowerCase();
                    System.out.println(calcAllShortestPaths(startWord, endWord));
                    break;
                case 5:
                    System.out.print("������Ҫ����PageRank�ĵ���: ");
                    String prWord = scanner.nextLine().trim().toLowerCase();
                    Double prValue = calPageRank(prWord);
                    if (prValue != null)
                        System.out.printf("���� '%s' ��PageRankΪ: %.5f\n", prWord, prValue);
                    else
                        System.out.println("���ʲ�������ͼ�С�");
                    break;
                case 6:
                    System.out.println(randomWalk());
                    break;
                case 7:
                    System.out.print("������Ҫ����� .dot �ļ��������� graph.dot��: ");
                    String dotFilename = scanner.nextLine().trim();
                    try {
                        exportGraphToDotFile(dotFilename);
                        System.out.println("ͼ�ѳɹ��������ļ�: " + dotFilename);
                    } catch (IOException e) {
                        System.out.println("�����ļ�ʱ����: " + e.getMessage());
                    }
                    break;
                case 8:
                    System.out.println("�˳�����");
                    scanner.close();
                    return;
                default:
                    System.out.println("��Чѡ�������ѡ��");
            }
        }
    }

    // ��ʾͼ�Ľṹ�����ٸ���·���ߣ�
    public static void showDirectedGraph(Map<String, Map<String, Integer>> G) {
        for (String node : G.keySet()) {
            System.out.print(node + " -> ");
            boolean first = true;
            for (Map.Entry<String, Integer> edge : G.get(node).entrySet()) {
                if (!first) System.out.print("; ");
                System.out.print(edge.getKey() + "(" + edge.getValue() + ")");
                first = false;
            }
            System.out.println();
        }
    }

    // �����ļ�������ͼ
    public static boolean loadGraphFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            String prevWord = null;
            while ((line = br.readLine()) != null) {
                String[] words = line.toLowerCase().split("\\W+");
                for (String word : words) {
                    if (word.isEmpty()) continue;
                    graph.putIfAbsent(word, new HashMap<>());
                    if (prevWord != null) {
                        graph.get(prevWord).put(word, graph.get(prevWord).getOrDefault(word, 0) + 1);
                    }
                    prevWord = word;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ����Ϊ Graphviz ��ʽ
    public static void exportGraphToDotFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("digraph G {");
            writer.newLine();
            for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
                String fromNode = entry.getKey();
                for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                    String toNode = edge.getKey();
                    int weight = edge.getValue();
                    writer.write("    " + escapeNodeName(fromNode) + " -> " + escapeNodeName(toNode));
                    writer.write(" [label=\"" + weight + "\"];");
                    writer.newLine();
                }
            }
            writer.write("}");
            writer.newLine();
        }
    }

    private static String escapeNodeName(String name) {
        return "\"" + name.replace("\"", "\\\"") + "\"";
    }

    // ��ѯ�ŽӴ�
    public static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        Set<String> bridges = new HashSet<>();
        for (String neighbor : graph.get(word1).keySet()) {
            if (graph.get(neighbor).containsKey(word2)) {
                bridges.add(neighbor);
            }
        }
        if (bridges.isEmpty()) {
            return "No bridge words from '" + word1 + "' to '" + word2 + "'!";
        } else {
            List<String> sortedBridges = new ArrayList<>(bridges);
            Collections.sort(sortedBridges);
            if (sortedBridges.size() == 1) {
                return "The bridge word from '" + word1 + "' to '" + word2 + "' is: " + sortedBridges.get(0) + ".";
            } else {
                return "The bridge words from '" + word1 + "' to '" + word2 + "' are: "
                        + String.join(", ", sortedBridges.subList(0, sortedBridges.size() - 1))
                        + " and " + sortedBridges.get(sortedBridges.size() - 1) + ".";
            }
        }
    }

    // �������ı�
    public static String generateNewText(String inputText) {
        if (graph == null || graph.isEmpty()) {
            return inputText;
        }
        String[] words = inputText.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i].toLowerCase();
            String word2 = words[i + 1].toLowerCase();
            result.append(words[i]).append(" ");
            List<String> bridges = getBridgeWords(word1, word2);
            if (!bridges.isEmpty()) {
                Collections.shuffle(bridges);
                result.append(bridges.get(0)).append(" ");
            }
        }
        result.append(words[words.length - 1]);
        return result.toString();
    }

    private static List<String> getBridgeWords(String word1, String word2) {
        List<String> bridges = new ArrayList<>();
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return bridges;
        }
        for (String neighbor : graph.get(word1).keySet()) {
            if (graph.get(neighbor).containsKey(word2)) {
                bridges.add(neighbor);
            }
        }
        return bridges;
    }

    // �����������·��
    public static String calcAllShortestPaths(String word1, String word2) {
        if (!graph.containsKey(word1)) {
            return "��ʼ���� '" + word1 + "' ����ͼ��";
        }

        // ���������룺���㵽���������ڵ�����·��
        if (word2 == null || word2.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (String target : graph.keySet()) {
                if (target.equals(word1)) continue;
                AllPathsResult resultObj = findAllShortestPaths(word1, target);
                if (resultObj.allPaths.isEmpty()) {
                    result.append("û�д� '").append(word1).append("' �� '").append(target).append("' ��·����\n");
                    continue;
                }
                result.append("�� '").append(word1).append("' �� '").append(target).append("' ���������·����·������: ").append(resultObj.distance).append("��:\n");
                for (List<String> path : resultObj.allPaths) {
                    result.append("  ").append(String.join(" �� ", path)).append("\n");
                }
            }
            return result.toString();
        }

        // ˫�������룺��������֮����������·��
        if (!graph.containsKey(word2)) {
            return "Ŀ�굥�� '" + word2 + "' ����ͼ��";
        }

        AllPathsResult resultObj = findAllShortestPaths(word1, word2);
        if (resultObj.allPaths.isEmpty()) {
            return "û�д� '" + word1 + "' �� '" + word2 + "' ��·����";
        }

        StringBuilder output = new StringBuilder();
        output.append("�������·����·������: ").append(resultObj.distance).append("��:\n");
        for (List<String> path : resultObj.allPaths) {
            output.append("  ").append(String.join(" �� ", path)).append("\n");
        }
        return output.toString();
    }

    // ʹ�� Dijkstra + ���ݲ����������·��
    private static AllPathsResult findAllShortestPaths(String start, String end) {
        Map<String, Integer> dist = new HashMap<>();
        Map<String, List<String>> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue));

        for (String node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(start, 0);
        pq.add(new AbstractMap.SimpleEntry<>(start, 0));

        while (!pq.isEmpty()) {
            Map.Entry<String, Integer> currentEntry = pq.poll();
            String current = currentEntry.getKey();

            if (dist.get(current) == Integer.MAX_VALUE) continue;

            Map<String, Integer> neighbors = graph.getOrDefault(current, Collections.emptyMap());
            for (Map.Entry<String, Integer> edge : neighbors.entrySet()) {
                String neighbor = edge.getKey();
                int weight = edge.getValue();
                int alt = dist.get(current) + weight;

                if (alt < dist.get(neighbor)) {
                    dist.put(neighbor, alt);
                    prev.put(neighbor, new ArrayList<>(Arrays.asList(current)));
                    pq.add(new AbstractMap.SimpleEntry<>(neighbor, alt));
                } else if (alt == dist.get(neighbor)) {
                    prev.computeIfAbsent(neighbor, k -> new ArrayList<>()).add(current);
                }
            }
        }

        if (dist.get(end) == Integer.MAX_VALUE) {
            return new AllPathsResult(Collections.emptyList(), -1);
        }

        List<List<String>> allPaths = new ArrayList<>();
        List<String> path = new ArrayList<>();
        buildAllPaths(prev, end, path, allPaths);

        return new AllPathsResult(allPaths, dist.get(end));
    }

    // �ݹ鹹���������·��
    private static void buildAllPaths(
            Map<String, List<String>> prev,
            String current,
            List<String> path,
            List<List<String>> allPaths) {
        path.add(0, current);
        if (!prev.containsKey(current)) {
            allPaths.add(new ArrayList<>(path));
            path.remove(0);
            return;
        }
        for (String parent : prev.get(current)) {
            buildAllPaths(prev, parent, path, allPaths);
        }
        path.remove(0);
    }

    // PageRank ����
    public static Double calPageRank(String word) {
        if (!graph.containsKey(word)) return null;
        int N = graph.size();  // ͼ�нڵ�����
        double dampingFactor = 0.85;  // ����ϵ��
        // ��ʼ�� PR ֵ����ѡ��ʹ�������Ϊ��ʼȨ�أ�
        Map<String, Double> pageRanks = new HashMap<>();
        Map<String, Integer> inDegrees = computeInDegrees();
        // ʹ�������Ϊ��ʼ PR ֵ����ѡ��Ҳ����ʹ�� TF-IDF�����ȡ������ֵ��
        for (String node : graph.keySet()) {
            int inDegree = inDegrees.getOrDefault(node, 0);
            pageRanks.put(node, 1.0 * (inDegree + 1) / N);  // ��ʼ PR = ��� + 1
        }
        // �ռ�����Ϊ 0 �Ľڵ�
        Set<String> zeroOutDegreeNodes = new HashSet<>();
        for (String node : graph.keySet()) {
            if (graph.get(node).isEmpty()) {
                zeroOutDegreeNodes.add(node);
            }
        }
        // �������ͼ�����ڿ��ٲ�����ߣ�
        Map<String, Set<String>> reverseGraph = buildReverseGraph();
        // �������� PR ֵ
        for (int iter = 0; iter < 100; iter++) {
            Map<String, Double> newRanks = new HashMap<>();
            double totalZeroOutDegreePR = zeroOutDegreeNodes.stream().mapToDouble(pageRanks::get).sum();
            for (String node : graph.keySet()) {
                double sum = 0;
                // ��������ָ��ǰ�ڵ����߽ڵ�
                for (String incoming : reverseGraph.getOrDefault(node, Collections.emptySet())) {
                    int outDegree = graph.get(incoming).size();
                    if (outDegree > 0) {
                        sum += pageRanks.get(incoming) / outDegree;
                    } else {
                        // ����߽ڵ����Ϊ0������ PR ֵ���ָ����нڵ㣨���� totalZeroOutDegreePR ����
                        // ���ﲻ���ظ���ӣ�ֻ������ͳһ����
                    }
                }
                //���ϳ���Ϊ0�Ľڵ���ֵ� PR ֵ
                sum += totalZeroOutDegreePR / N;
                // Ӧ�� PageRank ��ʽ
                newRanks.put(node, (1 - dampingFactor) / N + dampingFactor * sum);
            }
            // ���� PR ֵ
            pageRanks = newRanks;
        }
        return pageRanks.get(word);
    }
    private static Map<String, Integer> computeInDegrees() {
        Map<String, Integer> inDegrees = new HashMap<>();
        for (String from : graph.keySet()) {
            for (String to : graph.get(from).keySet()) {
                inDegrees.put(to, inDegrees.getOrDefault(to, 0) + 1);
            }
        }
        return inDegrees;
    }
    private static Map<String, Set<String>> buildReverseGraph() {
        Map<String, Set<String>> reverseGraph = new HashMap<>();
        for (String from : graph.keySet()) {
            for (String to : graph.get(from).keySet()) {
                reverseGraph.computeIfAbsent(to, k -> new HashSet<>()).add(from);
            }
        }
        return reverseGraph;
    }

    // �������
    public static String randomWalk() {
        Random rand = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        // ���ͼΪ�գ�������ʾ
        if (nodes.isEmpty()) {
            return "ͼ���޽ڵ㣬�޷�����������ߡ�";
        }
        // ���ѡ�����
        String current = nodes.get(rand.nextInt(nodes.size()));
        StringBuilder walk = new StringBuilder(current);
        Set<String> visitedEdges = new HashSet<>();
        while (true) {
            // ��ȡ��ǰ�ڵ���ھ�
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null || neighbors.isEmpty()) {
                walk.append("����ǰ�ڵ��޳��ߣ�");
                break;
            }
            // ������ѡ�ڵ��б�����Ȩ�أ�
            List<String> choices = new ArrayList<>();
            for (Map.Entry<String, Integer> edge : neighbors.entrySet()) {
                for (int i = 0; i < edge.getValue(); i++) {
                    choices.add(edge.getKey());
                }
            }
            // ������к�ѡ�߶��ѷ��ʹ�����ֹ����
            boolean allVisited = true;
            for (String choice : choices) {
                if (!visitedEdges.contains(current + "->" + choice)) {
                    allVisited = false;
                    break;
                }
            }
            if (allVisited) {
                walk.append("�����г����ѷ��ʣ�");
                break;
            }
            // ѡ����һ���ڵ�
            String next;
            do {
                next = choices.get(rand.nextInt(choices.size()));
            } while (visitedEdges.contains(current + "->" + next));
            String edge = current + "->" + next;
            visitedEdges.add(edge);
            walk.append(" ").append(next);
            current = next;
        }
        return "�������·��: " + walk.toString();
    }
}