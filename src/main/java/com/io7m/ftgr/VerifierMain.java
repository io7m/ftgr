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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;
import org.apache.commons.collections4.BidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class VerifierMain
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Verifier.class);
  }

  public static void main(String[] args)
    throws IOException, JPropertyException
  {
    if (args.length < 1) {
      System.err.println("usage: ftgr.conf [logback.xml]");
      System.exit(1);
    }

    if (args.length > 1) {
      final LoggerContext context =
        (LoggerContext) LoggerFactory.getILoggerFactory();
      try {
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(args[1]);
      } catch (final Exception ex) {
        System.err.println("Could not load logback.xml: ");
        ex.printStackTrace();
      }
    }

    final FTGRConfiguration config =
      FTGRConfiguration.fromProperties(JProperties.fromFile(new File(args[0])));

    boolean ok = true;

    try (final FileInputStream s = new FileInputStream(
      config.getCommitMappingFile())) {

      final BidiMap<GitCommitName, FossilCommitName> in_commits =
        FossilCommitMap.fromStream(s);

      final File in_git_repos = config.getGitRepository();
      final FossilRepositorySpecificationBuilderType frb =
        FossilRepositorySpecification.newBuilder(
          config.getFossilRepository());
      final FossilRepositorySpecificationType in_fossil_repos = frb.build();

      final GitExecutableType in_git = GitExecutable.newExecutable(
        config.getGitExecutable(), config.getFaketimeExecutable());

      final FossilExecutableType in_fossil =
        FossilExecutable.newExecutable(config.getFossilExecutable());

      final File in_git_tmp =
        Files.createTempDirectory("verifier-git-tmp-").toFile();
      VerifierMain.LOG.info("using temporary git repository: {}", in_git_tmp);

      final File in_fossil_tmp =
        Files.createTempDirectory("verifier-fossil-tmp-").toFile();
      VerifierMain.LOG.info(
        "using temporary fossil repository: {}",
        in_fossil_tmp);

      final VerifierType v = Verifier.newVerifier(
        in_commits,
        in_git_repos,
        in_fossil_repos,
        in_git,
        in_fossil,
        in_git_tmp,
        in_fossil_tmp);

      final List<VerifierResult> results = v.verify();
      for (int index = 0; index < results.size(); ++index) {
        final VerifierResult r = results.get(index);
        ok = r.isOk() && ok;
        VerifierMain.LOG.info(
          "commit git:{}: (ok: {}) {}",
          r.getGitCommit(),
          Boolean.toString(r.isOk()),
          r.getMessage());
      }
    }

    if (ok == false) {
      throw new IOException("One or more commits failed verification!");
    }
  }
}
