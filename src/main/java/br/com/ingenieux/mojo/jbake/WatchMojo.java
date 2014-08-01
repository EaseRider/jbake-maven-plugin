package br.com.ingenieux.mojo.jbake;

/*
 * Copyright 2013 ingenieux Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.ingenieux.mojo.jbake.util.DirWatcher;

/**
 * Runs jbake on a folder while watching for changes
 */
@Mojo(name = "watch", requiresDirectInvocation = true, requiresProject = false)
public class WatchMojo extends GenerateMojo {

  public void execute() throws MojoExecutionException {
    super.execute();

    getLog().info(
        "Now listening for changes on path " + inputDirectory.getPath());

    initServer();

    try {
      final AtomicBoolean done = new AtomicBoolean(false);
      final BufferedReader reader = new BufferedReader(
          new InputStreamReader(System.in));

      (new Thread() {
        @Override
        public void run() {
          try {
            getLog().info("Running. Hit <ENTER> to finish");
            reader.readLine();
          } catch (Exception exc) {
          } finally {
            done.set(true);
          }
        }
      }).start();

      DirWatcher dirWatcher = new DirWatcher(Paths.get(inputDirectory
                                                           .getPath()));

      Long lastProcessed = Long.valueOf(System.currentTimeMillis());

      do {
        Long result = dirWatcher.processEvents();

        if (null == result) {
          Thread.sleep(1000);
        } else if (result >= lastProcessed) {
          getLog().info("Refreshing");

          super.execute();

          lastProcessed = Long.valueOf(System.currentTimeMillis());
        }
      } while (!done.get());
    } catch (Exception exc) {
      getLog().info("Oops", exc);

      throw new MojoExecutionException("Oops", exc);
    } finally {
      getLog().info("Finishing");

      stopServer();
    }
  }

  protected void stopServer() {
  }

  protected void initServer() throws MojoExecutionException {
  }
}
