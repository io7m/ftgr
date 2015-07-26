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

import com.io7m.junreachable.UnreachableCodeException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public final class ProcessUtilities
{
  private ProcessUtilities()
  {
    throw new UnreachableCodeException();
  }

  static void executeLogged(
    final Logger log,
    final Process p,
    final List<String> out_lines)
    throws IOException
  {
    try (final InputStream p_stdout = p.getInputStream()) {
      try (final BufferedReader r_stdout = new BufferedReader(
        new InputStreamReader(
          p_stdout))) {

        while (true) {
          final String out_line = r_stdout.readLine();
          if (out_line == null) {
            break;
          }

          out_lines.add(out_line);
          log.debug("execute: {}", out_line);
        }
      }

      try {
        p.waitFor();
      } catch (InterruptedException e) {
        log.error(
          "interrupted whilst waiting for process: ", e);
      }

      if (p.exitValue() > 0) {
        try (final ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
          IOUtils.writeLines(out_lines, "\n", bao);
          throw new IOException(
            new String(bao.toByteArray(), "UTF-8"));
        }
      }
    }
  }
}

