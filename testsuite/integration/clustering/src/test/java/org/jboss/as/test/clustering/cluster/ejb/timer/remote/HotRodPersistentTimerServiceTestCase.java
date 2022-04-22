/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2022, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.clustering.cluster.ejb.timer.remote;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.clustering.cluster.ejb.timer.AbstractTimerServiceTestCase;
import org.jboss.as.test.shared.CLIServerSetupTask;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.ClassRule;
import org.junit.rules.TestRule;

/**
 * @author Paul Ferraro
 */
@ServerSetup({ InfinispanServerSetupTask.class, HotRodPersistentTimerServiceTestCase.TimerManagementSetupTask.class, HotRodPersistentTimerServiceTestCase.TimerServiceSetupTask.class })
public class HotRodPersistentTimerServiceTestCase extends AbstractTimerServiceTestCase {

    @ClassRule
    public static final TestRule INFINISPAN_SERVER_RULE = infinispanServerTestRule();

    @Deployment(name = DEPLOYMENT_1, managed = false, testable = false)
    @TargetsContainer(NODE_1)
    public static Archive<?> deployment0() {
        return createArchive();
    }

    @Deployment(name = DEPLOYMENT_2, managed = false, testable = false)
    @TargetsContainer(NODE_2)
    public static Archive<?> deployment1() {
        return createArchive();
    }

    private static Archive<?> createArchive() {
        return createArchive(HotRodPersistentTimerServiceTestCase.class);
    }

    static class TimerManagementSetupTask extends CLIServerSetupTask {
        TimerManagementSetupTask() {
            this.builder.node(THREE_NODES)
                    .setup("/subsystem=infinispan/cache-container=ejb/invalidation-cache=hotrod-persistent:add")
                    .setup("/subsystem=infinispan/cache-container=ejb/invalidation-cache=hotrod-persistent/component=expiration:add(interval=0)")
                    .setup("/subsystem=infinispan/cache-container=ejb/invalidation-cache=hotrod-persistent/component=locking:add(isolation=REPEATABLE_READ)")
                    .setup("/subsystem=infinispan/cache-container=ejb/invalidation-cache=hotrod-persistent/component=transaction:add(mode=BATCH)")
                    .setup("/subsystem=infinispan/cache-container=ejb/invalidation-cache=hotrod-persistent/store=hotrod:add(remote-cache-container=ejb, cache-configuration=default, fetch-state=false, purge=false, passivation=false, shared=true)")
                    .setup("/subsystem=distributable-ejb/infinispan-timer-management=hotrod:add(cache-container=ejb, cache=hotrod-persistent)")
                    .teardown("/subsystem=distributable-ejb/infinispan-timer-management=hotrod:remove")
                    .teardown("/subsystem=infinispan/cache-container=ejb/invalidation-cache=hotrod-persistent:remove")
                    ;
        }
    }

    static class TimerServiceSetupTask extends CLIServerSetupTask {
        TimerServiceSetupTask() {
            this.builder.node(THREE_NODES)
                    .setup("/subsystem=ejb3/service=timer-service:undefine-attribute(name=default-data-store)")
                    .setup("/subsystem=ejb3/service=timer-service:write-attribute(name=default-persistent-timer-management, value=hotrod)")
                    .teardown("/subsystem=ejb3/service=timer-service:undefine-attribute(name=default-persistent-timer-management)")
                    .teardown("/subsystem=ejb3/service=timer-service:write-attribute(name=default-data-store, value=file")
                    ;
        }
    }
}
