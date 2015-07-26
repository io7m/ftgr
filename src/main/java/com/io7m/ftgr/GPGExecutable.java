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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GPGExecutable implements GPGExecutableType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(GPGExecutable.class);
  }

  private final File exec;

  private GPGExecutable(final File in_exec)
  {
    this.exec = NullCheck.notNull(in_exec);
  }

  public static GPGExecutableType newExecutable(final File exec)
  {
    return new GPGExecutable(exec);
  }

  @Override public boolean hasSecretKey(final long id)
    throws IOException
  {
    final String key_id = String.format("0x%016x", Long.valueOf(id));

    final List<String> args = new ArrayList<>();
    args.add(this.exec.toString());
    args.add("--list-secret-key");
    args.add(key_id);
    GPGExecutable.LOG.debug("execute: {}", args);

    final ProcessBuilder pb = new ProcessBuilder();
    final Map<String, String> env = pb.environment();
    env.clear();
    pb.command(args);
    pb.redirectErrorStream(true);
    pb.redirectInput(ProcessBuilder.Redirect.from(new File("/dev/null")));

    final Process p = pb.start();

    final List<String> out_lines = new ArrayList<>();
    ProcessUtilities.executeLogged(
      GPGExecutable.LOG, pb.start(), out_lines);
    return true;
  }
}
