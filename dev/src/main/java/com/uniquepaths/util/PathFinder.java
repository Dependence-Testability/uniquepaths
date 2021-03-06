package com.uniquepaths.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathFinder {

  /**
   * Brute Force implementation for computing the average length
   * of the paths in a graph between two nodes.
   *
   * @param graph the graph for which the average path length between
   *     two nodes is to be determined.
   * @param start the value representing the node to start our
   *     graph traversal from.
   * @param end the value representing the node to end our graph
   *     traversal on.
   *
   * @return the number of unique paths between <code>start</code>
   *     and <code>end</code>
   *
   */
  public static <T> double[] uniquePaths(Graph<T> graph,
      T start, T end) {
    Node<T> sNode = graph.getNode(start);
    Node<T> eNode = graph.getNode(end);
    ArrayList<Integer> counts = new ArrayList<>();
    int numberOfPaths = uniquePaths(sNode,
        eNode, 0, counts);
    int sum = 0;
    for (int length : counts) {
      sum += length;
    }
    return new double[]{(double) numberOfPaths,
        numberOfPaths == 0.0 ? 0.0 : (double) sum/numberOfPaths};
  }

  /**
   * Auxiliary function to help determine the number of paths between
   * two nodes in a graph.
   *
   * @param curr the node to start graph traversal from.
   * @param end the node to end our graph traversal on.
   * @param length the current length of the path provided that the
   *     <code>curr</code> has a path that leads to <code>end</code>.
   * @param counts the record of path lengths that we have determined
   *     thus far.
   *
   * @return the number of unique paths between <code>start</code>
   *     and <code>end</code>
   *
   */
  private static <T> int uniquePaths(Node<T> curr,
      Node<T> end, int length, ArrayList<Integer> counts) {
    if (curr.getValue() == end.getValue()) {
      counts.add(length);
      return 1;
    }
    int pathCount = 0;
    curr.setVisited(true);
    Node<T> node;
    for (Map.Entry<Node<T>, Integer> entry : curr.getEdges()) {
      node = entry.getKey();
      if (!node.visited()) {
        pathCount += uniquePaths(node, end, length + 1, counts);
      }
    }
    curr.setVisited(false);
    return pathCount;
  }

  /**
   * Finds the number of paths that exist between two nodes
   * in a graph as well as the average lengths of those paths.
   *
   * @param graph the Directed Acyclical Graph from which we
   *     are to determine the number of paths and average lengths
   *     of those paths.
   * @param start the value in the contracted graph from which we are to
   *     begin traversal
   * @param end the value in the contracted graph at which we are to end
   *     our traversal
   * @param start the value in the original graph from which we are to
   *     begin traversal
   * @param end the value in the original graph at which we are to end
   *     our traversal
   *
   * @return an array containing the number of paths from
   *     <code>start</code> to <code>end</code> in <code>graph</code>
   *     and the average length of those paths.
   *
   */
  public static <T> double[] dagTraversal(Graph<T> graph, Graph<T> contracted,
      T sStart, T sEnd, T start, T end) {
    SuperNode<T> sNode = (SuperNode<T>) contracted.getNode(sStart);
    SuperNode<T> eNode = (SuperNode<T>) contracted.getNode(sEnd);
    Node<T> s = graph.getNode(start);
    Node<T> e = graph.getNode(end);
    SCC<T> entrySCC = sNode.scc;
    SCC<T> exitSCC = eNode.scc;
    computeEntryExitSCC(entrySCC, exitSCC, s, e);
    List<Node<T>> dfsOrdered = dfsTopoSort(contracted, sNode, eNode);
    sNode.addDistance((int)entrySCC.getTotalAvgPathLength(),
        entrySCC.getTotalNumberPaths());
    eNode.addDistance((int)exitSCC.getTotalAvgPathLength(),
        exitSCC.getTotalNumberPaths());
    eNode.setVisited(true);
    System.out.println(dfsOrdered);
    calculatePath(dfsOrdered, 0, sNode);
    double lengthSums = 0.0;
    double totalNumPaths = 0.0;
    for (Map.Entry<Integer, Integer> entry : sNode.getDistances().entrySet()) {
      lengthSums += entry.getValue() * entry.getKey();
      totalNumPaths += entry.getValue();
    }
    lengthSums = totalNumPaths == 0.0 ? 0.0
        : lengthSums/totalNumPaths;
    PathApproximation.resetGraph(graph);
    return new double[]{totalNumPaths, lengthSums};
  }

  private static <T> void computeEntryExitSCC(SCC<T> entry, SCC<T> exit,
      Node<T> s, Node<T> e) {
    double[] result = new double[2];
    int result1 = 1;
    double result2 = 1.0;
    if (entry.size() > 1) {
      for (Node<T> node : entry.getOutNodes()) {
        // result = PathApproximation.lengthDistribution(entry, s.getValue(),
        //     node.getValue());
        result = uniquePaths(entry, s.getValue(), node.getValue());
        result1 += result[0];
        result2 += result[1] * result[0];
      }
      result2 = result1 == 0.0 ? 0.0 : result2/result1;
      entry.setTotalNumberPaths((int)result1);
      entry.setTotalAvgPathLength(result2);
    }

    result1 = 1;
    result2 = 1.0;

    if (exit.size() > 1) {
      for (Node<T> node : exit.getInNodes()) {
        // result = PathApproximation.lengthDistribution(exit,
        //     node.getValue(), e.getValue());
        result = uniquePaths(exit, node.getValue(), e.getValue());
        result1 += result[0];
        result2 += result[1] * result[0];
      }
      result2 = result1 == 0.0 ? 0.0 : result2/result1;
      exit.setTotalNumberPaths((int)Math.round(result1));
      exit.setTotalAvgPathLength(result2);
    }
  }

  /**
   * Performs a Topologicial Sort of the nodes in graph in
   * order of Depth-First Search.
   *
   * @param graph the graph for which the Topological Sort
   *     is to take place on.
   * @param sNode the node to begin the Topological Sort at.
   *
   * @return an {@link List} of the nodes in order of Topological Sort
   *
   */
  private static <T> List<Node<T>> dfsTopoSort(Graph<T> graph, Node<T> sNode, Node<T> eNode) {
    List<Node<T>> topoSorted = new ArrayList<>();
    Set<Node<T>> tSortSet = new HashSet<>();
    dfsTopoSort(topoSorted, tSortSet, sNode, eNode);

    // Since topological sort returns the list of nodes in order of
    // finishing times, the first node we exam (our start node), will
    // always be the last node in the list. For clarity in the algorithm
    // for computing the number of paths from s to t as well as the
    // average length of those paths between them, we will reverse the list.
    Collections.reverse(topoSorted);
    return topoSorted;
  }

  /** Auxiliary method to aid in computing the Topological Sort */
  private static <T> void dfsTopoSort(List<Node<T>> tSorted,
      Set<Node<T>> tSortSet, Node<T> node, Node<T> end) {
    Node<T> adjNode;
    node.setVisited(true);
    for (Map.Entry<Node<T>, Integer> edge : node.getEdges()) {
      adjNode = edge.getKey();
      if (!adjNode.visited() && !tSortSet.contains(adjNode)) {
        dfsTopoSort(tSorted, tSortSet, adjNode, end);
      }
    }
    node.setVisited(false);
    tSorted.add(node);
    tSortSet.add(node);
  }

  /**
   * Computes the number of paths that exist between two nodes in a graph.
   * 
   * For this algorithm we perform a Depth-First Search (DFS) and only look
   * at unvisited adjacent nodes that succeed the current node in order of 
   * Topological Sort. Because this is a Directed Acyclical Graph (DAG) we
   * can guarantee that any valid-adjacent nodes are nodes that we would
   * have to repeatedly visit everytime we come across that node in the graph.
   * Due to this property we only have to visit each node in the graph once 
   * and make a record of the number of possible paths from that node to the 
   * destination node, any node that has a directed edge to that node can then 
   * aggregate its path count based off of this pre-existing record for its 
   * adjacent node.
   *
   * @param sorted a list of nodes sorted according to Topological Sort. The
   *     nodes we are computing the number of paths for in this graph must be 
   *     contained within this list.
   * @param position the current position in <code>sorted</code>
   * @param curr the node for which we are computing the number of paths
   *     to the final node in the topological sort.
   *
   */
  private static <T> void calculatePath(List<Node<T>> sorted,
      int position, Node<T> curr) {
    int numPaths = 0;
    double avgPathLen = 0.0;
    int avgPathRound;
    int currCount;
    if (!curr.visited()) {
      curr.setVisited(true);
      for (int i = position + 1; i < sorted.size(); ++i) {
        if (curr.hasEdge(sorted.get(i))) {
          Node<T> adj = sorted.get(i);
          SuperNode<T> currCasted = (SuperNode<T>) curr;
          SuperNode<T> adjCasted = (SuperNode<T>) adj;
          numPaths = currCasted.scc.getTotalNumberPaths();
          avgPathLen = currCasted.scc.getTotalAvgPathLength();
          avgPathRound = (int) Math.round(avgPathLen);
          calculatePath(sorted, i, adj);
          Map<T, Integer> weightValues
              = currCasted.getEdgeWeights(adjCasted.getValue());
          int edgeCount = currCasted.getEdgeCount(adjCasted.getValue());
          for (Map.Entry<Integer, Integer> entry
              : adj.getDistances().entrySet()) {
            double average = 0.0;
            for (Map.Entry<T, Integer> outEdge : weightValues.entrySet()) {
              average += outEdge.getValue();
            }
            currCount = curr.getDistanceCount(entry.getKey())
                == null ? 0 : curr.getDistanceCount(
                    entry.getKey() + avgPathRound);
            curr.addDistance(entry.getKey() + avgPathRound + (int) average,
                (entry.getValue() + currCount) * numPaths + edgeCount);
          }
        }
      }
    }
  }
}
