/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.transaction;

/**
 * Strategy for implementing transaction synchronization processors
 *
 * @author Oleg Zhurakousky
 * @author Gary Russell
 * @since 2.2
 *
 */
public interface TransactionSynchronizationProcessor {

	public abstract void processBeforeCommit(MessageSourceResourceHolder holder);

	public abstract void processAfterCommit(MessageSourceResourceHolder holder);

	public abstract void processAfterRollback(MessageSourceResourceHolder holder);

}