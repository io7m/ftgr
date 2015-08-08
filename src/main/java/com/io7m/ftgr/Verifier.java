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

import com.io7m.jnull.NonNull;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import org.apache.commons.collections4.BidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Verifier implements VerifierType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Verifier.class);
  }

  private final File                                     git_repos;
  private final FossilRepositorySpecificationType        fossil_repos;
  private final GitExecutableType                        git;
  private final FossilExecutableType                     fossil;
  private final BidiMap<GitCommitName, FossilCommitName> commits;
  private final File                                     git_tmp;
  private final File                                     fossil_tmp;

  private Verifier(
    final BidiMap<GitCommitName, FossilCommitName> in_commits,
    final File in_git_repos,
    final FossilRepositorySpecificationType in_fossil_repos,
    final GitExecutableType in_git,
    final FossilExecutableType in_fossil,
    final File in_git_tmp,
    final File in_fossil_tmp)
  {
    this.commits = NullCheck.notNull(in_commits);
    this.git_repos = NullCheck.notNull(in_git_repos);
    this.fossil_repos = NullCheck.notNull(in_fossil_repos);
    this.git = NullCheck.notNull(in_git);
    this.fossil = NullCheck.notNull(in_fossil);
    this.git_tmp = NullCheck.notNull(in_git_tmp);
    this.fossil_tmp = NullCheck.notNull(in_fossil_tmp);
  }

  public static VerifierType newVerifier(
    final BidiMap<GitCommitName, FossilCommitName> in_commits,
    final File in_git_repos,
    final FossilRepositorySpecificationType in_fossil_repos,
    final GitExecutableType in_git,
    final FossilExecutableType in_fossil,
    final File in_git_tmp,
    final File in_fossil_tmp)
  {
    return new Verifier(
      in_commits,
      in_git_repos,
      in_fossil_repos,
      in_git,
      in_fossil,
      in_git_tmp,
      in_fossil_tmp);
  }

  @Override public List<VerifierResult> verify()
    throws IOException
  {
    final List<VerifierResult> results = new ArrayList<>();

    if (this.git_tmp.mkdirs() == false) {
      if (this.git_tmp.isDirectory() == false) {
        throw new IOException(
          String.format(
            "Not a directory: %s", this.git_tmp));
      }
    }
    if (this.fossil_tmp.mkdirs() == false) {
      if (this.fossil_tmp.isDirectory() == false) {
        throw new IOException(
          String.format(
            "Not a directory: %s", this.fossil_tmp));
      }
    }

    Verifier.LOG.debug("cloning git repository");
    this.git.cloneRepository(this.git_repos, this.git_tmp);
    Verifier.LOG.debug("opening fossil repository");
    this.fossil.open(this.fossil_repos, this.fossil_tmp);

    for (final GitCommitName git_commit : this.commits.keySet()) {
      Verifier.LOG.debug("verifying git commit {}", git_commit);
      results.add(this.verifyGitCommit(git_commit));
    }

    return results;
  }

  private VerifierResult verifyGitCommit(final GitCommitName git_commit)
    throws IOException
  {
    final FossilCommitName fossil_commit =
      NullCheck.notNull(this.commits.get(git_commit));

    this.git.checkoutCommit(this.git_tmp, git_commit);

    final Map<Path, String> git_commit_content = new HashMap<>();
    this.verifyGetTreeSHA256Sums(git_commit_content, this.git_tmp.toPath());

    this.fossil.checkOut(this.fossil_repos, fossil_commit, this.fossil_tmp);
    final Map<Path, String> fossil_commit_content = new HashMap<>();
    this.verifyGetTreeSHA256Sums(
      fossil_commit_content, this.fossil_tmp.toPath());

    final int fossil_size = fossil_commit_content.size();
    final int git_size = git_commit_content.size();
    Verifier.LOG.debug("fossil commit files: {} files", fossil_size);
    Verifier.LOG.debug("git commit files: {} files", git_size);

    if (fossil_size != git_size) {
      return this.missingOrExtraFiles(
        git_commit, fossil_commit, git_commit_content, fossil_commit_content);
    }

    return this.checkAllSums(
      git_commit, fossil_commit, git_commit_content, fossil_commit_content);
  }

  private VerifierResult checkAllSums(
    final GitCommitName git_commit,
    final FossilCommitName fossil_commit,
    final Map<Path, String> git_commit_content,
    final Map<Path, String> fossil_commit_content)
  {
    boolean ok = true;
    StringBuilder sb = new StringBuilder();

    for (final Path p : git_commit_content.keySet()) {
      final String git_sum = git_commit_content.get(p);
      final String fsl_sum = fossil_commit_content.get(p);

      if (git_sum.equals(fsl_sum) == false) {
        ok = false;
        sb.append("Bad checksum: ");
        sb.append(p);
        sb.append(": ");
        sb.append(git_sum);
        sb.append(" expected ");
        sb.append(fsl_sum);
        sb.append("\n");
      }
    }

    if (ok) {
      sb.setLength(0);
      sb.append("OK");
    }

    return new VerifierResult(fossil_commit, git_commit, sb.toString(), ok);
  }

  @NonNull private VerifierResult missingOrExtraFiles(
    final GitCommitName git_commit,
    final FossilCommitName fossil_commit,
    final Map<Path, String> git_commit_content,
    final Map<Path, String> fossil_commit_content)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Missing or extraneous files.\n");

    {
      final Map<Path, String> extras = new HashMap<>(git_commit_content);
      extras.keySet().removeAll(fossil_commit_content.keySet());

      for (final Path e : extras.keySet()) {
        sb.append("Git commit has extra file: ");
        sb.append(e);
        sb.append("\n");
      }
    }

    {
      final Map<Path, String> extras = new HashMap<>(fossil_commit_content);
      extras.keySet().removeAll(git_commit_content.keySet());

      for (final Path e : extras.keySet()) {
        sb.append("Git commit is missing file: ");
        sb.append(e);
        sb.append("\n");
      }
    }

    return new VerifierResult(
      fossil_commit, git_commit, sb.toString(), false);
  }

  private void verifyGetTreeSHA256Sums(
    final Map<Path, String> content,
    final Path base)
    throws IOException
  {
    Files.walkFileTree(
      base, new FileVisitor<Path>()
      {
        @Override public FileVisitResult preVisitDirectory(
          final Path dir,
          final BasicFileAttributes attrs)
          throws IOException
        {
          final Path relative = NullCheck.notNull(base.relativize(dir));
          Verifier.LOG.trace("dir: {}", relative);

          if (".git".equals(relative.toString())) {
            return FileVisitResult.SKIP_SUBTREE;
          }

          return FileVisitResult.CONTINUE;
        }

        @Override public FileVisitResult visitFile(
          final Path file,
          final BasicFileAttributes attrs)
          throws IOException
        {
          final Path relative = NullCheck.notNull(base.relativize(file));
          Verifier.LOG.trace("file: {}", relative);

          if (".gitignore".equals(relative.getFileName().toString())) {
            return FileVisitResult.CONTINUE;
          }
          if (".fslckout".equals(relative.getFileName().toString())) {
            return FileVisitResult.CONTINUE;
          }

          content.put(relative, Verifier.this.sha256(file));
          return FileVisitResult.CONTINUE;
        }

        @Override public FileVisitResult visitFileFailed(
          final Path file,
          final IOException exc)
          throws IOException
        {
          return FileVisitResult.CONTINUE;
        }

        @Override public FileVisitResult postVisitDirectory(
          final Path dir,
          final IOException exc)
          throws IOException
        {
          return FileVisitResult.CONTINUE;
        }
      });
  }

  private String sha256(final Path file)
    throws IOException
  {
    NullCheck.notNull(file);

    try {
      final MessageDigest md = MessageDigest.getInstance("SHA-256");
      final byte[] buffer = new byte[8192];
      try (final InputStream fs = Files.newInputStream(
        file, StandardOpenOption.READ)) {

        while (true) {
          int r = fs.read(buffer);
          if (r == -1) {
            break;
          }
          md.update(buffer, 0, r);
        }

        final StringBuilder sb = new StringBuilder();
        final byte[] dg = md.digest();
        for (byte b : dg) {
          sb.append(String.format("%02x", b));
        }
        final String s = sb.toString();
        Verifier.LOG.debug("sha256: {} {}", file, s);
        return s;
      }
    } catch (NoSuchAlgorithmException e) {
      throw new UnreachableCodeException(e);
    }
  }
}
