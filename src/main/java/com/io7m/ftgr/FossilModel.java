/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.ftgr;

import com.io7m.jnull.NullCheck;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jgrapht.EdgeFactory;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class FossilModel implements FossilModelType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FossilModel.class);
  }

  private final DirectedAcyclicGraph<FossilModelCommitNode,
    FossilModelCommitLink>
                                                         graph;
  private final Map<Integer, FossilModelCommitNode>      nodes;
  private final Set<String>                              branches;
  private final Map<Integer, BigInteger>                 signers;
  private final FossilModelCommitNode                    root;
  private final BidiMap<FossilTagName, FossilCommitName> tags;

  private FossilModel(
    final DirectedAcyclicGraph<FossilModelCommitNode, FossilModelCommitLink>
      in_graph,
    final Map<Integer, FossilModelCommitNode> in_nodes,
    final Set<String> in_branches,
    final Map<Integer, BigInteger> in_signers,
    final FossilModelCommitNode in_root,
    final BidiMap<FossilTagName, FossilCommitName> in_tags)
  {
    this.graph = NullCheck.notNull(in_graph);
    this.nodes = NullCheck.notNull(in_nodes);
    this.branches = NullCheck.notNull(in_branches);
    this.signers = NullCheck.notNull(in_signers);
    this.root = NullCheck.notNull(in_root);
    this.tags = NullCheck.notNull(in_tags);
  }

  public static FossilModelBuilderType newBuilder()
  {
    return new Builder();
  }

  @Override public Set<String> getBranches()
  {
    return this.branches;
  }

  @Override public Map<Integer, BigInteger> getSigners()
  {
    return this.signers;
  }

  @Override
  public DirectedAcyclicGraph<FossilModelCommitNode, FossilModelCommitLink>
  getGraph()
  {
    return this.graph;
  }

  @Override public Map<Integer, FossilModelCommitNode> getNodes()
  {
    return this.nodes;
  }

  @Override public FossilModelCommitNode getRootNode()
  {
    return this.root;
  }

  @Override public BidiMap<FossilTagName, FossilCommitName> getTags()
  {
    return this.tags;
  }

  private static final class Builder implements FossilModelBuilderType
  {
    private final EdgeFactory<FossilModelCommitNode, FossilModelCommitLink>
                                                           edges;
    private final DirectedAcyclicGraph<FossilModelCommitNode,
      FossilModelCommitLink>
                                                           graph;
    private final Map<Integer, FossilModelCommitNode>      nodes;
    private final Set<String>                              branches;
    private final Map<Integer, BigInteger>                 signers;
    private final BidiMap<FossilTagName, FossilCommitName> tags;

    Builder()
    {
      this.edges =
        new EdgeFactory<FossilModelCommitNode, FossilModelCommitLink>()
        {
          @Override public FossilModelCommitLink createEdge(
            final FossilModelCommitNode source,
            final FossilModelCommitNode target)
          {
            return new FossilModelCommitLink(source, target);
          }
        };

      this.nodes = new HashMap<>(128);
      this.branches = new HashSet<>(32);
      this.signers = new HashMap<>(8);
      this.tags = new DualHashBidiMap<>();
      this.graph = new DirectedAcyclicGraph<>(this.edges);
    }

    @Override public void addCommit(final FossilCommit c)
      throws FossilException
    {
      NullCheck.notNull(c);
      final FossilModelCommitNode n = new FossilModelCommitNode(c, c.getId());
      this.graph.addVertex(n);
      this.nodes.put(Integer.valueOf(c.getId()), n);
      this.branches.add(c.getBranch());
    }

    @Override public void setSigningKey(
      final int commit,
      final BigInteger key_id)
    {
      this.signers.put(Integer.valueOf(commit), key_id);
    }

    @Override public FossilModelType build()
      throws FossilGraphException
    {
      final Set<FossilModelCommitNode> roots = new HashSet<>(2);

      final Iterator<FossilModelCommitNode> iter = NullCheck.notNull(
        this.graph.iterator());
      while (iter.hasNext()) {
        final FossilModelCommitNode node = NullCheck.notNull(iter.next());
        if (this.graph.incomingEdgesOf(node).isEmpty()) {
          roots.add(node);
        }
      }

      if (roots.size() != 1) {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("Graph has multiple root nodes.\n");
        for (final FossilModelCommitNode r : roots) {
          sb.append("  Node: ");
          sb.append(r);
          sb.append("\n");
        }
        throw new FossilGraphException(sb.toString());
      }

      final FossilModelCommitNode root = roots.iterator().next();
      FossilModel.LOG.debug("root node is: {}", root);

      return new FossilModel(
        this.graph, this.nodes, this.branches, this.signers, root, this.tags);
    }

    @Override public void addParentLink(final FossilParentLink p)
      throws FossilGraphException
    {
      NullCheck.notNull(p);

      try {
        final FossilModelCommitNode parent =
          this.nodes.get(Integer.valueOf(p.getParent()));
        final FossilModelCommitNode child =
          this.nodes.get(Integer.valueOf(p.getChild()));

        this.graph.addDagEdge(child, parent);
      } catch (final DirectedAcyclicGraph.CycleFoundException e) {
        throw new FossilGraphException(e);
      }
    }

    @Override public void addTag(
      final FossilTagName tag,
      final FossilCommitName commit)
    {
      NullCheck.notNull(tag);
      NullCheck.notNull(commit);
      this.tags.put(tag, commit);
    }
  }
}
