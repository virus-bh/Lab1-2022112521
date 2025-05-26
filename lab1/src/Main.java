import java.io.*;
import java.util.*;

public class Main {
    // 图的数据结构：邻接表表示法，存储出边
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();

    // 所有最短路径的结构化类
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
        System.out.print("请输入要加载的文本文件名（例如 input.txt）: ");
        String filename = scanner.nextLine();
        if (!loadGraphFromFile(filename)) {
            System.out.println("无法加载文件，请检查文件名是否正确。");
            return;
        }

        while (true) {
            System.out.println("\n请选择操作：");
            System.out.println("1. 显示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算两个单词之间的最短路径");
            System.out.println("5. 计算某个单词的PageRank值");
            System.out.println("6. 执行随机游走");
            System.out.println("7. 导出图为Graphviz (.dot 文件)");
            System.out.println("8. 退出");
            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("无效输入，请输入数字。");
                continue;
            }

            switch (choice) {
                case 1:
                    showDirectedGraph(graph);
                    break;
                case 2:
                    System.out.print("请输入第一个单词: ");
                    String word1 = scanner.nextLine().trim().toLowerCase();
                    System.out.print("请输入第二个单词: ");
                    String word2 = scanner.nextLine().trim().toLowerCase();
                    System.out.println(queryBridgeWords(word1, word2));
                    break;
                case 3:
                    System.out.print("请输入原始文本: ");
                    String inputText = scanner.nextLine();
                    System.out.println(generateNewText(inputText));
                    break;
                case 4:
                    System.out.print("请输入起点单词: ");
                    String startWord = scanner.nextLine().trim().toLowerCase();
                    System.out.print("请输入终点单词: ");
                    String endWord = scanner.nextLine().trim().toLowerCase();
                    System.out.println(calcAllShortestPaths(startWord, endWord));
                    break;
                case 5:
                    System.out.print("请输入要计算PageRank的单词: ");
                    String prWord = scanner.nextLine().trim().toLowerCase();
                    Double prValue = calPageRank(prWord);
                    if (prValue != null)
                        System.out.printf("单词 '%s' 的PageRank为: %.5f\n", prWord, prValue);
                    else
                        System.out.println("单词不存在于图中。");
                    break;
                case 6:
                    System.out.println(randomWalk());
                    break;
                case 7:
                    System.out.print("请输入要保存的 .dot 文件名（例如 graph.dot）: ");
                    String dotFilename = scanner.nextLine().trim();
                    try {
                        exportGraphToDotFile(dotFilename);
                        System.out.println("图已成功导出到文件: " + dotFilename);
                    } catch (IOException e) {
                        System.out.println("保存文件时出错: " + e.getMessage());
                    }
                    break;
                case 8:
                    System.out.println("退出程序。");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效选项，请重新选择。");
            }
        }
    }

    // 显示图的结构（不再高亮路径边）
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

    // 加载文件并构建图
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

    // 导出为 Graphviz 格式
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

    // 查询桥接词
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

    // 生成新文本
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

    // 计算所有最短路径
    public static String calcAllShortestPaths(String word1, String word2) {
        if (!graph.containsKey(word1)) {
            return "起始单词 '" + word1 + "' 不在图中";
        }

        // 单单词输入：计算到所有其他节点的最短路径
        if (word2 == null || word2.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (String target : graph.keySet()) {
                if (target.equals(word1)) continue;
                AllPathsResult resultObj = findAllShortestPaths(word1, target);
                if (resultObj.allPaths.isEmpty()) {
                    result.append("没有从 '").append(word1).append("' 到 '").append(target).append("' 的路径。\n");
                    continue;
                }
                result.append("从 '").append(word1).append("' 到 '").append(target).append("' 的所有最短路径（路径长度: ").append(resultObj.distance).append("）:\n");
                for (List<String> path : resultObj.allPaths) {
                    result.append("  ").append(String.join(" → ", path)).append("\n");
                }
            }
            return result.toString();
        }

        // 双单词输入：计算它们之间的所有最短路径
        if (!graph.containsKey(word2)) {
            return "目标单词 '" + word2 + "' 不在图中";
        }

        AllPathsResult resultObj = findAllShortestPaths(word1, word2);
        if (resultObj.allPaths.isEmpty()) {
            return "没有从 '" + word1 + "' 到 '" + word2 + "' 的路径。";
        }

        StringBuilder output = new StringBuilder();
        output.append("所有最短路径（路径长度: ").append(resultObj.distance).append("）:\n");
        for (List<String> path : resultObj.allPaths) {
            output.append("  ").append(String.join(" → ", path)).append("\n");
        }
        return output.toString();
    }

    // 使用 Dijkstra + 回溯查找所有最短路径
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

    // 递归构建所有最短路径
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

    // PageRank 箘法
    public static Double calPageRank(String word) {
        if (!graph.containsKey(word)) return null;
        int N = graph.size();  // 图中节点总数
        double dampingFactor = 0.85;  // 阻尼系数
        // 初始化 PR 值（可选：使用入度作为初始权重）
        Map<String, Double> pageRanks = new HashMap<>();
        Map<String, Integer> inDegrees = computeInDegrees();
        // 使用入度作为初始 PR 值（可选：也可以使用 TF-IDF、出度、或均等值）
        for (String node : graph.keySet()) {
            int inDegree = inDegrees.getOrDefault(node, 0);
            pageRanks.put(node, 1.0 * (inDegree + 1) / N);  // 初始 PR = 入度 + 1
        }
        // 收集出度为 0 的节点
        Set<String> zeroOutDegreeNodes = new HashSet<>();
        for (String node : graph.keySet()) {
            if (graph.get(node).isEmpty()) {
                zeroOutDegreeNodes.add(node);
            }
        }
        // 构建入边图（用于快速查找入边）
        Map<String, Set<String>> reverseGraph = buildReverseGraph();
        // 迭代更新 PR 值
        for (int iter = 0; iter < 100; iter++) {
            Map<String, Double> newRanks = new HashMap<>();
            double totalZeroOutDegreePR = zeroOutDegreeNodes.stream().mapToDouble(pageRanks::get).sum();
            for (String node : graph.keySet()) {
                double sum = 0;
                // 遍历所有指向当前节点的入边节点
                for (String incoming : reverseGraph.getOrDefault(node, Collections.emptySet())) {
                    int outDegree = graph.get(incoming).size();
                    if (outDegree > 0) {
                        sum += pageRanks.get(incoming) / outDegree;
                    } else {
                        // 若入边节点出度为0，则其 PR 值均分给所有节点（已由 totalZeroOutDegreePR 处理）
                        // 这里不再重复添加，只在最终统一处理
                    }
                }
                //加上出度为0的节点均分的 PR 值
                sum += totalZeroOutDegreePR / N;
                // 应用 PageRank 公式
                newRanks.put(node, (1 - dampingFactor) / N + dampingFactor * sum);
            }
            // 更新 PR 值
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

    // 随机游走
    public static String randomWalk() {
        Random rand = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        // 如果图为空，返回提示
        if (nodes.isEmpty()) {
            return "图中无节点，无法进行随机游走。";
        }
        // 随机选择起点
        String current = nodes.get(rand.nextInt(nodes.size()));
        StringBuilder walk = new StringBuilder(current);
        Set<String> visitedEdges = new HashSet<>();
        while (true) {
            // 获取当前节点的邻居
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null || neighbors.isEmpty()) {
                walk.append("（当前节点无出边）");
                break;
            }
            // 构建候选节点列表（根据权重）
            List<String> choices = new ArrayList<>();
            for (Map.Entry<String, Integer> edge : neighbors.entrySet()) {
                for (int i = 0; i < edge.getValue(); i++) {
                    choices.add(edge.getKey());
                }
            }
            // 如果所有候选边都已访问过，终止游走
            boolean allVisited = true;
            for (String choice : choices) {
                if (!visitedEdges.contains(current + "->" + choice)) {
                    allVisited = false;
                    break;
                }
            }
            if (allVisited) {
                walk.append("（所有出边已访问）");
                break;
            }
            // 选择下一个节点
            String next;
            do {
                next = choices.get(rand.nextInt(choices.size()));
            } while (visitedEdges.contains(current + "->" + next));
            String edge = current + "->" + next;
            visitedEdges.add(edge);
            walk.append(" ").append(next);
            current = next;
        }
        return "随机游走路径: " + walk.toString();
    }
}