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
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public final class FTGRMain
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FTGRMain.class);
  }

  public static void main(String args[])
    throws
    IOException,
    JPropertyException,
    FossilDatabaseException,
    FossilException,
    ReplayException
  {
    if (args.length < 1) {
      System.err.println("usage: ftgr.conf [logback.xml]");
      System.exit(1);
    }

    if (args.length > 1) {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      try {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(args[1]);
      } catch (Exception ex) {
        System.err.println("Could not load logback.xml: ");
        ex.printStackTrace();
      }
    }

    final FTGRConfiguration config =
      FTGRConfiguration.fromProperties(JProperties.fromFile(new File(args[0])));

    final GitRepositorySpecificationBuilderType git_repos_b =
      GitRepositorySpecification.newBuilder(
        config.getGitRepository());

    final Map<String, GitIdent> name_map = config.getNameMap();
    for (final String name : name_map.keySet()) {
      final GitIdent ident = NullCheck.notNull(name_map.get(name));
      FTGRMain.LOG.debug(
        "adding user name mapping: {} → {}:{}",
        name,
        ident.getName(),
        ident.getEmail());
      git_repos_b.addUserNameMapping(name, ident);
    }

    final GitRepositorySpecificationType git_repos = git_repos_b.build();

    final GitExecutableType git = GitExecutable.newExecutable(
      config.getGitExecutable(), config.getFaketimeExecutable());

    final GPGExecutableType gpg =
      GPGExecutable.newExecutable(config.getGPGExecutable());

    final FossilExecutableType fossil =
      FossilExecutable.newExecutable(config.getFossilExecutable());
    final FossilRepositorySpecificationBuilderType fossil_repos_b =
      FossilRepositorySpecification.newBuilder(config.getFossilRepository());
    final FossilRepositorySpecificationType fossil_repos =
      fossil_repos_b.build();
    final FossilDatabaseType db = FossilDatabase.openDatabase(
      config.getFossilRepository(), config.getFossilExecutable());

    final FossilModelBuilderType model_builder = FossilModel.newBuilder();
    final Map<Integer, FossilCommit> commits;
    try (final FossilDatabaseTransactionType t = db.newTransaction()) {
      commits = t.getAllCommits();

      for (final Integer k : commits.keySet()) {
        FossilCommit c = NullCheck.notNull(commits.get(k));
        model_builder.addCommit(c);
      }

      final List<FossilParentLink> es = t.getParentLinks();
      for (int index = 0; index < es.size(); ++index) {
        final FossilParentLink p = NullCheck.notNull(es.get(index));
        model_builder.addParentLink(p);
      }
    }

    for (final Integer k : commits.keySet()) {
      final FossilCommit c = NullCheck.notNull(commits.get(k));
      final String uuid = c.getCommitBlob();
      final ByteBuffer data = fossil.getBlobForUUID(fossil_repos, uuid);
      final OptionType<Long> key_id_opt =
        FossilManifest.getSignatureKey(uuid, data);

      if (key_id_opt.isSome()) {
        final Some<Long> some = (Some<Long>) key_id_opt;
        model_builder.setSigningKey(k.intValue(), some.get());
      }
    }

    final FossilModelType model = model_builder.build();
    final ReplayPlannerType planner =
      ReplayPlanner.newPlanner(gpg, fossil, git, git_repos, fossil_repos);
    final List<ReplayOperationType> plan = planner.plan(model);
    final ReplayExecutorType exec = ReplayExecutor.newExecutor();
    exec.executePlan(plan, config.getDryRun());
  }
}
