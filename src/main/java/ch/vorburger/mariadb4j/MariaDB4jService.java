/*
 * Copyright (c) 2014 Michael Vorburger
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */
package ch.vorburger.mariadb4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import ch.vorburger.exec.ManagedProcessException;

/**
 * MariaDB4j starter "Service". This is basically just "sugar" - you can of
 * course also use the DB class directly instead of this convenience utility.
 * 
 * This class does not depend on Spring, and is intended for direct "JavaBean"
 * like usage, and may be useful for DI containers such as Guice. When using
 * Spring, then the MariaDB4jSpringService may be of interest. If you're using
 * Spring Boot, then have a look at the MariaDB4jApplication.
 * 
 * The main() could be used typically from an IDE (waits for CR to shutdown..).
 * 
 * @author Michael Vorburger
 */
public class MariaDB4jService {

	protected DB db;
	protected DBConfigurationBuilder configBuilder;

	public DB getDB() {
		if (db == null)
			throw new IllegalStateException("start() me up first!");
		return db;
	}

	public DBConfigurationBuilder getConfiguration() {
		if (configBuilder == null)
			configBuilder = DBConfigurationBuilder.newBuilder();
		return configBuilder;
	}
	
	@PostConstruct // note this is from javax.annotation, not a Spring Framework dependency
	public void start() throws ManagedProcessException {
		db = DB.newEmbeddedDB(getConfiguration().build());
		db.start();
	}

	@PreDestroy // note this is from javax.annotation, not a Spring Framework dependency
	public void stop() throws ManagedProcessException {
		if (!isRunning())
			return;
		db.stop();
		db = null;
		configBuilder = null;
	}

	public boolean isRunning() {
		return db != null;
	}

	public static void main(String[] args) throws Exception {
		MariaDB4jService service = new MariaDB4jService();
		service.start();

		waitForKeyPressToCleanlyExit();
		
		// NOTE: In Eclipse, the MariaDB4j Shutdown Hook is not invoked on
		// exit.. so: (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38016)
		service.stop();
	}

	public static void waitForKeyPressToCleanlyExit() throws IOException {
		// NOTE: In Eclipse, System.console() is not available.. so: (@see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429)
		System.out.println("\n\nHit Enter to quit...");
		BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
		d.readLine();
	}
}