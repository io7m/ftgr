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

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jnull.NullCheck;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GitExecutable implements GitExecutableType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(GitExecutable.class);
  }

  private final File exec;
  private final File faketime_exec;

  private GitExecutable(
    final File in_exec,
    final File in_faketime_exec)
  {
    this.exec = NullCheck.notNull(in_exec);
    this.faketime_exec = NullCheck.notNull(in_faketime_exec);
  }

  public static GitExecutableType newExecutable(
    final File exec,
    final File in_faketime_exec)
  {
    return new GitExecutable(exec, in_faketime_exec);
  }

  @Override
  public void createRepository(final GitRepositorySpecificationType repos)
    throws IOException
  {
    NullCheck.notNull(repos);

    final File workdir = repos.getDirectory().getCanonicalFile();
    if (workdir.mkdirs() == false) {
      if (workdir.isDirectory() == false) {
        throw new IOException(String.format("Not a directory: %s", workdir));
      }
    }

    final List<String> args = new ArrayList<>(2);
    args.add(this.exec.toString());
    args.add("init");
    GitExecutable.LOG.debug("execute {} in {}", args, workdir);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.directory(workdir);
    pb.redirectErrorStream(true);

    final List<String> out_lines = new ArrayList<>();
    ProcessUtilities.executeLogged(
      GitExecutable.LOG, pb.start(), out_lines);
  }

  @Override public void createBranch(
    final GitRepositorySpecificationType repos,
    final String branch)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(branch);

    final File workdir = repos.getDirectory().getCanonicalFile();
    final List<String> args = new ArrayList<>(4);
    args.add(this.exec.toString());
    args.add("checkout");
    args.add("-b");
    args.add(branch);
    GitExecutable.LOG.debug("execute {} in {}", args, workdir);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.directory(workdir);
    pb.redirectErrorStream(true);

    final List<String> out_lines = new ArrayList<>();
    ProcessUtilities.executeLogged(
      GitExecutable.LOG, pb.start(), out_lines);
  }

  @Override public void checkoutBranch(
    final GitRepositorySpecificationType repos,
    final String branch)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(branch);

    final File workdir = repos.getDirectory().getCanonicalFile();

    /**
     * Switch to the correct branch.
     */

    final List<String> args = new ArrayList<>(3);
    args.add(this.exec.toString());
    args.add("checkout");
    args.add(branch);
    GitExecutable.LOG.debug("execute {} in {}", args, workdir);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.directory(workdir);
    pb.redirectErrorStream(true);

    final List<String> out_lines = new ArrayList<>();
    ProcessUtilities.executeLogged(
      GitExecutable.LOG, pb.start(), out_lines);
  }

  @Override public String createCommit(
    final GitRepositorySpecificationType repos,
    final Timestamp time,
    final GitIdent user,
    final String comment,
    final String branch,
    final long key_id)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(time);
    NullCheck.notNull(user);
    NullCheck.notNull(comment);
    NullCheck.notNull(branch);

    return this.commit(
      repos, time, user, comment, branch, Option.some(Long.valueOf(key_id)));
  }

  @Override public String createRootCommit(
    final GitRepositorySpecificationType repos,
    final Timestamp time,
    final GitIdent user,
    final String comment,
    final String branch)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(time);
    NullCheck.notNull(user);
    NullCheck.notNull(comment);
    NullCheck.notNull(branch);

    final File workdir = repos.getDirectory().getCanonicalFile();

    /**
     * Create the initial branch by checking it out.
     */

    {
      final List<String> args = new ArrayList<>(4);
      args.add(this.exec.toString());
      args.add("checkout");
      args.add("-b");
      args.add(branch);
      GitExecutable.LOG.debug("execute {} in {}", args, workdir);

      final ProcessBuilder pb = new ProcessBuilder();
      final Map<String, String> env = pb.environment();
      env.clear();
      pb.command(args);
      pb.directory(workdir);
      pb.redirectErrorStream(true);

      final List<String> out_lines = new ArrayList<>();
      ProcessUtilities.executeLogged(
        GitExecutable.LOG, pb.start(), out_lines);
    }

    /**
     * Write a .gitignore file for the initial commit. Use it to hide
     * any Fossil files.
     */

    {
      try (final FileOutputStream out = new FileOutputStream(
        new File(
          workdir, ".gitignore"))) {
        final List<String> lines = new ArrayList<>();
        lines.add(".fslckout");
        IOUtils.writeLines(lines, "\n", out);
        out.flush();
      }
    }

    /**
     * Add the .gitignore file to the index.
     */

    {
      final List<String> args = new ArrayList<>(4);
      args.add(this.exec.toString());
      args.add("add");
      args.add("-v");
      args.add(".gitignore");
      GitExecutable.LOG.debug("execute {} in {}", args, workdir);

      final ProcessBuilder pb = new ProcessBuilder();
      final Map<String, String> env = pb.environment();
      env.clear();
      pb.command(args);
      pb.directory(workdir);
      pb.redirectErrorStream(true);

      final List<String> out_lines = new ArrayList<>();
      ProcessUtilities.executeLogged(
        GitExecutable.LOG, pb.start(), out_lines);
    }

    /**
     * Make the initial commit.
     */

    final OptionType<Long> no_key = Option.none();
    return this.commit(repos, time, user, comment, branch, no_key);
  }

  @Override public void addAll(final GitRepositorySpecificationType repos)
    throws IOException
  {
    NullCheck.notNull(repos);

    final File workdir = repos.getDirectory().getCanonicalFile();

    final List<String> args = new ArrayList<>(4);
    args.add(this.exec.toString());
    args.add("add");
    args.add("-v");
    args.add(".");
    GitExecutable.LOG.debug("execute {} in {}", args, workdir);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.directory(workdir);
    pb.redirectErrorStream(true);

    final List<String> out_lines = new ArrayList<>();
    ProcessUtilities.executeLogged(
      GitExecutable.LOG, pb.start(), out_lines);
  }

  @Override public void merge(
    final GitRepositorySpecificationType repos,
    final Timestamp time,
    final GitIdent user,
    final String comment,
    final String merge_to,
    final String merge_from,
    final long key_id)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(merge_from);
    NullCheck.notNull(merge_to);

    final File workdir = repos.getDirectory().getCanonicalFile();

    /**
     * Switch to the branch that will receive commits from the
     * other branch.
     */

    {
      final List<String> args = new ArrayList<>(4);
      args.add(this.exec.toString());
      args.add("checkout");
      args.add(merge_to);
      GitExecutable.LOG.debug("execute {} in {}", args, workdir);

      final ProcessBuilder pb = new ProcessBuilder();
      final Map<String, String> env = pb.environment();
      env.clear();
      pb.command(args);
      pb.directory(workdir);
      pb.redirectErrorStream(true);

      final List<String> out_lines = new ArrayList<>();
      ProcessUtilities.executeLogged(
        GitExecutable.LOG, pb.start(), out_lines);
    }

    /**
     * Perform the merge.
     */

    {
      final List<String> args = new ArrayList<>(8);
      args.add(this.faketime_exec.toString());
      args.add(time.toString());
      args.add(this.exec.toString());
      args.add("merge");
      args.add("--no-ff");
      args.add("--gpg-sign=" + Long.toHexString(key_id));
      args.add(String.format("-m Merge %s", merge_from));
      args.add(merge_from);
      GitExecutable.LOG.debug("execute {} in {}", args, workdir);

      final ProcessBuilder pb = new ProcessBuilder();
      final Map<String, String> env = pb.environment();
      env.put("GIT_AUTHOR_DATE", time.toString());
      env.put("GIT_AUTHOR_NAME", user.getName());
      env.put("GIT_AUTHOR_EMAIL", user.getEmail());
      env.put("GIT_COMMITTER_DATE", time.toString());
      env.put("GIT_COMMITTER_NAME", user.getName());
      env.put("GIT_COMMITTER_EMAIL", user.getEmail());

      pb.command(args);
      pb.directory(workdir);
      pb.redirectErrorStream(true);

      final List<String> out_lines = new ArrayList<>();
      ProcessUtilities.executeLogged(
        GitExecutable.LOG, pb.start(), out_lines);
    }
  }

  private String commit(
    final GitRepositorySpecificationType repos,
    final Timestamp time,
    final GitIdent user,
    final String comment,
    final String branch,
    final OptionType<Long> key)
    throws IOException
  {
    final File workdir = repos.getDirectory().getCanonicalFile();

    final List<String> args = new ArrayList<>(10);
    args.add(this.faketime_exec.toString());
    args.add(time.toString());
    args.add(this.exec.toString());
    args.add("commit");
    args.add("--allow-empty");
    args.add("-v");
    args.add("-F");
    args.add("-");
    args.add(
      String.format(
        "--author=%s <%s>", user.getName(), user.getEmail()));

    if (key.isSome()) {
      final Some<Long> some = (Some<Long>) key;
      args.add("--gpg-sign=" + Long.toHexString(some.get()));
    } else {
      args.add("--no-gpg-sign");
    }

    GitExecutable.LOG.debug("execute {} in {}", args, workdir);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.put("GIT_AUTHOR_DATE", time.toString());
    env.put("GIT_AUTHOR_NAME", user.getName());
    env.put("GIT_AUTHOR_EMAIL", user.getEmail());
    env.put("GIT_COMMITTER_DATE", time.toString());
    env.put("GIT_COMMITTER_NAME", user.getName());
    env.put("GIT_COMMITTER_EMAIL", user.getEmail());

    pb.command(args);
    pb.directory(workdir);
    pb.redirectErrorStream(true);
    final Process p = pb.start();

    try (final OutputStream stdin = p.getOutputStream()) {
      IOUtils.write(comment, stdin);
      stdin.flush();
      stdin.close();
    }

    final List<String> out_lines = new ArrayList<>();
    ProcessUtilities.executeLogged(
      GitExecutable.LOG, p, out_lines);

    final File git = new File(workdir, ".git");
    final File git_refs = new File(git, "refs");
    final File git_heads = new File(git_refs, "heads");
    final File head = new File(git_heads, branch);

    GitExecutable.LOG.debug("reading {}", head);

    try (final InputStream is = new FileInputStream(head)) {
      final List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
      if (lines.isEmpty()) {
        throw new IOException(
          String.format(
            "File %s turned out to be empty!", head));
      }
      return NullCheck.notNull(lines.get(0).trim());
    }
  }
}
