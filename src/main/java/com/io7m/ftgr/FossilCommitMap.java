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
import com.io7m.junreachable.UnreachableCodeException;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class FossilCommitMap
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FossilCommitMap.class);
  }

  private FossilCommitMap()
  {
    throw new UnreachableCodeException();
  }

  public static BidiMap<GitCommitName, FossilCommitName> fromStream(
    final InputStream s)
    throws IOException
  {
    NullCheck.notNull(s);

    final DualHashBidiMap<GitCommitName, FossilCommitName> rm =
      new DualHashBidiMap<>();

    int line_number = 1;
    try (final BufferedReader r = new BufferedReader(
      new InputStreamReader(s))) {

      while (true) {
        final String line = r.readLine();
        if (line == null) {
          break;
        }

        FossilCommitMap.parseLine(rm, line_number, line);
        line_number = line_number + 1;
      }

    }

    return rm;
  }

  private static void parseLine(
    final DualHashBidiMap<GitCommitName, FossilCommitName> rm,
    final int line_number,
    final String line)
    throws IOException
  {
    final String[] parts = NullCheck.notNull(line.split("\\|"));
    final String git = NullCheck.notNull(parts[0]);
    final String fsl = NullCheck.notNull(parts[1]);

    FossilCommitMap.LOG.debug("{}: {}", line_number, line);

    final String git_actual;
    if (git.startsWith("git:")) {
      git_actual = NullCheck.notNull(git.substring(4));
    } else {
      throw new IOException(
        String.format(
          "Parse error: %d: Commit must start with 'git:'", line_number));
    }

    final String fsl_actual;
    if (fsl.startsWith("fossil:")) {
      fsl_actual = NullCheck.notNull(fsl.substring(7));
    } else {
      throw new IOException(
        String.format(
          "Parse error: %d: Commit must start with 'fossil:'",
          line_number));
    }

    FossilCommitMap.LOG.debug(
      "{}: (git {}) (fossil {})", line_number, git_actual, fsl_actual);

    rm.put(new GitCommitName(git_actual), new FossilCommitName(fsl_actual));
  }
}
