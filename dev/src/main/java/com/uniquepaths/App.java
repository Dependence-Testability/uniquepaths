package com.uniquepaths;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.List;

import com.uniquepaths.util.Edge;
import com.uniquepaths.util.Graph;
import com.uniquepaths.util.Node;
import com.uniquepaths.util.PathApproximation;
import com.uniquepaths.util.PathFinder;
import com.uniquepaths.util.SCC;
import com.uniquepaths.util.StronglyConnectedComponents;

public class App {
    
  public static void main(String[] args) {
    mrTest(args[0]);
  }

  private static void mrTest(String fileName) {
    Graph<Integer> graph;
    Graph<Integer> contracted;
    List<SCC<Integer>> sccs;
    List<SCC<Integer>> permuted;
    double[] result;
    int s = 1;
    int e = 21;

    System.out.println("Stage 1: Preparation; Pre mapreduce stage");
    graph = readGraphFromFile(fileName);
    sccs = StronglyConnectedComponents.getStronglyConnectedComponents(graph);
    System.out.println("Stage 2: Mapper Stage");
    System.out.println("Number of SCCS: " + sccs.size());
    for (SCC<Integer> scc : sccs) {
      scc.computeInternalDistances();
      if (scc.containsNode(s)) {
        scc.getInNodes().clear();
        scc.addInNode(s);
      }
      if (scc.containsNode(e)) {
        scc.getOutNodes().clear();
        scc.addOutNode(e);
      }
      if (scc.size() == 1) {
        List<Map.Entry<Integer, Node<Integer>>> entries
            = new ArrayList<>(scc.getNodes());
        scc.getInNodes().clear();
        scc.getOutNodes().clear();
        scc.getInNodes().add(entries.get(0).getValue());
        scc.getOutNodes().add(entries.get(0).getValue());
      }
    }

    permuted = StronglyConnectedComponents.getPermutedSCCs(sccs);
    System.out.println("original: " + sccs.size()
        + "    permutations: " + permuted.size());
    for (SCC<Integer> scc : permuted) {
      scc.computeInternalDistances();
    }

    System.out.println("Stage 3: Reduce");
    contracted = StronglyConnectedComponents.contractSCCs(sccs, graph);
    Node<Integer> start = graph.getNode(s);
    Node<Integer> end = graph.getNode(e);

    result = PathFinder.dagTraversal(graph, contracted,
        sccs.get(start.getSccId()).getExpandedNodes().get(0).getValue(),
        sccs.get(end.getSccId()).getExpandedNodes().get(0).getValue(), s, e);

    System.out.println("MapReduce Results: ");
    System.out.println("\tNumber of Paths: " + result[0]);
    System.out.println("\tAverage length of paths: " + result[1]);
    result = PathFinder.uniquePaths(graph, s, e);
    System.out.println("Actual Results: ");
    System.out.println("\tNumber of Paths: " + result[0]);
    System.out.println("\tAverage length of paths: " + result[1]);
  }

  private static Graph<Integer> readGraphFromFile(String fileName) {
    File file = null;
    Scanner scan = null;
    Graph<Integer> graph = null;
    try {
      file = new File(fileName);
      scan = new Scanner(file);
      graph = new Graph<>();
      while (scan.hasNextLine()) {
        String line = scan.nextLine();
        String[] coords = line.split(" ");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        graph.addEdge(x, y);
      } 
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (scan != null) {
        scan.close();
      }
    }
    return graph;
  }
}
