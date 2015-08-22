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
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReplayPlanner implements ReplayPlannerType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayPlanner.class);
  }

  private final GitExecutableType                 git;
  private final GPGExecutableType                 gpg;
  private final GitRepositorySpecificationType    git_repos;
  private final FossilExecutableType              fossil;
  private final FossilRepositorySpecificationType fossil_repos;

  private ReplayPlanner(
    final GPGExecutableType in_gpg,
    final FossilExecutableType in_fossil_exec,
    final GitExecutableType in_git,
    final GitRepositorySpecificationType in_git_repos,
    final FossilRepositorySpecificationType in_fossil_repos)
  {
    this.gpg = NullCheck.notNull(in_gpg);
    this.fossil = NullCheck.notNull(in_fossil_exec);
    this.git = NullCheck.notNull(in_git);
    this.git_repos = NullCheck.notNull(in_git_repos);
    this.fossil_repos = NullCheck.notNull(in_fossil_repos);
  }

  public static ReplayPlannerType newPlanner(
    final GPGExecutableType in_gpg,
    final FossilExecutableType in_fossil_exec,
    final GitExecutableType in_git,
    final GitRepositorySpecificationType in_git_repos,
    final FossilRepositorySpecificationType in_fossil_repos)
  {
    return new ReplayPlanner(
      in_gpg, in_fossil_exec, in_git, in_git_repos, in_fossil_repos);
  }

  @Override public List<ReplayOperationType> plan(
    final FossilModelType m,
    final BidiMap<GitCommitName, FossilCommit> commit_log)
    throws ReplayException
  {
    ReplayPlanner.LOG.debug("planning replay for model");

    /**
     * Check that the HOME environment variable is set.
     */

    ReplayPlanner.LOG.debug("checking that $HOME is set...");
    if (System.getenv("HOME") == null) {
      throw new ReplayExceptionEnviromentNotSet("HOME");
    }

    final List<ReplayOperationType> p = new ArrayList<>(128);

    /**
     * Check that the user performing the replay has all the required private
     * keys.
     */

    final Map<Integer, Long> signers = m.getSigners();
    for (final Long k : new HashSet<>(signers.values())) {
      p.add(new ReplayOpCheckKey(this.gpg, k));
    }

    /**
     * Check that all Fossil commit names have been assigned Git names.
     */

    {
      final DirectedAcyclicGraph<FossilModelCommitNode, FossilModelCommitLink>
        g = m.getGraph();
      final Iterator<FossilModelCommitNode> iter = g.iterator();

      final Set<String> checked = new HashSet<>(128);
      while (iter.hasNext()) {
        final FossilModelCommitNode node = iter.next();
        final FossilCommit commit = node.getCommit();
        final String fossil_name = commit.getCommitUser();
        if (checked.contains(fossil_name) == false) {
          p.add(new ReplayOpCheckName(this.git_repos, fossil_name));
          checked.add(fossil_name);
        }
      }
    }

    /**
     * Create the repository and the root commit.
     */

    p.add(new ReplayOpGitCreateRepository(this.git, this.git_repos));
    final FossilModelCommitNode root_node = m.getRootNode();
    p.add(new ReplayOpGitCreateRootCommit(this.git, this.git_repos, root_node));

    /**
     * Open the Fossil repository into this directory.
     */

    p.add(
      new ReplayOpFossilOpen(
        this.fossil, this.fossil_repos, this.git_repos));

    /**
     * Create commits, branching and merging as necessary.
     */

    {
      final DirectedAcyclicGraph<FossilModelCommitNode, FossilModelCommitLink>
        g = m.getGraph();
      final Iterator<FossilModelCommitNode> iter = g.iterator();

      /**
       * The commits are sorted topologically, order them by date.
       */

      final List<FossilModelCommitNode> dated = new ArrayList<>(128);
      while (iter.hasNext()) {
        dated.add(iter.next());
      }

      Collections.sort(
        dated, new Comparator<FossilModelCommitNode>()
        {
          @Override public int compare(
            final FossilModelCommitNode o1,
            final FossilModelCommitNode o2)
          {
            final FossilCommit c1 = o1.getCommit();
            final FossilCommit c2 = o2.getCommit();
            return c1.getCommitTime().compareTo(c2.getCommitTime());
          }
        });

      for (int index = 0; index < dated.size(); ++index) {
        final FossilModelCommitNode node = NullCheck.notNull(dated.get(index));
        this.processCommit(
          p, signers, root_node, g, node, m.getTags(), commit_log);
      }
    }

    return p;
  }

  private void processCommit(
    final List<ReplayOperationType> plan,
    final Map<Integer, Long> signers,
    final FossilModelCommitNode root_node,
    final DirectedAcyclicGraph<FossilModelCommitNode, FossilModelCommitLink> g,
    final FossilModelCommitNode node,
    final BidiMap<FossilTagName, FossilCommitName> tags,
    final BidiMap<GitCommitName, FossilCommit> commit_log)
  {
    /**
     * If this node is the root node, ignore it.
     */

    if (node.equals(root_node)) {
      return;
    }

    final FossilCommit commit = node.getCommit();
    final Set<FossilModelCommitLink> parents = g.incomingEdgesOf(node);

    /**
     * Fossil creates otherwise empty commits that are responsible
     * for creating branches. If this is one of those branches, then
     * ignore the commit and create the branch in git.
     */

    final String current_branch = commit.getBranch();
    if (commit.isBranchNew()) {
      plan.add(
        new ReplayOpGitCreateBranch(
          this.git, this.git_repos, current_branch));
      return;
    }

    /**
     * All commits from this point on must be signed.
     */

    final Long k = NullCheck.notNull(
      signers.get(
        Integer.valueOf(
          commit.getId())));

    /**
     * If a commit has two parents, then it was the result of a merge
     * operation.
     */

    if (parents.size() == 2) {
      String parent_branch = null;
      final Iterator<FossilModelCommitLink> parent_iter = parents.iterator();

      while (parent_iter.hasNext()) {
        final FossilModelCommitLink parent_link = parent_iter.next();
        final FossilModelCommitNode parent = g.getEdgeSource(parent_link);
        final FossilCommit parent_commit = parent.getCommit();
        if (current_branch.equals(parent_commit.getBranch()) == false) {
          parent_branch = parent_commit.getBranch();
        }
      }

      plan.add(
        new ReplayOpGitMerge(
          this.git,
          this.git_repos,
          commit,
          current_branch,
          NullCheck.notNull(parent_branch),
          k));
      return;
    }

    /**
     * Otherwise, this is a regular commit.
     */

    plan.add(
      new ReplayOpGitCheckoutBranch(
        this.git, this.git_repos, commit.getBranch()));
    plan.add(
      new ReplayOpFossilCheckout(
        this.fossil, this.fossil_repos, this.git_repos, commit));
    plan.add(new ReplayOpGitAddAll(this.git, this.git_repos));
    plan.add(
      new ReplayOpGitCommit(
        this.git, this.git_repos, commit, k, commit_log));

    /**
     * Tag the commit, if necessary.
     */

    final FossilCommitName commit_name = commit.getCommitBlob();
    if (tags.containsValue(commit_name)) {
      final FossilTagName name = tags.getKey(commit_name);
      plan.add(new ReplayOpGitTag(this.git, this.git_repos, commit, k, name));
    }
  }
}
