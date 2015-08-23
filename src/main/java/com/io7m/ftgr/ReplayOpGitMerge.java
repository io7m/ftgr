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
import java.math.BigInteger;

public final class ReplayOpGitMerge implements ReplayOperationType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayOpGitMerge.class);
  }

  private final GitExecutableType              exec;
  private final GitRepositorySpecificationType repos;
  private final String                         merge_to;
  private final String                         merge_from;
  private final BigInteger                     key;
  private final FossilCommit                   commit;

  public ReplayOpGitMerge(
    final GitExecutableType in_exec,
    final GitRepositorySpecificationType in_repos,
    final FossilCommit in_commit,
    final String in_merge_to,
    final String in_merge_from,
    final BigInteger key_id)
  {
    this.exec = NullCheck.notNull(in_exec);
    this.repos = NullCheck.notNull(in_repos);
    this.commit = NullCheck.notNull(in_commit);
    this.merge_to = NullCheck.notNull(in_merge_to);
    this.merge_from = NullCheck.notNull(in_merge_from);
    this.key = NullCheck.notNull(key_id);
  }

  @Override public void execute(
    final DryRun dry_run)
    throws ReplayException
  {
    try {
      ReplayOpGitMerge.LOG.info(
        "merging branch {} to {}", this.merge_from, this.merge_to);

      if (dry_run == DryRun.EXECUTE) {
        final GitIdent ident = this.repos.getUserNameMapping(
          this.commit.getCommitUser());

        this.exec.merge(
          this.repos,
          this.commit.getCommitTime(),
          ident,
          this.commit.getCommitComment(),
          this.merge_to,
          this.merge_from,
          this.key);
      }
    } catch (final IOException e) {
      throw new ReplayException(e);
    }
  }
}
