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

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;

public interface GitExecutableType
{
  void cloneRepository(
    File source,
    File target)
    throws IOException;

  void createRepository(GitRepositorySpecificationType repos)
    throws IOException;

  void createBranch(
    GitRepositorySpecificationType repos,
    String branch)
    throws IOException;

  void checkoutBranch(
    GitRepositorySpecificationType repos,
    String branch)
    throws IOException;

  void checkoutCommit(
    File repos,
    GitCommitName commit)
    throws IOException;

  GitCommitName createCommit(
    GitRepositorySpecificationType repos,
    Timestamp time,
    GitIdent user,
    String comment,
    String branch,
    BigInteger key_id)
    throws IOException;

  void createTag(
    GitRepositorySpecificationType repos,
    Timestamp time,
    GitIdent user,
    BigInteger key_id,
    String tag_name)
    throws IOException;

  GitCommitName createRootCommit(
    GitRepositorySpecificationType repos,
    Timestamp time,
    GitIdent user,
    String comment,
    String branch)
    throws IOException;

  void addAll(GitRepositorySpecificationType repos)
    throws IOException;

  void merge(
    GitRepositorySpecificationType repos,
    Timestamp time,
    GitIdent user,
    String comment,
    String merge_to,
    String merge_from,
    BigInteger key_id)
    throws IOException;
}
