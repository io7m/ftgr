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
import com.io7m.junreachable.UnreachableCodeException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

final class FossilManifest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FossilManifest.class);
  }

  private FossilManifest()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Return the key id for the given manifest.
   *
   * @param uuid The UUID of the blob that contained the manifest
   * @param b    The blob
   *
   * @return The signing key, if any
   *
   * @throws IOException On I/O errors
   */

  public static OptionType<BigInteger> getSignatureKey(
    final FossilCommitName uuid,
    final ByteBuffer b)
    throws IOException
  {
    NullCheck.notNull(uuid);
    NullCheck.notNull(b);

    FossilManifest.LOG.debug("blob {}: trying to fetch signature", uuid);

    try (final ByteArrayInputStream bai = new ByteArrayInputStream(b.array())) {
      final List<String> lines = IOUtils.readLines(bai, StandardCharsets.UTF_8);
      if (lines.size() > 0) {
        final String line = lines.get(0);
        if (FossilManifest.lineIsSignedMessage(line)) {
          return FossilManifest.tryLines(uuid, lines);
        }
      }
    }

    return Option.none();
  }

  private static OptionType<BigInteger> tryLines(
    final FossilCommitName uuid,
    final List<String> lines)
    throws IOException
  {
    while (true) {
      if (lines.isEmpty()) {
        return Option.none();
      }
      final String line = lines.get(0);
      if (FossilManifest.lineIsSignature(line)) {
        return FossilManifest.trySignatureLines(uuid, lines);
      }
      lines.remove(0);
    }
  }

  private static OptionType<BigInteger> trySignatureLines(
    final FossilCommitName uuid,
    final List<String> lines)
    throws IOException
  {
    try (final ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
      IOUtils.writeLines(lines, "\n", bao);

      try (final ByteArrayInputStream bai = new ByteArrayInputStream(
        bao.toByteArray())) {
        return FossilManifest.trySignature(uuid, bai);
      }
    }
  }

  private static OptionType<BigInteger> trySignature(
    final FossilCommitName uuid,
    final ByteArrayInputStream bai)
    throws IOException
  {
    try (final ArmoredInputStream ais = new ArmoredInputStream(bai)) {
      final JcaPGPObjectFactory fact = new JcaPGPObjectFactory(ais);
      final Iterator<Object> iter =
        NullCheck.notNull((Iterator<Object>) fact.iterator());
      if (iter.hasNext()) {
        final Object sig = NullCheck.notNull(iter.next());
        if (sig instanceof PGPSignatureList) {
          FossilManifest.LOG.debug("blob {}: received a PGP signature list");
          final PGPSignatureList sig_list =
            NullCheck.notNull((PGPSignatureList) sig);
          if (sig_list.size() > 0) {
            final PGPSignature ssig = NullCheck.notNull(sig_list.get(0));
            final BigInteger id =
              new BigInteger(Long.toUnsignedString(ssig.getKeyID(), 16), 16);
            FossilManifest.LOG.debug(
              "blob {}: signed by 0x{}", uuid, String.format("%016x", id));
            return Option.some(id);
          }
          FossilManifest.LOG.debug("blob {}: PGP signature list was empty");
        }
      } else {
        FossilManifest.LOG.debug("blob {}: iterator has no pgp objects", uuid);
      }
    }

    return Option.none();
  }

  private static boolean lineIsSignature(final String line)
  {
    return "-----BEGIN PGP SIGNATURE-----".equals(line);
  }

  private static boolean lineIsSignedMessage(final String line)
  {
    return "-----BEGIN PGP SIGNED MESSAGE-----".equals(line);
  }
}
