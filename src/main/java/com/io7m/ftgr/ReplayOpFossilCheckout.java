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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ReplayOpFossilCheckout implements ReplayOperationType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayOpFossilCheckout.class);
  }

  private final FossilCommit                      commit;
  private final FossilExecutableType              fossil_exec;
  private final FossilRepositorySpecificationType fossil_repos;
  private final GitRepositorySpecificationType    git_repos;

  public ReplayOpFossilCheckout(
    final FossilExecutableType in_fossil_exec,
    final FossilRepositorySpecificationType in_fossil_repos,
    final GitRepositorySpecificationType in_git_repos,
    final FossilCommit in_commit)
  {
    this.fossil_exec = NullCheck.notNull(in_fossil_exec);
    this.fossil_repos = NullCheck.notNull(in_fossil_repos);
    this.git_repos = NullCheck.notNull(in_git_repos);
    this.commit = NullCheck.notNull(in_commit);
  }

  @Override public <A, E extends Exception> A matchOperation(
    final ReplayOperationMatcherType<A, E> m)
    throws E
  {
    return m.onFossilCheckout(this);
  }

  @Override public void execute(
    final DryRun dry_run)
    throws ReplayException
  {
    try {
      ReplayOpFossilCheckout.LOG.info(
        "checking out revision {} from fossil", commit.getCommitBlob());

      if (dry_run == DryRun.EXECUTE) {
        this.fossil_exec.checkOut(
          this.fossil_repos, this.commit, this.git_repos.getDirectory());
      }
    } catch (IOException e) {
      throw new ReplayException(e);
    }
  }
}
