package com.uniquepaths.util;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SCC<T> extends Graph<T> {

  private Set<Node<T>> outNodes;
  private Set<Node<T>> inNodes;
  private List<Node<T>> nodeList;
  private Map<Node<T>, Map<Node<T>, Integer>> numberPaths;
  private Map<Node<T>, Map<Node<T>, Double>> avgPathLengths;
  private int totalNumberOfPaths;
  private double totalAvgPathLength;

  public SCC() {
    super();
    this.outNodes = new HashSet<>();
    this.inNodes = new HashSet<>();
    this.nodeList = new ArrayList<>();
    this.numberPaths = new HashMap<>();
    this.avgPathLengths = new HashMap<>();
  }

  public int size() {
    return nodeList.size();
  }

  public void addNode(Node<T> node) {
    T value = (T) node.getValue();
    getOrAddNode(value);
    nodeList.add(node);
  }

  public void addInNode(T value) {
    Node<T> node = getOrAddNode(value);
    inNodes.add(node);
  }

  public Set<Node<T>> getInNodes() {
    return inNodes;
  }

  public void addOutNode(T value) {
    Node<T> node = getOrAddNode(value);
    outNodes.add(node);
  }

  public Set<Node<T>> getOutNodes() {
    return outNodes;
  }

  public List<Node<T>> getExpandedNodes() {
    return nodeList;
  }

  public boolean containsNode(Node<T> node) {
    return containsNode(node.getValue());
  }

  public boolean containsNode(T value) {
    return nodeMap.containsKey(value);
  }

  public int getTotalNumberPaths() {
    return totalNumberOfPaths;
  }

  public int getNumberPaths(T start, T end) {
    Node<T> sNode = nodeMap.get(start);
    Node<T> eNode = nodeMap.get(end);
    Map<Node<T>, Integer> map = numberPaths.get(sNode);
    int numPaths = 0;
    if (map != null) {
      numPaths = map.get(eNode);
    }
    return numPaths;
  }

  public double getTotalAvgPathLength() {
    return totalAvgPathLength;
  }

  public double getAvgPathLength(T start, T end) {
    Node<T> sNode = nodeMap.get(start);
    Node<T> eNode = nodeMap.get(end);
    Map<Node<T>, Double> map = avgPathLengths.get(sNode);
    double avgLength = 0.0;
    if (map != null) {
      avgLength = map.get(eNode);
    }
    return avgLength;
  }

  public void computeInternalDistances() {
    double[] result;
    int pathCount;
    double avgLength;
    Graph<T> curr = this;
    this.totalNumberOfPaths = 0;
    this.totalAvgPathLength = 0.0;
    for (Node<T> in : inNodes) {
      for (Node<T> out : outNodes) {
        result = PathApproximation.lengthDistribution(curr, in.getValue(),
            out.getValue());
        pathCount = (int) result[0];
        avgLength = result[1];
        numberPaths.get(in).put(out, pathCount);
        avgPathLengths.get(in).put(out, avgLength);
        System.out.println("pathCount: " + pathCount);
        System.out.println("avgLength: " + avgLength);
        totalNumberOfPaths += pathCount;
        totalAvgPathLength += (pathCount * avgLength);
      }
    }
    totalAvgPathLength = totalNumberOfPaths == 0 ?
        0.0 : totalAvgPathLength/totalNumberOfPaths;
  }

  public void addEdges(Graph<T> graph, Graph<T> transpose) {
    Node<T> wholeNode;
    for (Node<T> node : nodeList) {
      wholeNode = graph.getNode(node.getValue());
      for (Map.Entry<Node<T>, Integer> entry : wholeNode.getEdges()) {
        if (nodeMap.containsKey(entry.getKey().getValue())) {
          addEdge(node.getValue(), entry.getKey().getValue());
        } else {
          this.outNodes.add(node);
        }
      }

      wholeNode = transpose.getNode(node.getValue());
      for (Map.Entry<Node<T>, Integer> entry : wholeNode.getEdges()) {
        if (!nodeMap.containsKey(entry.getKey().getValue())) {
          this.inNodes.add(node);
          this.numberPaths.put(node, new HashMap<Node<T>, Integer>());
          this.avgPathLengths.put(node, new HashMap<Node<T>, Double>());
        }
      }
    }
    //System.out.println("In Nodes: " + inNodes);
    //System.out.println("Out Nodes: " + outNodes);
  }

  public String toString() {
    StringBuilder strBldr = new StringBuilder();
    strBldr.append(super.toString());
    strBldr.append("outNodes: ");
    strBldr.append(outNodes);
    strBldr.append("\ninNodes: ");
    strBldr.append(inNodes);
    strBldr.append("\n");
    return strBldr.toString();
  }
}
