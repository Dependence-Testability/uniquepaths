package com.uniquepaths.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class StronglyConnectedComponents {

  public static <T> List<SCC<T>> getStronglyConnectedComponents(
      Graph<T> graph) {
    int index = 0;
    HashMap<Node<T>, Integer> lowLinkMap = new HashMap<>();
    HashMap<Node<T>, Integer> indexMap = new HashMap<>();
    Graph<T> transpose = getTranspose(graph);
    Stack<Node<T>> stack = new Stack<>();
    List<SCC<T>> sccList = new ArrayList<>();
    Node<T> curr;

    for (Map.Entry<T, Node<T>> entry : graph.getNodes()) {
      curr = entry.getValue();
      if (indexMap.get(curr) == null) {
        strongConnect(graph, transpose, curr, sccList, lowLinkMap, indexMap,
            stack, index);
      }
    }
    return sccList;
  }

  private static <T> void strongConnect(Graph<T> graph, Graph<T> transpose,
      Node<T> node, List<SCC<T>> sccList, HashMap<Node<T>, Integer> lowLinkMap,
      HashMap<Node<T>, Integer> indexMap, Stack<Node<T>> stack, int index) {
    Node<T> edgeNode;
    Node<T> stackNode;
    SCC<T> scc;
    int sccId;

    indexMap.put(node, index);
    lowLinkMap.put(node, index);
    ++index;
    stack.push(node);
    node.setOnStack(true);

    for (Map.Entry<Node<T>, Integer> edge : node.getEdges()) {
      edgeNode = edge.getKey();
      if (!indexMap.containsKey(edgeNode)) {
        strongConnect(graph, transpose, edgeNode, sccList, lowLinkMap,
            indexMap, stack, index);
        lowLinkMap.put(node,
            Integer.min(lowLinkMap.get(node), lowLinkMap.get(edgeNode)));
      } else if (edgeNode.onStack()) {
        lowLinkMap.put(node,
            Integer.min(lowLinkMap.get(node), lowLinkMap.get(edgeNode)));
      }
    }

    if (lowLinkMap.get(node) == indexMap.get(node)) {
      scc = new SCC<>();
      sccId = sccList.size();
      do {
        stackNode = stack.pop();
        stackNode.setOnStack(false);
        scc.addNode(stackNode);
        stackNode.setSccId(sccId);
        scc.setSccId(sccId);
      } while (stackNode != node);
      scc.addEdges(graph, transpose);
      sccList.add(scc);
    }
  }

  public static <T> Graph<T> contractSCCs(List<SCC<T>> sccList,
       Graph<T> graph) {
    Graph<T> contracted = new Graph<>();
    SCC<T> scc;
    SuperNode<T> superNode;
    Node<T> adjNode;
    T value;
    int sccId;

    for (int i = 0; i < sccList.size(); ++i) {
      scc = sccList.get(i);
      contracted.addSuperNode(i, scc);
    }
    for (int i = 0; i < sccList.size(); ++i) {
      scc = sccList.get(i);
      value = (T) scc.getExpandedNodes().get(0).getValue();
      for (Node<T> expandedNode : scc.getExpandedNodes()) {
        for (Map.Entry<Node<T>, Integer> edge : expandedNode.getEdges()) {
          adjNode = edge.getKey();
          if (!scc.containsNode(adjNode)) {
            sccId = adjNode.getSccId();
            contracted.addEdge(value,
                sccList.get(sccId).getExpandedNodes().get(0).getValue());
            superNode = (SuperNode<T>) contracted.getNode(value);
            for (Node<T> outNode : scc.getOutNodes()) {
              for (Map.Entry<Node<T>, Integer> outEdge : outNode.getEdges()) {
                Node<T> outEdgeNode = outEdge.getKey();
                if (!scc.containsNode(outEdgeNode)) {
                  int externalSCCId = outEdgeNode.getSccId();
                  superNode.incrementEdgeCount(sccList.get(externalSCCId)
                        .getExpandedNodes().get(0).getValue());
                  superNode.addEdgeWeight(
                        sccList.get(externalSCCId).getExpandedNodes().get(0)
                            .getValue(), outEdgeNode.getValue(),
                                outEdge.getValue());
                }
              }
            }
          }
        }
      }
    }
    return contracted;
  }

  public static <T> Graph<T> getTranspose(Graph<T> graph) {
    Graph<T> transpose = new Graph<>();
    T startVal;
    T endVal;
    for (Map.Entry<T, Node<T>> entry : graph.getNodes()) {
      startVal = entry.getKey();
      for (Map.Entry<Node<T>, Integer> edge : entry.getValue().getEdges()) {
        endVal = edge.getKey().getValue();
        transpose.addEdge(endVal, startVal);
      }
    }
    return transpose;
  }

  public static <T> List<SCC<T>> getPermutedSCCs(List<SCC<T>> sccList) {
    SCC<T> newScc;
    int totalNumberOfPermutations;
    List<SCC<T>> permutations = new ArrayList<>();
    for (SCC<T> scc : sccList) {
      for (Node<T> inNode : scc.getInNodes()) {
        for (Node<T> outNode : scc.getOutNodes()) {
          newScc = scc.clone();
          newScc.addInNode(inNode.getValue());
          newScc.addOutNode(outNode.getValue());
          permutations.add(newScc);
        }
      }
    }
    return permutations;
  }
}
