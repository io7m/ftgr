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
import com.io7m.jnull.NullCheck;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class FossilExecutable implements FossilExecutableType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FossilExecutable.class);
  }

  private final File exec;

  private FossilExecutable(final File in_exec)
  {
    this.exec = NullCheck.notNull(in_exec);
  }

  public static FossilExecutableType newExecutable(final File exec)
  {
    return new FossilExecutable(exec);
  }

  @Override public OptionType<String> getArtifactForName(
    final FossilRepositorySpecificationType repos,
    final String name)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(name);

    final List<String> args = new ArrayList<>(5);
    args.add(this.exec.toString());
    args.add("whatis");
    args.add(name);
    args.add("-R");
    args.add(repos.getRepositoryFile().toString());
    FossilExecutable.LOG.debug("execute: {}", args);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.redirectErrorStream(true);
    final Process p = pb.start();

    final List<String> out_lines = new ArrayList<>(8);
    ProcessUtilities.executeLogged(
      FossilExecutable.LOG, p, out_lines);

    final Iterator<String> iter = out_lines.iterator();
    while (iter.hasNext()) {
      final String line = iter.next();
      if (line.startsWith("artifact:")) {
        final String[] parts = line.split(":");
        return Option.some(parts[1].trim());
      }
    }

    return Option.none();
  }

  @Override public List<String> getNonPropagatingTags(
    final FossilRepositorySpecificationType repos)
    throws IOException
  {
    NullCheck.notNull(repos);

    final List<String> args = new ArrayList<>(5);
    args.add(this.exec.toString());
    args.add("tag");
    args.add("list");
    args.add("-R");
    args.add(repos.getRepositoryFile().toString());
    FossilExecutable.LOG.debug("execute: {}", args);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.redirectErrorStream(true);
    final Process p = pb.start();

    final List<String> out_lines = new ArrayList<>(64);
    ProcessUtilities.executeLogged(
      FossilExecutable.LOG, p, out_lines);

    final Iterator<String> iter = out_lines.iterator();
    while (iter.hasNext()) {
      final String name = iter.next();
      if ("trunk".equals(name)) {
        iter.remove();
      }
    }

    return out_lines;
  }

  @Override public ByteBuffer getBlobForUUID(
    final FossilRepositorySpecificationType repos,
    final FossilCommitName uuid)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(uuid);

    final List<String> args = new ArrayList<>(5);
    args.add(this.exec.toString());
    args.add("artifact");
    args.add("-R");
    args.add(repos.getRepositoryFile().toString());
    args.add(uuid.toString());
    FossilExecutable.LOG.debug("execute: {}", args);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.redirectErrorStream(true);
    final Process p = pb.start();

    final List<String> out_lines = new ArrayList<>(32);
    ProcessUtilities.executeLogged(
      FossilExecutable.LOG, p, out_lines);

    try (final ByteArrayOutputStream buffer_out = new ByteArrayOutputStream()) {
      IOUtils.writeLines(out_lines, "\n", buffer_out);
      return ByteBuffer.wrap(buffer_out.toByteArray());
    }
  }

  @Override public void open(
    final FossilRepositorySpecificationType repos,
    final File directory)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(directory);

    final List<String> args = new ArrayList<>(5);
    args.add(this.exec.toString());
    args.add("open");
    args.add("--empty");
    args.add(repos.getRepositoryFile().toString());
    FossilExecutable.LOG.debug("execute: {}", args);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    env.put("HOME", NullCheck.notNull(System.getenv("HOME")));

    pb.command(args);
    pb.directory(directory);
    pb.redirectErrorStream(true);

    final List<String> out_lines = new ArrayList<>(32);
    ProcessUtilities.executeLogged(
      FossilExecutable.LOG, pb.start(), out_lines);
  }

  @Override public void checkOut(
    final FossilRepositorySpecificationType repos,
    final FossilCommit commit,
    final File directory)
    throws IOException
  {
    NullCheck.notNull(repos);
    NullCheck.notNull(commit);
    NullCheck.notNull(directory);

    /**
     * Check out files...
     */

    {
      final List<String> args = new ArrayList<>(4);
      args.add(this.exec.toString());
      args.add("checkout");
      args.add("--force");
      args.add(commit.getCommitBlob().toString());
      FossilExecutable.LOG.debug("execute: {}", args);

      final ProcessBuilder pb = new ProcessBuilder();
      final Map<String, String> env = pb.environment();
      env.clear();
      env.put("HOME", NullCheck.notNull(System.getenv("HOME")));

      pb.command(args);
      pb.directory(directory);
      pb.redirectErrorStream(true);

      final List<String> out_lines = new ArrayList<>(32);
      ProcessUtilities.executeLogged(
        FossilExecutable.LOG, pb.start(), out_lines);
    }

    /**
     * ... and clean up anything not relevant to this commit.
     */

    {
      final List<String> args = new ArrayList<>(3);
      args.add(this.exec.toString());
      args.add("clean");
      args.add("--force");
      args.add("--verbose");
      FossilExecutable.LOG.debug("execute: {} in {}", args, directory);

      final ProcessBuilder pb = new ProcessBuilder();
      final Map<String, String> env = pb.environment();
      env.clear();
      env.put("HOME", NullCheck.notNull(System.getenv("HOME")));

      pb.command(args);
      pb.directory(directory);
      pb.redirectErrorStream(true);

      final List<String> out_lines = new ArrayList<>(32);
      ProcessUtilities.executeLogged(
        FossilExecutable.LOG, pb.start(), out_lines);
    }
  }
}
