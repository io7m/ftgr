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

public final class ReplayOpGitTag implements ReplayOperationType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayOpGitTag.class);
  }

  private final GitExecutableType              git;
  private final GitRepositorySpecificationType repos;
  private final long                           key;
  private final FossilCommit                   commit;
  private final String                         tag_name;

  public ReplayOpGitTag(
    final GitExecutableType in_git,
    final GitRepositorySpecificationType in_repos,
    final FossilCommit in_commit,
    final long key_id,
    final String in_tag_name)
  {
    this.git = NullCheck.notNull(in_git);
    this.repos = NullCheck.notNull(in_repos);
    this.commit = NullCheck.notNull(in_commit);
    this.key = key_id;
    this.tag_name = NullCheck.notNull(in_tag_name);
  }

  @Override public void execute(final DryRun dry_run)
    throws ReplayException
  {
    ReplayOpGitTag.LOG.info(
      "tag {} on branch {} ({})",
      this.commit.getCommitBlob(),
      this.commit.getBranch(),
      this.commit.getCommitComment());

    try {
      final GitIdent ident = this.repos.getUserNameMapping(
        this.commit.getCommitUser());
      this.git.createTag(
        this.repos,
        this.commit.getCommitTime(),
        ident,
        this.key,
        this.tag_name);

    } catch (final IOException e) {
      throw new ReplayException(e);
    }
  }
}
