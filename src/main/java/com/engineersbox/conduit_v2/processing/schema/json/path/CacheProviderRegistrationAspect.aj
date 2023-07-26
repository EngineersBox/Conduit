package com.engineersbox.conduit_v2.processing.schema.json.path;

import com.engineersbox.conduit_v2.processing.task.worker.ClientBoundForkJoinWorkerThead;
import com.jayway.jsonpath.spi.cache.Cache;

public aspect CacheProviderRegistrationAspect {

    pointcut pathFuncInstantiationVisit():
            execution(public static com.jayway.jsonpath.spi.cache.Cache com.jayway.com.jayway.jsonpath.spi.cache.CacheProvider.getCache());

    Cache around(): pathFuncInstantiationVisit() {
        if (!(Thread.currentThread() instanceof ClientBoundForkJoinWorkerThead cbfjwThread)) {
            return proceed();
        }
        final Cache cache = AffinityCacheProvider.getCacheInstance(cbfjwThread.getAffinityId());
        if (cache != null) {
            return cache;
        }
        return proceed();
    }
    // -javaagent:$MAVEN_REPOSITORY$/org/aspectj/aspectjweaver/1.9.19/aspectjweaver-1.9.19.jar
}
